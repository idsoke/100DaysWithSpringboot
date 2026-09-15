package com.belajar.belajarspring.security;

import com.belajar.belajarspring.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class LoginRateLimiterServiceTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final long WINDOW_SECONDS = 60;

    private MutableClock clock;
    private LoginRateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        rateLimiterService = new LoginRateLimiterService(MAX_ATTEMPTS, WINDOW_SECONDS, clock);
    }

    @Test
    void checkAllowed_withinLimit_shouldNotThrow() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            rateLimiterService.checkAllowed("127.0.0.1");
        }
    }

    @Test
    void checkAllowed_exceedingLimit_shouldThrowWithRetryAfter() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            rateLimiterService.checkAllowed("127.0.0.1");
        }

        RateLimitExceededException exception = catchThrowableOfType(
                () -> rateLimiterService.checkAllowed("127.0.0.1"),
                RateLimitExceededException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getRetryAfterSeconds()).isGreaterThan(0).isLessThanOrEqualTo(WINDOW_SECONDS);
    }

    @Test
    void checkAllowed_differentKeys_shouldBeTrackedIndependently() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            rateLimiterService.checkAllowed("127.0.0.1");
        }

        // IP lain belum pernah mencoba, jadi tidak ikut kena limit milik "127.0.0.1".
        rateLimiterService.checkAllowed("10.0.0.5");
    }

    @Test
    void checkAllowed_afterWindowExpires_shouldResetCounter() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            rateLimiterService.checkAllowed("127.0.0.1");
        }

        clock.advance(Duration.ofSeconds(WINDOW_SECONDS + 1));

        rateLimiterService.checkAllowed("127.0.0.1");
    }

    @Test
    void reset_shouldAllowImmediateRetryWithoutWaitingForWindow() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            rateLimiterService.checkAllowed("127.0.0.1");
        }

        rateLimiterService.reset("127.0.0.1");

        rateLimiterService.checkAllowed("127.0.0.1");
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
