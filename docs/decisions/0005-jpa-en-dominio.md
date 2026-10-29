# ADR-0005: Anotaciones JPA en las entidades de dominio

* Estado: Aceptada · Fecha: 2026-10-29

## Decisión
Las entidades persistentes de los módulos CRUD (identity, catalog, workbench,
audit) usan anotaciones JPA directamente. La lógica pura (motor de simulación,
motor de reglas) se modela con tipos inmutables sin JPA, alimentados por
adaptadores.

## Motivo
Separar modelo de dominio y modelo de persistencia duplicaría clases y mapeos
sin beneficio en los módulos de lectura/escritura simples.

## Consecuencias
Los controladores siguen exponiendo solo DTOs. Si un módulo gana lógica de
dominio rica, se reevalúa.
