package org.eshishkin.demo.ai.web;

import lombok.RequiredArgsConstructor;
import org.eshishkin.demo.ai.etl.dto.TextMessage;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class RagController {
    private final KafkaTemplate<String, TextMessage> kafkaTemplate;

    @PostMapping("/send-to-kafka")
    public void save3(@RequestBody String message) {

        var reader = new TextReader(new InputStreamResource(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8))));
        var documents = reader.read();

        documents.forEach(doc -> {
            var future = kafkaTemplate.send(
                    "events_raw",
                    doc.getId(),
                    new TextMessage(doc.getId(), doc.getContent(), doc.getMetadata())
            );

            future.join();
        });
    }
}
