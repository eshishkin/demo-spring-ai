package org.eshishkin.demo.ai.etl.topology.retry;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.nio.ByteBuffer;
import java.util.Optional;

class RetryHeaderEnrichmentProcessor<K, V> implements Processor<K, V, K, RetryAttemptAwareResponse<V>> {

    private static final String RETRY_ATTEMPTS = "retry-attempts";
    private static final String ORIGINAL_TIMESTAMP = "original-timestamp";

    private ProcessorContext<K, RetryAttemptAwareResponse<V>> context;

    @Override
    public void init(ProcessorContext<K, RetryAttemptAwareResponse<V>> context) {
        this.context = context;
    }

    @Override
    public void process(org.apache.kafka.streams.processor.api.Record<K, V> record) {
        int attempt = evaluateRetryAttempt(record) + 1;

        var forwarded = new org.apache.kafka.streams.processor.api.Record<>(
                record.key(), new RetryAttemptAwareResponse<>(record.value(), attempt), record.timestamp()
        );

        forwarded.headers().add(RETRY_ATTEMPTS, ByteBuffer.wrap(new byte[4]).putInt(attempt).array());
        forwarded.headers().add(ORIGINAL_TIMESTAMP, evaluateOriginalTimestamp(record));

        context.forward(forwarded);
    }


    private byte[] evaluateOriginalTimestamp(org.apache.kafka.streams.processor.api.Record<K, V> record) {
        return Optional.ofNullable(record.headers().lastHeader(ORIGINAL_TIMESTAMP))
                .map(Header::value)
                .orElseGet(() -> ByteBuffer.wrap(new byte[8]).putLong(record.timestamp()).array());
    }

    private Integer evaluateRetryAttempt(Record<K, V> record) {
        return Optional.ofNullable(record.headers().lastHeader(RETRY_ATTEMPTS))
                .map(Header::value)
                .map(ByteBuffer::wrap)
                .map(b -> {
                    try {
                        return b.getInt();
                    } catch (Exception ex) {
                        return 0;
                    }
                })
                .orElse(0);
    }

}
