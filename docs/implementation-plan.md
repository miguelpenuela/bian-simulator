# Plan de implementación y backlog

> Última revisión: 2026-10-29

El trabajo avanza en **incrementos verticales**: cada incremento entrega una
funcionalidad completa (BD → API → UI → pruebas) en lugar de capas aisladas.

## Incremento 1 — Base técnica + identidad + exploración de catálogo *(en curso)*

Cubre la Fase 1, la Fase 2 y la parte de lectura de la Fase 3.

- [x] Documentación de arquitectura, modelo de datos y ADRs
- [ ] Backend Spring Boot 4 con Flyway, Actuator, OpenAPI y errores RFC 9457
- [ ] Login por sesión con CSRF, bloqueo por intentos, roles y permisos
- [ ] Auditoría de inicio/cierre de sesión
- [ ] API de catálogo: versiones, facetas, búsqueda paginada y ficha de detalle
- [ ] Catálogo de demostración etiquetado (`EDUCATIONAL_DEMO`, `UNVERIFIED`)
- [ ] Frontend: login, layout, dashboard básico, explorador y ficha
- [ ] Docker Compose, Dockerfiles y `.env.example`
- [ ] CI en GitHub Actions (backend con Testcontainers, frontend, smoke de compose)

**Criterio de aceptación**: un usuario inicia sesión, busca y filtra el catálogo
y navega a la ficha de un Service Domain y sus elementos relacionados; todo
corriendo con `docker compose up`, con pruebas en verde en CI.

## Backlog priorizado

Prioridad: P1 = siguiente, P2 = después, P3 = más adelante.

| # | Prio | Historia / tarea | Módulo |
|---|------|------------------|--------|
| 1 | P1 | Workspaces y membresía; protección de recursos por workspace | identity/workbench |
| 2 | P1 | CRUD de proyectos y diagramas con revisión optimista | workbench |
| 3 | P1 | Editor React Flow: paleta desde catálogo, nodos personalizados, conexiones, panel de propiedades | workbench (UI) |
| 4 | P1 | Guardado automático, deshacer/rehacer, exportación/importación JSON versionada | workbench |
| 5 | P1 | Contratos del motor de simulación + ejecución determinista paso a paso | simulation |
| 6 | P1 | Escenario demo "Transferencia entre cuentas" ejecutable | scenarios/simulation |
| 7 | P2 | Fallos simulados (timeout, no disponible, reintentos, rechazo de regla) y límites | simulation |
| 8 | P2 | Motor de reglas de validación + reglas 1–7 (objetivas) | validation |
| 9 | P2 | Reglas 8–12 (dependientes de escenario / heurísticas) | validation |
| 10 | P2 | Laboratorio: 5 escenarios educativos, comparación con criterios | scenarios |
| 11 | P2 | Banco de preguntas original, modo práctica y examen simulado, historial | assessment |
| 12 | P2 | Importación de catálogo JSON/CSV con vista previa, duplicados y confirmación | catalog |
| 13 | P2 | Favoritos e historial de consulta del catálogo (persistidos) | catalog |
| 14 | P2 | Visualización de relaciones del catálogo con React Flow | catalog (UI) |
| 15 | P3 | Informes Markdown/HTML/JSON; PDF | reporting |
| 16 | P3 | Versiones de proyecto y comparación visual | workbench |
| 17 | P3 | Exportación SVG/PNG | workbench |
| 18 | P3 | Administración de usuarios y roles | identity |
| 19 | P3 | Pruebas E2E Playwright de los 10 flujos | qa |
| 20 | P3 | ArchUnit, request-id/MDC, rate limiting general, hardening | shared |

## Decisiones que requieren confirmación del responsable del producto

1. **Fuente oficial de datos BIAN**: ¿se dispone de un archivo exportado del
   BIAN Service Landscape con permiso de uso? Hasta entonces solo existe el
   catálogo educativo.
2. **Groupid/paquete Java**: se usa `com.example.biansimulator` según el
   enunciado; ¿cambiarlo por un dominio propio antes de crecer?
3. **Rama por defecto**: GitHub tiene `main` como rama por defecto, pero el
   trabajo se publica en `master` según lo indicado. Conviene unificar.
4. **Multi-tenencia SaaS**: ¿workspace = tenant, o habrá una entidad
   `organization` por encima? Afecta al incremento 2.
5. **Generación de PDF**: biblioteca a elegir (OpenPDF vs. renderizado HTML con
   navegador headless) en el incremento de reportes.
