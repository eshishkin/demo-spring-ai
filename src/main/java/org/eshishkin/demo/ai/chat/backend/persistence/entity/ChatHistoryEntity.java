package org.eshishkin.demo.ai.chat.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_history")
public class ChatHistoryEntity {
    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private String type;
}
