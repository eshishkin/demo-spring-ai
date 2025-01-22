package org.eshishkin.demo.ai.etl.topology.retry;

record RetryAttemptAwareResponse<V>(V value, int retryAttempts) {
}
