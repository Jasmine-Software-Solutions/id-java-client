package com.jasminesoftwaresolutions.idclient.retry;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Configurable retry policy with exponential backoff and jitter.
 */
public class RetryPolicy {
    private final int maxRetries;
    private final Duration initialBackoff;
    private final Duration maxBackoff;
    private final double backoffMultiplier;
    private final double jitterFactor;
    private final Set<Integer> retryableStatusCodes;

    private RetryPolicy(Builder builder) {
        this.maxRetries = builder.maxRetries;
        this.initialBackoff = builder.initialBackoff;
        this.maxBackoff = builder.maxBackoff;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.jitterFactor = builder.jitterFactor;
        this.retryableStatusCodes = builder.retryableStatusCodes;
    }

    /**
     * Returns the default retry policy: 3 retries, 500ms initial backoff, 10s max,
     * 2x multiplier, 0.2 jitter, retrying on 429, 500, 502, 503, 504.
     */
    public static RetryPolicy defaultPolicy() {
        return new Builder().build();
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getInitialBackoff() {
        return initialBackoff;
    }

    public Duration getMaxBackoff() {
        return maxBackoff;
    }

    public boolean isRetryable(int statusCode) {
        return retryableStatusCodes.contains(statusCode);
    }

    /**
     * Computes the delay for the given attempt (0-based). Returns {@code null} if
     * max retries exceeded.
     */
    public Duration delayForAttempt(int attempt) {
        if (attempt >= maxRetries) return null;

        double baseMs = initialBackoff.toMillis() * Math.pow(backoffMultiplier, attempt);
        long cappedMs = Math.min((long) baseMs, maxBackoff.toMillis());

        // Apply jitter: delay = cappedMs * (1 - jitterFactor * random)
        double jitter = 1.0 - jitterFactor * ThreadLocalRandom.current().nextDouble();
        long delayMs = Math.round(cappedMs * jitter);

        return Duration.ofMillis(Math.max(delayMs, 0));
    }

    public static class Builder {
        private int maxRetries = 5;
        private Duration initialBackoff = Duration.ofMillis(500);
        private Duration maxBackoff = Duration.ofSeconds(10);
        private double backoffMultiplier = 2.0;
        private double jitterFactor = 0.2;
        private Set<Integer> retryableStatusCodes = Set.of(429, 500, 502, 503, 504);

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder initialBackoff(Duration initialBackoff) {
            this.initialBackoff = initialBackoff;
            return this;
        }

        public Builder maxBackoff(Duration maxBackoff) {
            this.maxBackoff = maxBackoff;
            return this;
        }

        public Builder backoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public Builder jitterFactor(double jitterFactor) {
            this.jitterFactor = jitterFactor;
            return this;
        }

        public Builder retryableStatusCodes(Set<Integer> retryableStatusCodes) {
            this.retryableStatusCodes = Set.copyOf(retryableStatusCodes);
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
