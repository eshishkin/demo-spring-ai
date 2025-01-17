package org.eshishkin.demo.ai.chat.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;

import static org.springframework.ai.vectorstore.RedisVectorStore.MetadataField.text;

@Configuration
public class VectorStorageConfiguration {

    @Bean
    public RedisVectorStore vectorStore(EmbeddingModel embeddingModel, RedisVectorStoreProperties properties,
                                        JedisConnectionFactory jedisConnectionFactory, ObjectProvider<ObservationRegistry> observationRegistry,
                                        ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
                                        BatchingStrategy batchingStrategy) {

        var config = RedisVectorStore.RedisVectorStoreConfig.builder()
                .withIndexName(properties.getIndex())
                .withPrefix(properties.getPrefix())
                .withMetadataFields(
                        text(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY),
                        text("conversationId"),
                        text("messageType"),
                        text(QuestionAnswerAdvisor.FILTER_EXPRESSION)
                )
                .build();

        return new RedisVectorStore(config, embeddingModel,
                new JedisPooled(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort()),
                properties.isInitializeSchema(), observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                customObservationConvention.getIfAvailable(() -> null), batchingStrategy);
    }
}
