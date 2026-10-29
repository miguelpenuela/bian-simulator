package com.example.biansimulator.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI bianSimulatorOpenApi() {
        return new OpenAPI().info(new Info()
                .title("BIAN Enterprise Architecture Simulator API")
                .version("v1")
                .description("API de una plataforma educativa. No está certificada por BIAN ni declara "
                        + "conformidad con el estándar. Autenticación por sesión: obtenga el token CSRF en "
                        + "GET /api/v1/auth/csrf y envíelo en la cabecera X-XSRF-TOKEN."));
    }
}
