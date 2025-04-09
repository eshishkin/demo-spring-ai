package org.eshishkin.demo.ai.etl.topology;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.eshishkin.demo.ai.etl.config.PipelineConfig;
import org.eshishkin.demo.ai.etl.dto.PipelineResponse;
import org.eshishkin.demo.ai.etl.dto.TextMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.document.MetadataMode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EventMetadataEnricherStep extends DefaultMappingIntermediateStep {
    private static final int DEFAULT_KEYWORD_COUNT = 5;
    private final EventMetadataEnricher enricher;

    public EventMetadataEnricherStep(StreamsBuilder builder, Serde<TextMessage> serde, PipelineConfig config, ChatModel llm) {
        super(builder, serde, config);

        enricher = new EventMetadataEnricher(llm, getKeywordsNumber());
    }

    @Override
    protected String getStepName() {
        return "event-metadata-enricher";
    }

    @Override
    protected KStream<String, PipelineResponse<List<TextMessage>>> process(KStream<String, TextMessage> stream) {
        return stream.mapValues(x -> wrap(x, this::process));
    }

    private List<TextMessage> process(TextMessage message) {
        var document = new Document(message.id(), message.data(), message.metadata());
        return enricher.transform(List.of(document))
                .stream()
                .map(doc -> new TextMessage(doc.getId(), doc.getText(), doc.getMetadata()))
                .toList();
    }

    private int getKeywordsNumber() {
        try {
            return Integer.parseInt(getStepConfig().getAdditionalProperty("keywords_number"));
        } catch (NumberFormatException ex) {
            return DEFAULT_KEYWORD_COUNT;
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class EventMetadataEnricher implements DocumentTransformer {
        private final ChatModel llm;
        private final int keywordCount;
        private final BeanOutputConverter<EventMetadata> converter = new BeanOutputConverter<>(EventMetadata.class);

        private static final String CONTEXT_STR_PLACEHOLDER = "context_str";

        private final static String TEMPLATE = """
                Here is the content wrapped by ###:
                
                ###
                {context_str}
                ###
                
                Check if the section represents an event with specific date. Print answer in the language of the section as json in format without markdown markup
                
                event: (boolean)
                description: (two line summary of the section)
                date:  (ISO date-time format if it's an event, null otherwise)
                place:  (if it's an event, null otherwise)
                keywords: (list of top {keywordCount} keywords)
                """;

        @Override
        public List<Document> apply(List<Document> documents) {

            for (Document document : documents) {
                var context = document.getFormattedContent(MetadataMode.NONE);
                var prompt = new PromptTemplate(TEMPLATE).create(
                        Map.of(CONTEXT_STR_PLACEHOLDER, context, "keywordCount", keywordCount)
                );

                askModel(prompt).ifPresent(result -> {
                    document.getMetadata().put("is_event", result.event);
                    document.getMetadata().put("description", result.description);
                    if (result.date != null) {
                        document.getMetadata().put("event_date", result.date);
                    }
                    if (result.place != null) {
                        document.getMetadata().put("event_place", result.place);
                    }
                    document.getMetadata().put("keywords", result.keywords);
                });
            }

            return documents;
        }

        private Optional<EventMetadata> askModel(Prompt prompt) {
            try {
                return Optional.ofNullable(converter.convert(llm.call(prompt).getResult().getOutput().getText()));
            } catch (Exception ex) {
                log.warn("Unable to parse content to EventMetadata", ex);
                return Optional.empty();
            }
        }
    }

    public record EventMetadata(Boolean event, String description, LocalDateTime date, String place,
                                List<String> keywords) {
    }
}
