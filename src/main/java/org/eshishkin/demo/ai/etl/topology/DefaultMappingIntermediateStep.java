package org.eshishkin.demo.ai.etl.topology;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Produced;
import org.eshishkin.demo.ai.etl.config.PipelineConfig;
import org.eshishkin.demo.ai.etl.dto.PipelineResponse;
import org.eshishkin.demo.ai.etl.dto.TextMessage;

import java.util.List;

import static org.apache.kafka.streams.kstream.Branched.withConsumer;

abstract class DefaultMappingIntermediateStep extends DefaultRetryableStep {

    public DefaultMappingIntermediateStep(StreamsBuilder builder, Serde<TextMessage> serde, PipelineConfig config) {
        super(builder, serde, config);
    }

    @Override
    protected Branched<String, PipelineResponse<List<TextMessage>>> onSuccess(Produced<String, TextMessage> produced) {
        return withConsumer(k -> k
                .flatMapValues(PipelineResponse::getData)
                .to(getStepConfig().getOutput(), produced)
        );
    }
}
