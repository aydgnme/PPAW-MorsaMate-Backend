package me.aydgn.MorseMate.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class RateLimitService {

    private final ConcurrentMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    public boolean isBlocked(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null) {
            return false;
        }

        // Check if lockout has expired
        if (record.lockedUntil != null && LocalDateTime.now().isBefore(record.lockedUntil)) {
            log.warn("Access blocked for key: {} until {}", key, record.lockedUntil);
            return true;
        }

        // Clear expired lockout
        if (record.lockedUntil != null && LocalDateTime.now().isAfter(record.lockedUntil)) {
            attempts.remove(key);
            return false;
        }

        return false;
    }

    public void registerAttempt(String key) {
        AttemptRecord record = attempts.computeIfAbsent(key, k -> new AttemptRecord());
        record.count++;
        record.lastAttempt = LocalDateTime.now();

        if (record.count >= MAX_ATTEMPTS) {
            record.lockedUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
            log.warn("Key {} locked until {} due to {} failed attempts", key, record.lockedUntil, record.count);
        }
    }

    public void registerSuccess(String key) {
        attempts.remove(key);
    }

    public int getRemainingAttempts(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - record.count);
    }

    private static class AttemptRecord {
        int count = 0;
        LocalDateTime lastAttempt;
        LocalDateTime lockedUntil;
    }
}
