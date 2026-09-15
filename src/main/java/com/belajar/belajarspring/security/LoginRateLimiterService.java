package com.belajar.belajarspring.security;

import com.belajar.belajarspring.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Fixed-window counter sederhana untuk membatasi percobaan login per key (biasanya IP client),
// mencegah brute-force credential guessing. Bukan sliding window/token bucket seperti Bucket4j,
// jadi ada celah kecil di batas window (burst dobel di detik pergantian window) - cukup untuk
// belajar konsepnya tanpa menambah dependency eksternal.
@Service
public class LoginRateLimiterService {

    private final int maxAttempts;
    private final Duration windowDuration;
    private final Clock clock;
    private final Map<String, AttemptWindow> windows = new ConcurrentHashMap<>();

    public LoginRateLimiterService(
            @Value("${login.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${login.rate-limit.window-seconds:60}") long windowSeconds,
            Clock clock) {
        this.maxAttempts = maxAttempts;
        this.windowDuration = Duration.ofSeconds(windowSeconds);
        this.clock = clock;
    }

    // Dipanggil sebelum autentikasi diproses. Melempar RateLimitExceededException
    // begitu jumlah percobaan dalam window berjalan melebihi batas.
    public void checkAllowed(String key) {
        Instant now = clock.instant();
        AttemptWindow window = windows.compute(key, (k, existing) ->
                (existing == null || existing.isExpired(now, windowDuration))
                        ? new AttemptWindow(now)
                        : existing);

        int attempts = window.count.incrementAndGet();
        if (attempts > maxAttempts) {
            long retryAfterSeconds = Duration.between(now, window.windowStart.plus(windowDuration)).getSeconds();
            throw new RateLimitExceededException(key, Math.max(retryAfterSeconds, 1));
        }
    }

    // Dipanggil setelah login berhasil, supaya user yang sempat salah ketik beberapa kali
    // tidak ikut kena limit begitu berhasil login.
    public void reset(String key) {
        windows.remove(key);
    }

    private static class AttemptWindow {
        private final Instant windowStart;
        private final AtomicInteger count = new AtomicInteger(0);

        private AttemptWindow(Instant windowStart) {
            this.windowStart = windowStart;
        }

        private boolean isExpired(Instant now, Duration windowDuration) {
            return windowStart.plus(windowDuration).isBefore(now);
        }
    }
}
