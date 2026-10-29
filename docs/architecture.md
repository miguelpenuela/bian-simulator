# Arquitectura — BIAN Enterprise Architecture Simulator

> Estado: **vigente** · Última revisión: 2026-10-29 · Incremento 1 completado

## 1. Contexto

Plataforma educativa y de diseño para explorar el modelo BIAN, componer
soluciones bancarias, simularlas, validarlas con reglas explícitas y practicar
para evaluaciones de conocimiento. **No** se conecta a sistemas bancarios reales
y **no** declara conformidad ni certificación oficial BIAN.

### Principio rector: procedencia explícita

Toda pieza de información mostrada lleva una procedencia (`provenance`) de este
conjunto cerrado:

| Código                | Significado                                              |
|-----------------------|----------------------------------------------------------|
| `OFFICIAL`            | Información oficial de BIAN verificada                   |
| `OFFICIAL_IMPORT`     | Importada de una fuente oficial (pendiente/verificada)   |
| `EDUCATIONAL_EXAMPLE` | Ejemplo educativo creado para la plataforma              |
| `SIMULATION_ASSUMPTION` | Supuesto de una simulación                            |
| `USER_DEFINED`        | Propuesto por el usuario                                 |
| `HEURISTIC`           | Recomendación generada por reglas heurísticas o IA       |

El catálogo incluido en el repositorio es **`EDUCATIONAL_EXAMPLE`** y todos sus
elementos tienen `validation_status = UNVERIFIED`. La interfaz lo muestra con un
aviso permanente de "modo demostración".

## 2. Vista de contenedores

```
┌──────────────┐  HTTPS (mismo origen)  ┌─────────────────────────────┐   JDBC   ┌────────────┐
│  Navegador   │ ─────────────────────► │ frontend (nginx)            │          │            │
│  React SPA   │                        │  /        → SPA estática    │          │ PostgreSQL │
└──────────────┘                        │  /api/*   → proxy backend   │          │    16      │
                                        └──────────────┬──────────────┘          │            │
                                                       ▼                         │            │
                                        ┌─────────────────────────────┐ ───────► │            │
                                        │ backend: Spring Boot 4      │          └────────────┘
                                        │ monolito modular            │
                                        └─────────────────────────────┘
```

* El SPA y la API se sirven desde el **mismo origen** (nginx en producción,
  proxy de Vite en desarrollo). Esto permite cookies de sesión `HttpOnly` +
  `SameSite=Lax` sin abrir CORS (ver ADR-0003).
* PostgreSQL es la única fuente de verdad; las migraciones las gestiona Flyway.

## 3. Backend: monolito modular

Paquete raíz `com.example.biansimulator`. Cada módulo es un paquete de primer
nivel con subpaquetes `domain`, `application`, `infrastructure`, `api` cuando
aplica.

| Módulo        | Responsabilidad                                         | Estado        |
|---------------|---------------------------------------------------------|---------------|
| `identity`    | Usuarios, roles, permisos, autenticación por sesión      | Incremento 1  |
| `catalog`     | Versiones de catálogo, elementos, relaciones, búsqueda   | Incremento 1 (lectura) |
| `audit`       | Registro de eventos relevantes (login, importaciones…)   | Incremento 1  |
| `shared`      | Errores (RFC 9457), paginación, configuración transversal| Incremento 1  |
| `workbench`   | Proyectos, diagramas, versiones, workspaces             | Pendiente     |
| `simulation`  | Motor de simulación determinista                        | Pendiente     |
| `scenarios`   | Laboratorio de escenarios                               | Pendiente     |
| `assessment`  | Banco de preguntas, intentos, progreso                  | Pendiente     |
| `reporting`   | Generación de informes                                  | Pendiente     |

### Reglas de dependencia entre módulos

1. `api` → `application` → `domain`. `infrastructure` implementa puertos de
   `application`/`domain`.
2. Un módulo solo usa de otro módulo sus **servicios de aplicación públicos**
   (por ejemplo, `AuditService`), nunca sus repositorios ni entidades.
3. `shared` no depende de ningún módulo.
4. Los controladores exponen **DTOs** (`record`), nunca entidades JPA.

Estas reglas se verificarán con pruebas de arquitectura (ArchUnit) en la
fase de endurecimiento; hoy se garantizan por revisión.

### Decisión pragmática sobre JPA

Las entidades de dominio llevan anotaciones JPA (ADR-0005). Separar un modelo de
persistencia duplicaría el código sin beneficio en esta etapa. El dominio que
requiere lógica pura (motor de simulación, reglas de validación) se modelará
como objetos sin JPA.

## 4. Seguridad (resumen)

* Autenticación por **sesión de servidor** (`JSESSIONID` HttpOnly) mediante
  `POST /api/v1/auth/login` con JSON. Ver ADR-0003.
* CSRF activo con patrón *double submit* (`XSRF-TOKEN` cookie + cabecera
  `X-XSRF-TOKEN`).
* Rotación del identificador de sesión al autenticarse (fijación de sesión).
* Contraseñas con BCrypt (`DelegatingPasswordEncoder`).
* Bloqueo temporal tras intentos fallidos repetidos (por usuario + IP).
* Autorización por **permisos** (`CATALOG_READ`, `CATALOG_IMPORT`, …) asignados
  a roles (`ADMINISTRATOR`, `ARCHITECT`, `DEVELOPER`, `LEARNER`, `VIEWER`).
  La matriz vive en la base de datos y en `Permission` (enum) como contrato.
* Errores sin trazas internas; logs sin credenciales ni identificadores de sesión.
* Sin secretos en el repositorio: el usuario demo solo se crea si se define
  `DEMO_USER_PASSWORD`.

## 5. Frontend

React 19 + TypeScript estricto + Vite. Estructura por funcionalidades:

```
src/
  app/            providers, router, layout
  components/     componentes UI reutilizables y accesibles
  features/
    authentication/   login, sesión, guardas de ruta
    dashboard/
    bian-catalog/     explorador y ficha de elementos
    (resto de features como páginas "no implementado aún")
  services/       cliente HTTP (CSRF, errores RFC 9457)
  types/          contratos de la API
  utils/
  tests/          setup de pruebas
```

* **TanStack Query** para estado del servidor; **Zustand** solo cuando exista
  estado de cliente compartido (el workbench).
* **React Hook Form + Zod** para formularios.
* **Tailwind CSS 4** con tokens propios.

## 6. Observabilidad

* Actuator: `/actuator/health` (liveness/readiness) expuesto; el resto cerrado.
* Perfil `prod`: logs estructurados JSON (ECS) en consola.
* `X-Request-Id` propagado y añadido al MDC (pendiente: incremento 2).

## 7. Versiones elegidas

| Componente        | Versión         |
|-------------------|-----------------|
| Java              | 21 (LTS)        |
| Spring Boot       | 4.0.9           |
| springdoc-openapi | 3.0.x           |
| PostgreSQL        | 16              |
| Flyway            | gestionado por Spring Boot |
| Testcontainers    | gestionado por Spring Boot (2.x) |
| Node.js           | 22 LTS          |
| React             | 19              |
| Vite              | 7               |
| Tailwind CSS      | 4               |

Las versiones exactas del frontend quedan fijadas en `frontend/package-lock.json`.

## 8. Riesgos identificados

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Presentar contenido sintético como oficial BIAN | Alto (credibilidad, propiedad intelectual) | Procedencia obligatoria en el modelo; aviso de modo demo; nada se marca `OFFICIAL` sin importación verificada |
| Licencia de datos oficiales BIAN | Alto | Solo importación de archivos que el usuario aporte con permiso; sin scraping |
| Complejidad del editor visual | Medio | React Flow; esquema JSON versionado; incrementos pequeños |
| Motor de simulación no determinista | Medio | Reloj simulado y semilla explícita; límites de pasos y tiempo |
| Alcance total muy amplio | Alto | Incrementos verticales; `PROJECT_STATUS.md` como fuente de verdad del avance |
