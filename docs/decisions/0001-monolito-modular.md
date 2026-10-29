# ADR-0001: Monolito modular en lugar de microservicios

* Estado: Aceptada · Fecha: 2026-10-29

## Contexto
El producto tiene ocho módulos funcionales con dominios acoplados (el simulador
lee diagramas, la validación lee catálogo y escenarios). El equipo es pequeño y
el producto aún no tiene carga que justifique despliegues independientes.

## Decisión
Un único despliegue Spring Boot, con un paquete de primer nivel por módulo y
reglas de dependencia explícitas (solo servicios de aplicación públicos entre
módulos; nunca repositorios ni entidades ajenas).

## Consecuencias
* (+) Transacciones locales, un solo pipeline, despliegue simple.
* (+) Extraer un módulo a servicio más adelante es viable si se respetan los límites.
* (−) Los límites dependen de disciplina; se añadirán pruebas ArchUnit.
