package com.belajar.belajarspring.exception;

public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String key, long retryAfterSeconds) {
        super(String.format("Terlalu banyak percobaan login dari [%s], coba lagi dalam %d detik",
                key, retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
