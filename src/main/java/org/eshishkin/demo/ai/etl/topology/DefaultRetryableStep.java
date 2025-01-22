package org.eshishkin.demo.ai.etl.topology;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.internals.StaticTopicNameExtractor;
import org.eshishkin.demo.ai.etl.config.PipelineConfig;
import org.eshishkin.demo.ai.etl.dto.PipelineResponse;
import org.eshishkin.demo.ai.etl.dto.TextMessage;
import org.eshishkin.demo.ai.etl.topology.retry.RetryAdvisor;

import java.util.List;
import java.util.function.Function;

import static org.apache.kafka.streams.kstream.Branched.withConsumer;

@RequiredArgsConstructor
abstract class DefaultRetryableStep {
    private final StreamsBuilder builder;
    private final Serde<TextMessage> serde;
    private final PipelineConfig pipelineConfig;

    @PostConstruct
    protected void init() {
        var consumed = Consumed.with(Serdes.String(), serde);
        var config = getStepConfig();

        build(builder.stream(config.getInput(), consumed));
        build(builder.stream(config.getRetryTopic(), consumed));
    }

    protected abstract String getStepName();

    protected final PipelineResponse<List<TextMessage>> wrap(TextMessage message,
                                                             Function<TextMessage, List<TextMessage>> function) {
        try {
            return PipelineResponse.success(function.apply(message));
        } catch (Exception ex) {
            return PipelineResponse.failure(List.of(message), ex);
        }
    }

    private void build(KStream<String, TextMessage> stream) {
        var produced = Produced.with(Serdes.String(), serde);

        var filtered = stream
                .filter((k, v) -> v != null)
                .filter((k, v) -> StringUtils.isNotBlank(v.id()))
                .filter((k, v) -> StringUtils.isNotBlank(v.data()));

        process(filtered)
                .split()
                .branch((k, v) -> v.getStatus() == PipelineResponse.Status.SUCCESS, onSuccess(produced))
                .defaultBranch(onFailure(produced));
    }

    protected abstract KStream<String, PipelineResponse<List<TextMessage>>> process(KStream<String, TextMessage> stream);

    protected abstract Branched<String, PipelineResponse<List<TextMessage>>> onSuccess(Produced<String, TextMessage> produced);

    protected final Branched<String, PipelineResponse<List<TextMessage>>> onFailure(Produced<String, TextMessage> produced) {
        return withConsumer(k -> {
            var config = getStepConfig();

            RetryAdvisor.<String, TextMessage>builder()
                    .produced(produced)
                    .maxRetries(config.getRetryLimit())
                    .retryTopicNameExtractor(new StaticTopicNameExtractor<>(config.getRetryTopic()))
                    .deadLetterTopicNameExtractor(new StaticTopicNameExtractor<>(config.getDeadLetterTopic()))
                    .build()
                    .wrap(k.flatMapValues(PipelineResponse::getData));
        });
    }

    protected final PipelineConfig.StepConfig getStepConfig() {
        return pipelineConfig.getStepConfig(getStepName());
    }
}
