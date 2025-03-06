package org.eshishkin.demo.ai.etl.topology;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.eshishkin.demo.ai.etl.config.PipelineConfig;
import org.eshishkin.demo.ai.etl.dto.PipelineResponse;
import org.eshishkin.demo.ai.etl.dto.TextMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class TokenizerStep extends DefaultMappingIntermediateStep {
    private final DocumentTransformer splitter = new TokenTextSplitter();

    public TokenizerStep(StreamsBuilder builder, Serde<TextMessage> serde, PipelineConfig pipelineConfig) {
        super(builder, serde, pipelineConfig);
    }

    @Override
    protected String getStepName() {
        return "tokenizer";
    }

    @Override
    protected KStream<String, PipelineResponse<List<TextMessage>>> process(KStream<String, TextMessage> stream) {
        return stream.mapValues(x -> wrap(x, this::tokenize));
    }

    private List<TextMessage> tokenize(TextMessage message) {
        var document = new Document(message.id(), message.data(), message.metadata());
        return splitter.transform(List.of(document))
                .stream()
                .map(doc -> new TextMessage(doc.getId(), doc.getText(), doc.getMetadata()))
                .toList();
    }
}
