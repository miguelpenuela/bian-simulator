# ADR-0004: Catálogo como grafo de elementos tipados y versionados

* Estado: Aceptada · Fecha: 2026-10-29

## Contexto
BIAN no es una jerarquía rígida: Business Areas, Business Domains, Service
Domains, operaciones, Control Records y Business Objects se relacionan de formas
que cambian entre versiones y fuentes.

## Decisión
* Una tabla `catalog_element` con `element_type` y una tabla
  `catalog_relationship` con `relationship_type` y `provenance`.
* Cada importación crea una `catalog_version` nueva e inmutable con
  `source_type` y nota de procedencia. Nada se sobrescribe.
* Cada elemento lleva `validation_status`; el catálogo de demostración es
  `EDUCATIONAL_DEMO` con todos los elementos `UNVERIFIED`.

## Consecuencias
* (+) Admite cualquier forma de grafo e importaciones de fuentes distintas.
* (+) Los diagramas referencian una versión concreta del catálogo.
* (−) Las consultas "jerárquicas" requieren recorrer relaciones (aceptable: el
  catálogo completo de BIAN tiene del orden de cientos de Service Domains).
