package org.eshishkin.demo.ai.chat.config;

import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;

import static org.springframework.ai.vectorstore.redis.RedisVectorStore.MetadataField.text;


@Configuration
public class VectorStorageConfiguration {

    @Bean
    public RedisVectorStore vectorStore(EmbeddingModel embeddingModel,
                                        RedisVectorStoreProperties properties,
                                        JedisConnectionFactory jedisConnectionFactory) {

        var jedis = new JedisPooled(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());
        return RedisVectorStore.builder(jedis, embeddingModel)
                .indexName(properties.getIndex())
                .prefix(properties.getPrefix())
                .metadataFields(
                        text(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY),
                        text("conversationId"),
                        text("messageType"),
                        text(QuestionAnswerAdvisor.FILTER_EXPRESSION)
                )
                .build();
    }
}
