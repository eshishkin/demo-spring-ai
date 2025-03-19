package org.eshishkin.demo.ai.chat.backend.config;

import org.eshishkin.demo.ai.chat.backend.persistence.ChatHistoryRepository;
import org.eshishkin.demo.ai.chat.backend.tools.memory.PersistentChatMemory;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
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
    public Advisor promtChatMemoryAdvisor(PersistentChatMemory memory) {
        return new PromptChatMemoryAdvisor(memory);
    }

    @Bean
    public PersistentChatMemory persistentChatMemory(ChatHistoryRepository repository) {
        return new PersistentChatMemory(repository);
    }

    @Bean
    public Advisor questionAnswerAdvisor(VectorStore store) {
        return new QuestionAnswerAdvisor(
                store,
                SearchRequest.builder().similarityThreshold(0.75).topK(30).build(),
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
