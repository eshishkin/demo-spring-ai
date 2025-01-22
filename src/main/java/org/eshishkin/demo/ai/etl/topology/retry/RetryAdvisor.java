package org.eshishkin.demo.ai.etl.topology.retry;

import lombok.Builder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;

import static org.apache.kafka.streams.kstream.Branched.withConsumer;

@Builder
public class RetryAdvisor<K, V> {

    private final Produced<K, V> produced;
    private final TopicNameExtractor<K, V> retryTopicNameExtractor;
    private final TopicNameExtractor<K, V> deadLetterTopicNameExtractor;
    private final int maxRetries;

    public void wrap(KStream<K, V> stream) {
        stream
                .process((ProcessorSupplier<K, V, K, RetryAttemptAwareResponse<V>>) RetryHeaderEnrichmentProcessor::new)
                .split()
                .branch((k, v) -> v.retryAttempts() <= maxRetries, retryAgain())
                .defaultBranch(toDeadLetter());
    }

    private Branched<K, RetryAttemptAwareResponse<V>> toDeadLetter() {
        return withConsumer(c -> c
                .mapValues(RetryAttemptAwareResponse::value)
                .to(deadLetterTopicNameExtractor, produced)
        );
    }

    private Branched<K, RetryAttemptAwareResponse<V>> retryAgain() {
        return withConsumer(c -> c
                .mapValues(RetryAttemptAwareResponse::value)
                .to(retryTopicNameExtractor, produced)
        );
    }
}
