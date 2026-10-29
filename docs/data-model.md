# Modelo de datos

> Estado: **vigente** · Última revisión: 2026-10-29

Convenciones generales:

* Claves primarias `UUID` (generadas por la aplicación, `uuid` en PostgreSQL)
  salvo catálogos cerrados pequeños (`role`, `permission`) que usan un código
  natural `varchar` como clave.
* Todas las tablas mutables tienen `created_at` y `updated_at` (`timestamptz`).
  Las tablas que registran autoría añaden `created_by` (`uuid`, FK a `app_user`).
* Enumeraciones como `varchar` + `CHECK` (más fácil de migrar que tipos `ENUM`).
* `JSONB` solo para estructuras variables que no se consultan relacionalmente
  (propiedades visuales de nodos, configuración de escenarios, trazas de
  simulación). Lo que requiere integridad o filtros se normaliza.
* Nombres en `snake_case`, singular.

## 1. Identity and Access — *implementado (incremento 1)*

```
app_user (id PK uuid, username UK, display_name, password_hash, enabled,
          created_at, updated_at)
role (code PK, name, description)
permission (code PK, description)
role_permission (role_code FK, permission_code FK, PK compuesta)
user_role (user_id FK, role_code FK, PK compuesta)
```

* `username` se guarda en minúsculas; índice único.
* `password_hash` en formato `{bcrypt}...` (DelegatingPasswordEncoder).

Roles iniciales y permisos (ver `V2__identity_seed_roles.sql`):

| Permiso            | ADMIN | ARCHITECT | DEVELOPER | LEARNER | VIEWER |
|--------------------|:-----:|:---------:|:---------:|:-------:|:------:|
| `CATALOG_READ`     | ✔ | ✔ | ✔ | ✔ | ✔ |
| `CATALOG_IMPORT`   | ✔ |   |   |   |   |
| `PROJECT_READ`     | ✔ | ✔ | ✔ | ✔ | ✔ |
| `PROJECT_WRITE`    | ✔ | ✔ | ✔ | ✔ |   |
| `SIMULATION_RUN`   | ✔ | ✔ | ✔ | ✔ |   |
| `SCENARIO_MANAGE`  | ✔ | ✔ |   |   |   |
| `ASSESSMENT_TAKE`  | ✔ | ✔ | ✔ | ✔ |   |
| `ASSESSMENT_MANAGE`| ✔ |   |   |   |   |
| `REPORT_GENERATE`  | ✔ | ✔ | ✔ |   |   |
| `USER_ADMIN`       | ✔ |   |   |   |   |
| `AUDIT_READ`       | ✔ |   |   |   |   |

## 2. Audit — *implementado (incremento 1)*

```
audit_event (id PK uuid, occurred_at, actor_user_id FK nullable, actor_username,
             action, target_type, target_id, outcome (SUCCESS|FAILURE|DENIED),
             details jsonb, client_ip)
  IX (occurred_at desc), IX (actor_user_id, occurred_at desc), IX (action)
```

Solo inserción. `details` nunca contiene credenciales.

## 3. BIAN Catalog — *implementado: lectura (incremento 1)*

BIAN no es una jerarquía rígida: el modelo usa **un único tipo de elemento con
relaciones tipadas** (ADR-0004). `ServiceDomainOperation`, `ControlRecord` y
`BusinessObject` son tipos de `catalog_element`.

```
catalog_version (id PK uuid, code UK, name, source_type, provenance_note,
                 standard_release_label nullable, status (ACTIVE|ARCHIVED),
                 imported_at, imported_by FK nullable, created_at, updated_at)

catalog_element (id PK uuid, catalog_version_id FK, external_ref, element_type,
                 name, description, functional_responsibility, category,
                 validation_status, source_reference, attributes jsonb,
                 created_at, updated_at)
  UK (catalog_version_id, external_ref)
  IX (catalog_version_id, element_type), IX (catalog_version_id, category),
  IX (lower(name))

catalog_relationship (id PK uuid, catalog_version_id FK, source_element_id FK,
                      target_element_id FK, relationship_type, description,
                      provenance, created_at)
  UK (source_element_id, target_element_id, relationship_type)
  IX (target_element_id)
```

* `source_type`: `OFFICIAL_IMPORT | EDUCATIONAL_DEMO | USER_IMPORT`.
* `element_type`: `BUSINESS_AREA | BUSINESS_DOMAIN | SERVICE_DOMAIN |
  SERVICE_OPERATION | CONTROL_RECORD | BUSINESS_OBJECT`.
* `validation_status`: `UNVERIFIED | VERIFIED | REJECTED`.
* `relationship_type`: `CONTAINS | HAS_OPERATION | HAS_CONTROL_RECORD |
  REFERENCES_BUSINESS_OBJECT | COLLABORATES_WITH`.
* `provenance` de relación: `SOURCE_DOCUMENTED | EDUCATIONAL_EXAMPLE`.
* Las versiones **nunca** se sobrescriben: una nueva importación crea una nueva
  `catalog_version`.

### Pendiente en el catálogo

```
import_job (id, catalog_version_id FK nullable, format, original_filename,
            checksum_sha256, status (PREVIEW|CONFIRMED|FAILED|DISCARDED),
            report jsonb, created_by, created_at, confirmed_at)
user_catalog_favorite (user_id FK, element_id FK, created_at, PK compuesta)
user_catalog_view (id, user_id FK, element_id FK, viewed_at)  -- historial
```

## 4. Workbench — *pendiente (incremento 2)*

```
workspace (id, name, owner_id FK, created_at, updated_at)
workspace_member (workspace_id FK, user_id FK, member_role, PK compuesta)
project (id, workspace_id FK, name, business_objective, catalog_version_id FK,
         created_by, created_at, updated_at, archived_at)
architecture_diagram (id, project_id FK, name, schema_version int,
                      viewport jsonb, settings jsonb, revision bigint (optimistic lock),
                      created_at, updated_at)
diagram_node (id, diagram_id FK, node_kind, catalog_element_id FK nullable,
              label, description, classification, position_x, position_y,
              group_node_id FK nullable, properties jsonb, created_at, updated_at)
  CHECK (node_kind <> 'CATALOG' OR catalog_element_id IS NOT NULL)
diagram_connection (id, diagram_id FK, source_node_id FK, target_node_id FK,
                    relationship_type, direction, protocol, description,
                    documentation_ref, rationale, provenance, validation_status,
                    created_at, updated_at)
project_version (id, project_id FK, version_number, label, snapshot jsonb,
                 snapshot_schema_version, created_by, created_at)
  UK (project_id, version_number)
architecture_decision (id, project_id FK, title, status, context, decision,
                       consequences, created_by, created_at, updated_at)
```

`node_kind`: `CATALOG | CHANNEL | EXTERNAL_SYSTEM | INTEGRATION_ADAPTER | API |
INFRASTRUCTURE | CUSTOM | BOUNDARY`. Las instantáneas de `project_version`
usan el esquema JSON versionado del proyecto (`schemaVersion`), que también es
el formato de exportación/importación.

## 5. Simulation & Scenarios — *pendiente*

```
simulation_scenario (id, code UK, title, description, objective, business_context,
                     difficulty, definition jsonb, reference_solution jsonb,
                     provenance, created_by, created_at, updated_at)
scenario_requirement (id, scenario_id FK, kind (FUNCTIONAL|NON_FUNCTIONAL|
                      CONSTRAINT|ACCEPTANCE), code, text, sort_order)
simulation_execution (id, scenario_id FK, diagram_id FK, diagram_revision,
                      config jsonb, seed bigint, status, outcome,
                      metrics jsonb, started_by, started_at, finished_at)
simulation_event (id, execution_id FK, sequence int, simulated_time_ms,
                  event_type, node_id, connection_id, payload jsonb)
  UK (execution_id, sequence)
```

## 6. Validation — *pendiente*

```
validation_rule (code PK, name, description, severity, kind (OBJECTIVE|HEURISTIC),
                 enabled_by_default, reference_url, created_at, updated_at)
validation_run (id, diagram_id FK, diagram_revision, scenario_id FK nullable,
                executed_by, executed_at)
validation_finding (id, run_id FK, rule_code FK, severity, message,
                    recommendation, evidence jsonb, node_id, connection_id)
```

## 7. Assessment — *pendiente*

```
learning_question (id, topic, difficulty, stem, options jsonb, correct_option,
                   explanation, source_reference, provenance, validation_status,
                   reviewed_at, created_at, updated_at)
assessment (id, title, mode (PRACTICE|MOCK_EXAM), question_count,
            time_limit_seconds, passing_score, topics jsonb, created_at)
assessment_attempt (id, assessment_id FK, user_id FK, started_at, submitted_at,
                    score, passed, question_ids jsonb)
assessment_answer (attempt_id FK, question_id FK, selected_option, correct,
                   answered_at, PK compuesta)
learning_progress (user_id FK, topic, attempts, correct, last_attempt_at,
                   PK compuesta)
```

## 8. Reporting — *pendiente*

```
report (id, project_id FK, project_version_id FK nullable, format,
        content_location / content bytea, generated_by, generated_at,
        sections jsonb)
```

## 9. Estrategia de versionado

* **Esquema de BD**: Flyway, migraciones `V<n>__descripcion.sql` inmutables.
  Datos demo en `db/demo` solo para perfiles `dev`/`demo`.
* **Catálogo**: inmutable por versión (`catalog_version`).
* **Diagramas**: `revision` con bloqueo optimista para guardado automático;
  `project_version` para instantáneas con nombre.
* **JSON de proyecto**: campo `schemaVersion` con migradores en el backend.
