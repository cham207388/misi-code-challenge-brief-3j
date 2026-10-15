package com.wsllclabs.memorialops.identity.service;

import com.wsllclabs.memorialops.exception.RateLimitedException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int LIMIT = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void guard(String username) {
        String key = normalize(username);
        Instant cutoff = Instant.now(clock).minusSeconds(WINDOW_SECONDS);
        Deque<Instant> window = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (window) {
            evict(window, cutoff);
            if (window.size() >= LIMIT) {
                throw new RateLimitedException("Too many sign-in attempts. Try again in a minute.");
            }
        }
    }

    public void recordFailure(String username) {
        String key = normalize(username);
        Instant now = Instant.now(clock);
        Instant cutoff = now.minusSeconds(WINDOW_SECONDS);
        Deque<Instant> window = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (window) {
            evict(window, cutoff);
            window.addLast(now);
        }
    }

    public void recordSuccess(String username) {
        attempts.remove(normalize(username));
    }

    private static void evict(Deque<Instant> window, Instant cutoff) {
        while (!window.isEmpty() && window.peekFirst().isBefore(cutoff)) {
            window.removeFirst();
        }
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
