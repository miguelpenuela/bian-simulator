# ADR-0002: Stack tecnológico y versiones

* Estado: Aceptada · Fecha: 2026-10-29

## Decisión
* Backend: Java 21, Spring Boot 4.0.9 (Spring Framework 7, Spring Security 7,
  Hibernate 7, Jackson 3), Maven, Flyway, springdoc-openapi 3.x.
* BD: PostgreSQL 16.
* Pruebas backend: JUnit 5, Mockito, Testcontainers 2.x (versiones gestionadas
  por el BOM de Spring Boot).
* Frontend: React 19, TypeScript (strict), Vite, React Router, TanStack Query,
  React Hook Form, Zod, Tailwind CSS 4, Vitest, React Testing Library.
  React Flow y Zustand se incorporan cuando se implementa el workbench (no antes).
  Playwright se incorpora con la suite E2E.

## Motivo
Es el stack indicado en el enunciado. Se elige Spring Boot 4.0.x en lugar de 3.5
porque la línea 3.5 ya no tiene soporte OSS a la fecha del proyecto.

## Consecuencias
* Spring Boot 4 introduce starters modulares (`spring-boot-starter-webmvc`,
  `spring-boot-starter-flyway`) y Jackson 3; el código evita depender de
  APIs de Jackson en el dominio.
