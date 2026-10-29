package com.example.biansimulator.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.biansimulator.identity.application.LoginAttemptService;
import com.example.biansimulator.identity.infrastructure.SecurityProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    static final class MutableClock extends Clock {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

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
            return now;
        }
    }

    MutableClock clock;
    LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        var props = new SecurityProperties(new SecurityProperties.Login(3, Duration.ofMinutes(10)),
                new SecurityProperties.Cors(List.of()));
        service = new LoginAttemptService(props, clock);
    }

    @Test
    void blocksAfterMaxFailuresWithinWindow() {
        service.recordFailure("ana", "1.1.1.1");
        service.recordFailure("ana", "1.1.1.1");
        assertThat(service.isBlocked("ana", "1.1.1.1")).isFalse();
        service.recordFailure("ana", "1.1.1.1");
        assertThat(service.isBlocked("ana", "1.1.1.1")).isTrue();
        assertThat(service.isBlocked("ana", "2.2.2.2")).isFalse();
        assertThat(service.retryAfterSeconds("ana", "1.1.1.1")).isEqualTo(600);
    }

    @Test
    void unblocksWhenFailuresLeaveTheWindow() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("ana", "1.1.1.1");
        }
        clock.now = clock.now.plus(Duration.ofMinutes(10)).plusSeconds(1);
        assertThat(service.isBlocked("ana", "1.1.1.1")).isFalse();
    }

    @Test
    void resetClearsFailures() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("ana", "1.1.1.1");
        }
        service.reset("ana", "1.1.1.1");
        assertThat(service.isBlocked("ana", "1.1.1.1")).isFalse();
    }
}
