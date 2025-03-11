package org.eshishkin.demo.ai.knowledge;

import lombok.RequiredArgsConstructor;
import org.eshishkin.demo.ai.etl.dto.TextMessage;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;


@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/knowledge")
public class KnowledgeController {
    private final KafkaTemplate<String, TextMessage> kafkaTemplate;

    @PostMapping(value = "/text", consumes = {"text/plain", "text/markdown"})
    public void addTextMessage(@RequestBody String message, @RequestHeader("Content-Type") String type) {
        process(getReader(type, new ByteArrayResource(message.getBytes(StandardCharsets.UTF_8))));
    }

    @PostMapping("/file")
    public void upload(@RequestParam("file") MultipartFile file) {
        process(getReader(file.getContentType(), file.getResource()));
    }

    private void process(DocumentReader reader) {
        reader.read().forEach(doc -> {
            var future = kafkaTemplate.send(
                    "events_raw",
                    doc.getId(),
                    new TextMessage(doc.getId(), doc.getText(), doc.getMetadata())
            );

            future.join();
        });
    }

    private DocumentReader getReader(String type, Resource resource) {
        return switch (type) {
            case "text/plain" -> new TextReader(resource);
            case "text/markdown" -> {
                var config = MarkdownDocumentReaderConfig.builder()
                        .withAdditionalMetadata(Map.of(TextReader.SOURCE_METADATA, defaultIfNull(resource.getFilename(), EMPTY)))
                        .build();
                yield new MarkdownDocumentReader(resource, config);
            }
            case null -> throw new IllegalArgumentException("Content-Type is missing");
            default -> throw new IllegalArgumentException("Unsupported content type: " + type);
        };
    }
}

