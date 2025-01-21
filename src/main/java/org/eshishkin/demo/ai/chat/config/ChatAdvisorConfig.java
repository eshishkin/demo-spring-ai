package org.eshishkin.demo.ai.chat.config;

import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatAdvisorConfig {

    @Bean
    public Advisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    public Advisor promtChatMemoryAdvisor() {
        return new PromptChatMemoryAdvisor(new InMemoryChatMemory());
    }

    @Bean
    public Advisor questionAnswerAdvisor(VectorStore store) {
        return new QuestionAnswerAdvisor(
                store,
                SearchRequest.defaults().withSimilarityThreshold(0.85)
        );
    }

}
