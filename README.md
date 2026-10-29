# BIAN Enterprise Architecture Simulator

Plataforma educativa para explorar conceptos BIAN, diseñar soluciones bancarias,
simularlas y validarlas con reglas explícitas.

> **Aviso.** Proyecto independiente, sin certificación ni respaldo de BIAN. No
> declara conformidad con el estándar. El catálogo incluido es **contenido
> educativo de demostración, no oficial** (ver [ADR-0006](docs/decisions/0006-datos-demo-y-procedencia.md)).
> La aplicación no se conecta a sistemas bancarios reales.

Estado actual y próximos pasos: [`PROJECT_STATUS.md`](PROJECT_STATUS.md).

## Arquitectura en una línea

SPA React (TypeScript) servida por nginx, que proxifica `/api` hacia un
**monolito modular Spring Boot 4** sobre **PostgreSQL 16**. Detalle en
[`docs/architecture.md`](docs/architecture.md), modelo de datos en
[`docs/data-model.md`](docs/data-model.md) y decisiones en [`docs/decisions/`](docs/decisions).

```
backend/    Spring Boot 4 · Java 21 · Maven · Flyway
frontend/   React 19 · Vite · TypeScript estricto · Tailwind 4 · TanStack Query
docs/       Arquitectura, modelo de datos, plan, ADRs
```

## Inicio rápido con Docker Compose

Requisitos: Docker 24+ con Compose v2.

```bash
cp .env.example .env
# Edite .env: defina POSTGRES_PASSWORD y DEMO_USER_PASSWORD con valores propios
docker compose up --build
```

Abra <http://localhost:8081> e inicie sesión con `DEMO_USER_USERNAME` /
`DEMO_USER_PASSWORD` (por defecto el usuario es `demo`).

| Servicio  | Contenedor | Acceso                                  |
|-----------|------------|-----------------------------------------|
| frontend  | nginx      | http://localhost:8081 (`APP_PORT`)      |
| backend   | Spring Boot| solo red interna, vía `/api` en nginx   |
| db        | PostgreSQL | solo red interna                        |

Comprobaciones de salud: `docker compose ps` muestra el estado *healthy* de
cada servicio (`pg_isready`, `/actuator/health/readiness`, `/healthz`).

Detener y borrar datos: `docker compose down -v`.

## Desarrollo local sin Docker

Requisitos: Java 21, Maven 3.9+, Node.js 22.22+, PostgreSQL 16.

```bash
# 1. Base de datos
createuser -P bian            # defina una contraseña
createdb -O bian bian_simulator

# 2. Backend (perfil dev: carga el catálogo demo)
cd backend
export DB_PASSWORD='<contraseña de bian>'
export DEMO_USER_PASSWORD='<elija una>'
mvn spring-boot:run           # http://localhost:8080

# 3. Frontend (otra terminal)
cd frontend
npm install
npm run dev                   # http://localhost:5173 (proxy de /api a :8080)
```

Documentación OpenAPI: <http://localhost:8080/api/v1/docs>.

## Pruebas

```bash
# Backend: unitarias + integración con PostgreSQL real (requiere Docker para Testcontainers)
cd backend && mvn verify

# Frontend
cd frontend
npm run typecheck
npm run lint
npm test
npm run build
```

CI (GitHub Actions, `.github/workflows/ci.yml`) ejecuta las tres suites:
backend, frontend y una prueba de humo que levanta el `docker compose`
completo e inicia sesión a través de nginx.

## Variables de entorno

| Variable | Uso | Por defecto |
|----------|-----|-------------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a PostgreSQL (backend) | `jdbc:postgresql://localhost:5432/bian_simulator`, `bian`, — |
| `SPRING_PROFILES_ACTIVE` | `dev` / `demo` cargan el catálogo demo; `prod` no | `dev` |
| `DEMO_USER_USERNAME`, `DEMO_USER_PASSWORD` | Usuario demo (solo dev/demo). Sin contraseña no se crea | `demo`, — |
| `SESSION_TIMEOUT` | Caducidad de la sesión por inactividad | `30m` |
| `SESSION_COOKIE_SECURE` | Cookie de sesión solo por HTTPS | `false` (dev), `true` (prod) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos si la SPA se sirve en otro dominio | vacío (CORS cerrado) |
| `APP_PORT` | Puerto publicado por Compose | `8081` |

No suba `.env` al repositorio: está en `.gitignore`.

## Seguridad (resumen)

Sesión de servidor con cookie `HttpOnly` y CSRF *double submit*, rotación de
sesión al iniciar, bloqueo temporal tras 5 intentos fallidos, contraseñas BCrypt,
autorización por permisos y auditoría de inicio y cierre de sesión. Ver
[ADR-0003](docs/decisions/0003-autenticacion-sesion.md).
