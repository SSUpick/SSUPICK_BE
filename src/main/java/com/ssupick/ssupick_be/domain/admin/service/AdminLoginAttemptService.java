package com.ssupick.ssupick_be.domain.admin.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AdminLoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final ConcurrentMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String key) {
        LoginAttempt attempt = attempts.get(key);
        if (attempt == null || attempt.lockedUntil == null) {
            return false;
        }

        if (Instant.now().isAfter(attempt.lockedUntil)) {
            attempts.remove(key, attempt);
            return false;
        }

        return true;
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    public void recordFailure(String key) {
        attempts.compute(key, (ignored, current) -> {
            int failedCount = current == null ? 1 : current.failedCount + 1;
            Instant lockedUntil = failedCount >= MAX_FAILED_ATTEMPTS
                    ? Instant.now().plus(LOCK_DURATION)
                    : null;

            return new LoginAttempt(failedCount, lockedUntil);
        });
    }

    private record LoginAttempt(
            int failedCount,
            Instant lockedUntil
    ) {}
}
