package com.example.biansimulator.identity.application;

import com.example.biansimulator.identity.infrastructure.SecurityProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Bloqueo temporal tras intentos fallidos (ventana deslizante por usuario+IP).
 * En memoria: suficiente para una instancia; con varias instancias se moverá a
 * un almacén compartido (ver ADR-0003).
 */
@Service
public class LoginAttemptService {

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
    private final int maxFailures;
    private final Duration window;
    private final Clock clock;

    public LoginAttemptService(SecurityProperties properties, Clock clock) {
        this.maxFailures = properties.login().maxFailedAttempts();
        this.window = properties.login().lockoutWindow();
        this.clock = clock;
    }

    public boolean isBlocked(String username, String clientIp) {
        Deque<Instant> attempts = failures.get(key(username, clientIp));
        if (attempts == null) {
            return false;
        }
        synchronized (attempts) {
            prune(attempts);
            return attempts.size() >= maxFailures;
        }
    }

    /** Segundos hasta que caduque el intento fallido más antiguo de la ventana. */
    public long retryAfterSeconds(String username, String clientIp) {
        Deque<Instant> attempts = failures.get(key(username, clientIp));
        if (attempts == null) {
            return 0;
        }
        synchronized (attempts) {
            prune(attempts);
            Instant oldest = attempts.peekFirst();
            return oldest == null ? 0
                    : Math.max(1, Duration.between(clock.instant(), oldest.plus(window)).toSeconds());
        }
    }

    public void recordFailure(String username, String clientIp) {
        Deque<Instant> attempts = failures.computeIfAbsent(key(username, clientIp), k -> new ArrayDeque<>());
        synchronized (attempts) {
            prune(attempts);
            attempts.addLast(clock.instant());
        }
    }

    public void reset(String username, String clientIp) {
        failures.remove(key(username, clientIp));
    }

    private void prune(Deque<Instant> attempts) {
        Instant limit = clock.instant().minus(window);
        while (!attempts.isEmpty() && !attempts.peekFirst().isAfter(limit)) {
            attempts.pollFirst();
        }
    }

    private static String key(String username, String clientIp) {
        return username + '|' + clientIp;
    }
}
