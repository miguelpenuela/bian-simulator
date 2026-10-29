-- BIAN Catalog: grafo de elementos tipados, versionado e inmutable por versión (ADR-0004)
CREATE TABLE catalog_version (
    id                     uuid         PRIMARY KEY,
    code                   varchar(40)  NOT NULL,
    name                   varchar(200) NOT NULL,
    source_type            varchar(20)  NOT NULL,
    provenance_note        text         NOT NULL,
    standard_release_label varchar(40),
    status                 varchar(10)  NOT NULL,
    imported_at            timestamptz  NOT NULL,
    imported_by            uuid         REFERENCES app_user (id) ON DELETE SET NULL,
    created_at             timestamptz  NOT NULL,
    updated_at             timestamptz  NOT NULL,
    CONSTRAINT uk_catalog_version_code UNIQUE (code),
    CONSTRAINT ck_catalog_version_source CHECK (source_type IN ('OFFICIAL_IMPORT', 'EDUCATIONAL_DEMO', 'USER_IMPORT')),
    CONSTRAINT ck_catalog_version_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE TABLE catalog_element (
    id                        uuid         PRIMARY KEY,
    catalog_version_id        uuid         NOT NULL REFERENCES catalog_version (id) ON DELETE CASCADE,
    external_ref              varchar(100) NOT NULL,
    element_type              varchar(30)  NOT NULL,
    name                      varchar(200) NOT NULL,
    description               text,
    functional_responsibility text,
    category                  varchar(120),
    validation_status         varchar(12)  NOT NULL,
    source_reference          varchar(500),
    attributes                jsonb,
    created_at                timestamptz  NOT NULL,
    updated_at                timestamptz  NOT NULL,
    CONSTRAINT uk_catalog_element_ref UNIQUE (catalog_version_id, external_ref),
    CONSTRAINT ck_catalog_element_type CHECK (element_type IN
        ('BUSINESS_AREA', 'BUSINESS_DOMAIN', 'SERVICE_DOMAIN', 'SERVICE_OPERATION', 'CONTROL_RECORD', 'BUSINESS_OBJECT')),
    CONSTRAINT ck_catalog_element_validation CHECK (validation_status IN ('UNVERIFIED', 'VERIFIED', 'REJECTED'))
);

CREATE INDEX ix_catalog_element_type ON catalog_element (catalog_version_id, element_type);
CREATE INDEX ix_catalog_element_category ON catalog_element (catalog_version_id, category);
CREATE INDEX ix_catalog_element_name ON catalog_element (lower(name));

CREATE TABLE catalog_relationship (
    id                 uuid        PRIMARY KEY,
    catalog_version_id uuid        NOT NULL REFERENCES catalog_version (id) ON DELETE CASCADE,
    source_element_id  uuid        NOT NULL REFERENCES catalog_element (id) ON DELETE CASCADE,
    target_element_id  uuid        NOT NULL REFERENCES catalog_element (id) ON DELETE CASCADE,
    relationship_type  varchar(40) NOT NULL,
    description        text,
    provenance         varchar(30) NOT NULL,
    created_at         timestamptz NOT NULL,
    CONSTRAINT uk_catalog_relationship UNIQUE (source_element_id, target_element_id, relationship_type),
    CONSTRAINT ck_catalog_relationship_self CHECK (source_element_id <> target_element_id),
    CONSTRAINT ck_catalog_relationship_type CHECK (relationship_type IN
        ('CONTAINS', 'HAS_OPERATION', 'HAS_CONTROL_RECORD', 'REFERENCES_BUSINESS_OBJECT', 'COLLABORATES_WITH')),
    CONSTRAINT ck_catalog_relationship_provenance CHECK (provenance IN ('SOURCE_DOCUMENTED', 'EDUCATIONAL_EXAMPLE'))
);

CREATE INDEX ix_catalog_relationship_source ON catalog_relationship (source_element_id);
CREATE INDEX ix_catalog_relationship_target ON catalog_relationship (target_element_id);
