package org.eshishkin.demo.ai.chat.backend.tools.memory;

import lombok.RequiredArgsConstructor;
import org.eshishkin.demo.ai.chat.backend.persistence.ChatHistoryRepository;
import org.eshishkin.demo.ai.chat.backend.persistence.entity.ChatHistoryEntity;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class PersistentChatMemory implements ChatMemory {
    private final ChatHistoryRepository repository;

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        var entities = messages.stream()
                .map(m -> {
                    var message = new ChatHistoryEntity();
                    message.setMessageId(UUID.randomUUID().toString());
                    message.setConversationId(conversationId);
                    message.setTime(LocalDateTime.now());
                    message.setMessage(m.getText());
                    message.setType(m.getMessageType().getValue());
                    return message;
                })
                .toList();

        repository.saveAll(entities);
    }

    @Override
    @Transactional
    public List<Message> get(String conversationId, int lastN) {
        return repository.findAllByConversationIdOrderByTimeDesc(conversationId, PageRequest.of(0, lastN))
                .map(m -> {
                    var type = MessageType.fromValue(m.getType());
                    return (Message) switch (type) {
                        case USER -> new UserMessage(m.getMessage());
                        case ASSISTANT -> new AssistantMessage(m.getMessage());
                        case SYSTEM -> new SystemMessage(m.getMessage());
                        case null, default -> null;
                    };
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }
}
