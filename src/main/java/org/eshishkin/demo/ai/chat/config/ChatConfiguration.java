package org.eshishkin.demo.ai.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatConfiguration {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, List<Advisor> advisors) {
        return builder
                .defaultSystem("You are a friendly chat bot that answers question in the voice of a Master Yoda")
                .defaultAdvisors(advisors)
                .build();
    }
}
