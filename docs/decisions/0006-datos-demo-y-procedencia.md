# ADR-0006: Datos de demostración y procedencia

* Estado: Aceptada · Fecha: 2026-10-29

## Contexto
No se dispone de un conjunto oficial de datos BIAN con permiso de uso. El
producto debe funcionar igualmente sin presentar contenido sintético como oficial.

## Decisión
* El catálogo incluido (`DEMO-EDU-1`) es un **ejemplo educativo**: los nombres
  de Service Domains se usan como referencia didáctica y **no** se verificaron
  contra una versión oficial; las descripciones, operaciones y relaciones son de
  redacción propia.
* Se carga desde `db/demo` (Flyway), solo con los perfiles `dev` y `demo`.
* La interfaz muestra un aviso permanente de modo demostración en el catálogo.
* El usuario demo solo se crea si se define `DEMO_USER_PASSWORD`.

## Consecuencias
Cuando exista una fuente oficial, se importará como nueva `catalog_version`
con `source_type = OFFICIAL_IMPORT`, sin modificar el catálogo demo.
