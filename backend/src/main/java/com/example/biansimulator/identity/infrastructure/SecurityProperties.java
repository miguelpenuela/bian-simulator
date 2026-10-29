package com.example.biansimulator.identity.infrastructure;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(Login login, Cors cors) {

    public SecurityProperties {
        login = login == null ? new Login(5, Duration.ofMinutes(15)) : login;
        cors = cors == null ? new Cors(List.of()) : cors;
    }

    public record Login(int maxFailedAttempts, Duration lockoutWindow) {
    }

    public record Cors(List<String> allowedOrigins) {

        public Cors {
            allowedOrigins = allowedOrigins == null ? List.of()
                    : allowedOrigins.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
    }
}
