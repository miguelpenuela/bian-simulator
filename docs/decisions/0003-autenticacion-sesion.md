# ADR-0003: Autenticación por sesión de servidor con CSRF

* Estado: Aceptada · Fecha: 2026-10-29

## Contexto
El cliente es un SPA servido desde el mismo origen que la API. Los requisitos
exigen expiración, renovación, revocación y almacenamiento seguro de credenciales.

## Opciones
1. JWT de acceso + refresh token: requiere almacenamiento seguro en el cliente,
   lista de revocación y rotación de refresh tokens; más superficie de error.
2. **Sesión de servidor** (`JSESSIONID` HttpOnly, SameSite=Lax) + CSRF.

## Decisión
Opción 2.
* `POST /api/v1/auth/login` (JSON) autentica con `AuthenticationManager`,
  rota el id de sesión y guarda el `SecurityContext` en la sesión.
* Expiración: timeout de sesión configurable (`SESSION_TIMEOUT`, 30 min por defecto).
* Renovación: deslizante, cada petición autenticada extiende la sesión.
* Revocación: `POST /api/v1/auth/logout` invalida la sesión; deshabilitar un
  usuario impide nuevos inicios de sesión.
* CSRF: cookie `XSRF-TOKEN` legible por JS + cabecera `X-XSRF-TOKEN`.
* Bloqueo temporal tras 5 intentos fallidos en 15 minutos por usuario+IP.

## Consecuencias
* (+) Ningún token accesible desde JavaScript; revocación inmediata.
* (−) Escalar horizontalmente exigirá sesiones compartidas (Spring Session JDBC
  sobre la misma PostgreSQL). Registrado como trabajo futuro.
* (−) Clientes no-navegador (CLI, integraciones) necesitarán otro mecanismo
  (tokens de API) cuando existan.
