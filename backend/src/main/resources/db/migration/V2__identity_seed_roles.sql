-- Roles y permisos iniciales (contrato: com.example.biansimulator.identity.domain.Permission)
INSERT INTO role (code, name, description) VALUES
 ('ADMINISTRATOR', 'Administrator', 'Administración completa de la plataforma'),
 ('ARCHITECT',     'Architect',     'Diseña soluciones, gestiona escenarios y genera informes'),
 ('DEVELOPER',     'Developer',     'Diseña y simula soluciones'),
 ('LEARNER',       'Learner',       'Estudia, practica evaluaciones y resuelve escenarios'),
 ('VIEWER',        'Viewer',        'Acceso de solo lectura');

INSERT INTO permission (code, description) VALUES
 ('CATALOG_READ',      'Consultar el catálogo'),
 ('CATALOG_IMPORT',    'Importar versiones de catálogo'),
 ('PROJECT_READ',      'Consultar proyectos y diagramas'),
 ('PROJECT_WRITE',     'Crear y modificar proyectos y diagramas'),
 ('SIMULATION_RUN',    'Ejecutar simulaciones'),
 ('SCENARIO_MANAGE',   'Crear y editar escenarios'),
 ('ASSESSMENT_TAKE',   'Realizar cuestionarios y exámenes de práctica'),
 ('ASSESSMENT_MANAGE', 'Gestionar el banco de preguntas'),
 ('REPORT_GENERATE',   'Generar informes'),
 ('USER_ADMIN',        'Administrar usuarios y roles'),
 ('AUDIT_READ',        'Consultar la auditoría');

INSERT INTO role_permission (role_code, permission_code)
SELECT 'ADMINISTRATOR', code FROM permission;

INSERT INTO role_permission (role_code, permission_code) VALUES
 ('ARCHITECT', 'CATALOG_READ'), ('ARCHITECT', 'PROJECT_READ'), ('ARCHITECT', 'PROJECT_WRITE'),
 ('ARCHITECT', 'SIMULATION_RUN'), ('ARCHITECT', 'SCENARIO_MANAGE'), ('ARCHITECT', 'ASSESSMENT_TAKE'),
 ('ARCHITECT', 'REPORT_GENERATE'),
 ('DEVELOPER', 'CATALOG_READ'), ('DEVELOPER', 'PROJECT_READ'), ('DEVELOPER', 'PROJECT_WRITE'),
 ('DEVELOPER', 'SIMULATION_RUN'), ('DEVELOPER', 'ASSESSMENT_TAKE'), ('DEVELOPER', 'REPORT_GENERATE'),
 ('LEARNER', 'CATALOG_READ'), ('LEARNER', 'PROJECT_READ'), ('LEARNER', 'PROJECT_WRITE'),
 ('LEARNER', 'SIMULATION_RUN'), ('LEARNER', 'ASSESSMENT_TAKE'),
 ('VIEWER', 'CATALOG_READ'), ('VIEWER', 'PROJECT_READ');
