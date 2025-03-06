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
                SearchRequest.builder().similarityThreshold(0.75).topK(15).build(),
                """
                        Context information is below, surrounded by ********
                        
                        ********
                        {question_answer_context}
                        ********
                        
                        Given the context and provided history information and not prior knowledge,
                        reply to the user comment. If the answer is not in the context or memory inform the user that you can't answer.
                        """
        );
    }

}
