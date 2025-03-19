package org.eshishkin.demo.ai.chat.backend.persistence;

import org.eshishkin.demo.ai.chat.backend.persistence.entity.ChatHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

public interface ChatHistoryRepository extends JpaRepository<ChatHistoryEntity, String> {

    @Modifying
    @Transactional
    void deleteByConversationId(String conversationId);

    Stream<ChatHistoryEntity> findAllByConversationIdOrderByTimeAsc(String conversationId, Pageable page);

    Stream<ChatHistoryEntity> findAllByConversationIdOrderByTimeDesc(String conversationId, Pageable page);

    @Query("""
            SELECT e FROM ChatHistoryEntity e 
            WHERE e.messageId in (
                SELECT j.messageId FROM ChatHistoryEntity j 
                WHERE j.conversationId = e.conversationId
                ORDER BY j.time ASC 
                LIMIT 1
            )
            """
    )
    Stream<ChatHistoryEntity> findFirstMessageByConversation();
}
