# Estado del proyecto

> Última actualización: 2026-10-29 · Rama: `master`

## Fase actual

**Incremento 1 completado**: Fase 1 (diseño), Fase 2 (base técnica) y Fase 3
en su parte de lectura (exploración del catálogo).

Siguiente: **Incremento 2 — Architecture Workbench** (Fase 4).

## Funcionalidades completadas (verificadas por pruebas)

| Funcionalidad | Backend | Frontend | Pruebas |
|---------------|:------:|:--------:|---------|
| Inicio y cierre de sesión (sesión de servidor + CSRF) | ✔ | ✔ | `AuthenticationFlowIT`, `LoginPage.test` |
| Rotación de id de sesión al autenticar | ✔ | — | `AuthenticationFlowIT.sessionIdChangesOnLogin` |
| Bloqueo temporal tras 5 intentos fallidos (usuario+IP) | ✔ | ✔ (mensaje) | `LoginAttemptServiceTest`, `AuthenticationFlowIT` |
| Roles y permisos centralizados, `@PreAuthorize` por permiso | ✔ | — | `PermissionsIT` |
| Auditoría de login (éxito, fallo, bloqueo) y logout | ✔ | — | `AuthenticationFlowIT` |
| Errores RFC 9457 homogéneos | ✔ | ✔ (`ApiError`) | ITs + `http.test` |
| Catálogo: versiones con procedencia | ✔ | ✔ | `CatalogApiIT`, `CatalogExplorerPage.test` |
| Catálogo: búsqueda por texto, filtros por tipo y categoría, paginación | ✔ | ✔ | `CatalogApiIT`, `CatalogExplorerPage.test` |
| Catálogo: facetas (conteos) | ✔ | ✔ | `CatalogApiIT` |
| Ficha de elemento y de Service Domain con relaciones | ✔ | ✔ | `CatalogApiIT`, `ElementDetailPage.test` |
| Catálogo demo educativo etiquetado (`EDUCATIONAL_DEMO`, `UNVERIFIED`) | ✔ | ✔ (aviso permanente) | `CatalogApiIT` |
| Dashboard sin estadísticas inventadas; módulos futuros marcados | — | ✔ | — |
| Docker Compose con healthchecks | ✔ | ✔ | job `compose-smoke` en CI |
| OpenAPI en `/api/v1/docs` | ✔ | — | — |

## Funcionalidades pendientes

Ver backlog priorizado en [`docs/implementation-plan.md`](docs/implementation-plan.md).
Resumen: workspaces, workbench visual, simulación, validación, laboratorio de
escenarios, formación, informes, importación de catálogo, favoritos e
historial, administración de usuarios, E2E con Playwright.

En la interfaz, los módulos no implementados aparecen como "pronto" y abren una
página que lo indica explícitamente.

## Pruebas ejecutadas

| Suite | Dónde | Resultado |
|-------|-------|-----------|
| Backend `mvn verify` (unitarias + integración con PostgreSQL 16 en Testcontainers) | GitHub Actions | 28/28 ✔ |
| Frontend Vitest + Testing Library | local y GitHub Actions | 15/15 ✔ |
| Frontend `tsc` estricto, ESLint, `vite build` | local y GitHub Actions | ✔ |
| Docker Compose: arranque, login y consulta del catálogo vía nginx | GitHub Actions | ver último run |

## Errores conocidos y limitaciones

- El bloqueo por intentos fallidos se guarda en memoria: no se comparte entre
  instancias ni sobrevive a reinicios (aceptable con una instancia; ADR-0003).
- Las sesiones son locales a la instancia; escalar horizontalmente requiere
  Spring Session JDBC.
- Bundle del frontend ~490 kB sin dividir por rutas (optimizable).
- No hay pruebas de arquitectura (ArchUnit) aún: los límites entre módulos se
  mantienen por revisión.
- Los nombres de Service Domains del catálogo demo no se han verificado contra
  una publicación oficial de BIAN (están marcados como tales).

## Decisiones pendientes (requieren al responsable del producto)

1. Fuente oficial de datos BIAN con permiso de uso.
2. Rama por defecto en GitHub (`main`) vs. rama de trabajo (`master`).
3. Paquete Java definitivo (`com.example.biansimulator`).
4. Modelo de tenencia para SaaS (workspace vs. organización).
5. Biblioteca de generación de PDF.

## Próxima tarea recomendada

Incremento 2, empezando por el modelo `workspace` / `project` /
`architecture_diagram` con migración V5, CRUD de proyectos protegido por
workspace y el editor React Flow con nodos del catálogo y nodos personalizados.
