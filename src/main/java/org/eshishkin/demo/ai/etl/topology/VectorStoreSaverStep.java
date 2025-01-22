package org.eshishkin.demo.ai.etl.topology;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.eshishkin.demo.ai.etl.config.PipelineConfig;
import org.eshishkin.demo.ai.etl.dto.PipelineResponse;
import org.eshishkin.demo.ai.etl.dto.TextMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.function.Function.identity;
import static org.apache.kafka.streams.kstream.Branched.withFunction;

@Component
public class VectorStoreSaverStep extends DefaultRetryableStep {
    private final VectorStore store;

    public VectorStoreSaverStep(VectorStore store, StreamsBuilder builder, Serde<TextMessage> serde, PipelineConfig config) {
        super(builder, serde, config);
        this.store = store;
    }


    @Override
    protected String getStepName() {
        return "event-vector-saver";
    }

    @Override
    protected KStream<String, PipelineResponse<List<TextMessage>>> process(KStream<String, TextMessage> stream) {
        return stream.mapValues(x -> wrap(x, this::process));
    }

    private List<TextMessage> process(TextMessage message) {
        var document = new Document(message.id(), message.data(), message.metadata());
        store.write(List.of(document));
        return List.of(message);
    }

    protected Branched<String, PipelineResponse<List<TextMessage>>> onSuccess(Produced<String, TextMessage> produced) {
        return withFunction(identity());
    }
}
