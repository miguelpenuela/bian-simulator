package com.example.biansimulator.identity.domain;

/**
 * Contrato central de permisos. Debe coincidir con la tabla {@code permission}
 * (migración V2). Los controladores autorizan por permiso, no por rol.
 */
public enum Permission {
    CATALOG_READ,
    CATALOG_IMPORT,
    PROJECT_READ,
    PROJECT_WRITE,
    SIMULATION_RUN,
    SCENARIO_MANAGE,
    ASSESSMENT_TAKE,
    ASSESSMENT_MANAGE,
    REPORT_GENERATE,
    USER_ADMIN,
    AUDIT_READ
}
