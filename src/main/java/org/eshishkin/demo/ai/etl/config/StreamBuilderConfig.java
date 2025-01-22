package org.eshishkin.demo.ai.etl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.eshishkin.demo.ai.etl.dto.TextMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.support.serializer.JsonSerde;

import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;

@Configuration
@EnableKafkaStreams
public class StreamBuilderConfig {

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
        return factoryBean -> factoryBean.setStreamsUncaughtExceptionHandler(ex -> REPLACE_THREAD);
    }

    @Bean
    public Serde<TextMessage> textMessageSerde(ObjectMapper mapper) {
        return new JsonSerde<>(TextMessage.class, mapper);
    }
}
