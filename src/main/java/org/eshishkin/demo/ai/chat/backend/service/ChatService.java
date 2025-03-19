package org.eshishkin.demo.ai.chat.backend.service;

import lombok.RequiredArgsConstructor;
import org.eshishkin.demo.ai.chat.backend.persistence.ChatHistoryRepository;
import org.eshishkin.demo.ai.chat.backend.persistence.entity.ChatHistoryEntity;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;
    private final ChatHistoryRepository repository;

    public Mono<String> ask(String chatId, String message) {
        var response = chatClient.prompt()
                .user(message)
                .advisors(spec -> {
                    spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
                })
                .stream();
        return response.content()
                .collectList()
                .map(messages -> String.join("", messages));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<ChatHistoryEntity> getChatHistory(String chatId) {
        return repository.findAllByConversationIdOrderByTimeAsc(chatId, Pageable.unpaged()).toList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Map<LocalDate, List<ChatHistoryEntity>> getChats() {
        return repository.findFirstMessageByConversation()
                .collect(groupingBy(c -> c.getTime().toLocalDate()));
    }
}
