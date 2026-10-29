-- =====================================================================================
-- CATÁLOGO DE DEMOSTRACIÓN — CONTENIDO EDUCATIVO, NO OFICIAL (ADR-0006)
--
-- * Los nombres de Service Domains se usan como referencia didáctica y NO han sido
--   verificados contra una versión oficial publicada por BIAN.
-- * Descripciones, responsabilidades, operaciones, Control Records, Business Objects,
--   agrupaciones y relaciones son de redacción propia con fines educativos.
-- * Todos los elementos quedan con validation_status = 'UNVERIFIED'.
-- Solo se carga con los perfiles 'dev' y 'demo'. Idempotente (UUID deterministas).
-- =====================================================================================

CREATE TEMP TABLE demo_el (
    ref  varchar(100), type varchar(30), name varchar(200), category varchar(120),
    description text, responsibility text
) ON COMMIT DROP;

CREATE TEMP TABLE demo_rel (
    src varchar(100), tgt varchar(100), type varchar(40), description text
) ON COMMIT DROP;

INSERT INTO catalog_version (id, code, name, source_type, provenance_note, standard_release_label,
                             status, imported_at, imported_by, created_at, updated_at)
VALUES (md5('DEMO-EDU-1')::uuid, 'DEMO-EDU-1', 'Catálogo educativo de demostración',
        'EDUCATIONAL_DEMO',
        'Conjunto educativo creado para la plataforma. No es una fuente oficial de BIAN: nombres de referencia sin verificar; descripciones y relaciones de redacción propia.',
        NULL, 'ACTIVE', now(), NULL, now(), now())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, provenance_note = EXCLUDED.provenance_note, updated_at = now();

-- ---------------------------------------------------------------- agrupaciones (demo)
INSERT INTO demo_el VALUES
 ('BA-SALES-SERVICE', 'BUSINESS_AREA', 'Ventas y servicio (agrupación demo)', 'Ventas y servicio',
  'Agrupación educativa de capacidades orientadas al contacto con el cliente: canales, ofertas y relación.', NULL),
 ('BA-OPERATIONS', 'BUSINESS_AREA', 'Operaciones y ejecución (agrupación demo)', 'Operaciones y ejecución',
  'Agrupación educativa de capacidades que mantienen y ejecutan productos: cuentas, pagos, tarjetas y préstamos.', NULL),
 ('BA-RISK-REFERENCE', 'BUSINESS_AREA', 'Riesgo y datos de referencia (agrupación demo)', 'Riesgo y referencia',
  'Agrupación educativa de capacidades de evaluación de riesgo y datos maestros.', NULL),

 ('BD-CHANNELS', 'BUSINESS_DOMAIN', 'Canales (agrupación demo)', 'Ventas y servicio',
  'Capacidades para gestionar sesiones e interacción en canales digitales y presenciales.', NULL),
 ('BD-CUSTOMER', 'BUSINESS_DOMAIN', 'Cliente y oferta (agrupación demo)', 'Ventas y servicio',
  'Capacidades para conocer al cliente, formalizar acuerdos y gestionar ofertas.', NULL),
 ('BD-ACCOUNTS', 'BUSINESS_DOMAIN', 'Cuentas y depósitos (agrupación demo)', 'Operaciones y ejecución',
  'Capacidades para administrar cuentas transaccionales y de ahorro.', NULL),
 ('BD-PAYMENTS', 'BUSINESS_DOMAIN', 'Pagos (agrupación demo)', 'Operaciones y ejecución',
  'Capacidades para recibir, decidir y ejecutar órdenes de pago.', NULL),
 ('BD-CARDS', 'BUSINESS_DOMAIN', 'Tarjetas (agrupación demo)', 'Operaciones y ejecución',
  'Capacidades para administrar tarjetas y autorizar sus transacciones.', NULL),
 ('BD-LENDING', 'BUSINESS_DOMAIN', 'Préstamos (agrupación demo)', 'Operaciones y ejecución',
  'Capacidades para originar y administrar productos de crédito.', NULL),
 ('BD-RISK', 'BUSINESS_DOMAIN', 'Evaluación de riesgo (agrupación demo)', 'Riesgo y referencia',
  'Capacidades de evaluación crediticia y prevención de fraude.', NULL),
 ('BD-REFERENCE', 'BUSINESS_DOMAIN', 'Datos de referencia (agrupación demo)', 'Riesgo y referencia',
  'Capacidades que mantienen datos maestros de partes y productos.', NULL);

-- ---------------------------------------------------------------- Service Domains (nombres de referencia, sin verificar)
INSERT INTO demo_el VALUES
 ('SD-SESSION-DIALOGUE', 'SERVICE_DOMAIN', 'Session Dialogue', 'Canales',
  'Mantiene el contexto de una interacción del cliente en un canal durante su duración.',
  'Abrir, mantener y cerrar el diálogo de una sesión de canal; no ejecuta lógica de producto.'),
 ('SD-PARTY-AUTHENTICATION', 'SERVICE_DOMAIN', 'Party Authentication', 'Canales',
  'Verifica la identidad de una parte que accede a servicios del banco.',
  'Evaluar credenciales y factores de autenticación y devolver un resultado de verificación.'),
 ('SD-PARTY-REFERENCE-DATA-DIRECTORY', 'SERVICE_DOMAIN', 'Party Reference Data Directory', 'Datos de referencia',
  'Directorio de datos de referencia de las partes (personas y organizaciones) con las que se relaciona el banco.',
  'Ser la fuente de los datos de identificación y contacto de una parte.'),
 ('SD-CUSTOMER-AGREEMENT', 'SERVICE_DOMAIN', 'Customer Agreement', 'Cliente y oferta',
  'Gestiona el acuerdo marco entre el banco y un cliente.',
  'Registrar y mantener los términos generales que rigen la relación con el cliente.'),
 ('SD-CUSTOMER-OFFER', 'SERVICE_DOMAIN', 'Customer Offer', 'Cliente y oferta',
  'Coordina el proceso de ofrecer un producto a un cliente hasta su aceptación o rechazo.',
  'Orquestar los pasos de una oferta (verificaciones, decisiones, aceptación) sin mantener el producto resultante.'),
 ('SD-PRODUCT-DIRECTORY', 'SERVICE_DOMAIN', 'Product Directory', 'Datos de referencia',
  'Catálogo de productos y servicios que ofrece el banco.',
  'Mantener la definición de los productos disponibles y sus características.'),
 ('SD-CURRENT-ACCOUNT', 'SERVICE_DOMAIN', 'Current Account', 'Cuentas y depósitos',
  'Administra una cuenta corriente/transaccional durante su ciclo de vida.',
  'Aplicar los términos de la cuenta, registrar movimientos y exponer su estado y saldo.'),
 ('SD-SAVINGS-ACCOUNT', 'SERVICE_DOMAIN', 'Savings Account', 'Cuentas y depósitos',
  'Administra una cuenta de ahorro durante su ciclo de vida.',
  'Aplicar los términos de la cuenta de ahorro, intereses y restricciones de retiro.'),
 ('SD-POSITION-KEEPING', 'SERVICE_DOMAIN', 'Position Keeping', 'Cuentas y depósitos',
  'Lleva el registro de las posiciones financieras derivadas de transacciones.',
  'Mantener el libro de movimientos y posiciones; no decide si una transacción procede.'),
 ('SD-PAYMENT-ORDER', 'SERVICE_DOMAIN', 'Payment Order', 'Pagos',
  'Recibe y procesa una solicitud de pago hasta decidir cómo ejecutarla.',
  'Validar la orden, aplicar verificaciones y decidir su ejecución; no realiza el movimiento de fondos.'),
 ('SD-PAYMENT-EXECUTION', 'SERVICE_DOMAIN', 'Payment Execution', 'Pagos',
  'Ejecuta la transferencia de fondos de una orden de pago aprobada.',
  'Realizar el movimiento de fondos por el mecanismo elegido y confirmar el resultado.'),
 ('SD-CREDIT-CARD', 'SERVICE_DOMAIN', 'Credit Card', 'Tarjetas',
  'Administra la facilidad de crédito asociada a una tarjeta.',
  'Mantener límites, saldos y términos de la tarjeta de crédito.'),
 ('SD-CARD-AUTHORIZATION', 'SERVICE_DOMAIN', 'Card Authorization', 'Tarjetas',
  'Decide si una transacción con tarjeta se autoriza.',
  'Evaluar la transacción frente a límites, estado y reglas de riesgo y emitir la decisión.'),
 ('SD-ISSUED-DEVICE-ADMINISTRATION', 'SERVICE_DOMAIN', 'Issued Device Administration', 'Tarjetas',
  'Administra los dispositivos emitidos al cliente (por ejemplo, tarjetas físicas).',
  'Gestionar emisión, bloqueo, reposición y estado del dispositivo.'),
 ('SD-CONSUMER-LOAN', 'SERVICE_DOMAIN', 'Consumer Loan', 'Préstamos',
  'Administra un préstamo de consumo durante su ciclo de vida.',
  'Mantener el plan de pagos, saldos e intereses del préstamo.'),
 ('SD-CUSTOMER-CREDIT-RATING', 'SERVICE_DOMAIN', 'Customer Credit Rating', 'Evaluación de riesgo',
  'Mantiene y calcula la calificación crediticia del cliente.',
  'Proveer una evaluación de solvencia a partir de información interna y externa.'),
 ('SD-FRAUD-EVALUATION', 'SERVICE_DOMAIN', 'Fraud Evaluation', 'Evaluación de riesgo',
  'Evalúa transacciones o solicitudes en busca de indicios de fraude.',
  'Analizar señales de riesgo y devolver una puntuación o recomendación.'),
 ('SD-CUSTOMER-EVENT-HISTORY', 'SERVICE_DOMAIN', 'Customer Event History', 'Cliente y oferta',
  'Conserva el historial de eventos relevantes de la relación con el cliente.',
  'Registrar eventos y permitir su consulta; no interpreta ni decide.');

-- ---------------------------------------------------------------- Business Objects (ejemplos educativos)
INSERT INTO demo_el VALUES
 ('BO-PARTY', 'BUSINESS_OBJECT', 'Parte (objeto demo)', NULL, 'Persona u organización con la que se relaciona el banco.', NULL),
 ('BO-ACCOUNT', 'BUSINESS_OBJECT', 'Cuenta (objeto demo)', NULL, 'Registro de un producto de depósito y sus condiciones.', NULL),
 ('BO-PAYMENT-INSTRUCTION', 'BUSINESS_OBJECT', 'Instrucción de pago (objeto demo)', NULL, 'Datos de ordenante, beneficiario, importe y fecha de un pago.', NULL),
 ('BO-TRANSACTION', 'BUSINESS_OBJECT', 'Transacción (objeto demo)', NULL, 'Movimiento financiero registrado contra una posición.', NULL),
 ('BO-CARD', 'BUSINESS_OBJECT', 'Tarjeta (objeto demo)', NULL, 'Dispositivo de pago emitido y su estado.', NULL),
 ('BO-LOAN', 'BUSINESS_OBJECT', 'Préstamo (objeto demo)', NULL, 'Facilidad de crédito con plan de pagos.', NULL),
 ('BO-CREDIT-ASSESSMENT', 'BUSINESS_OBJECT', 'Evaluación crediticia (objeto demo)', NULL, 'Resultado de valorar la solvencia de una parte.', NULL),
 ('BO-AGREEMENT', 'BUSINESS_OBJECT', 'Acuerdo (objeto demo)', NULL, 'Términos y condiciones pactados con el cliente.', NULL),
 ('BO-PRODUCT', 'BUSINESS_OBJECT', 'Producto (objeto demo)', NULL, 'Definición de un producto o servicio ofrecido.', NULL);

-- ---------------------------------------------------------------- Operaciones y Control Records (generados por SD)
-- Operaciones de ejemplo con términos de acción genéricos; NO son el inventario oficial de cada SD.
INSERT INTO demo_el
SELECT sd.ref || '-OP-' || upper(op.action), 'SERVICE_OPERATION', sd.name || ' · ' || op.action, sd.category,
       op.text || ' (operación de ejemplo, no verificada).', NULL
FROM demo_el sd
CROSS JOIN (VALUES ('Initiate', 'Inicia una nueva instancia gestionada por el dominio'),
                   ('Update', 'Modifica datos de una instancia existente'),
                   ('Retrieve', 'Consulta el estado o los datos de una instancia'),
                   ('Execute', 'Ejecuta una acción concreta sobre una instancia')) AS op(action, text)
WHERE sd.type = 'SERVICE_DOMAIN';

INSERT INTO demo_el
SELECT sd.ref || '-CR', 'CONTROL_RECORD', 'Registro de control de ' || sd.name || ' (demo)', sd.category,
       'Registro educativo que representa la instancia que gestiona el dominio durante su ciclo de vida.', NULL
FROM demo_el sd WHERE sd.type = 'SERVICE_DOMAIN';

-- ---------------------------------------------------------------- relaciones educativas
INSERT INTO demo_rel VALUES
 ('BA-SALES-SERVICE', 'BD-CHANNELS', 'CONTAINS', NULL),
 ('BA-SALES-SERVICE', 'BD-CUSTOMER', 'CONTAINS', NULL),
 ('BA-OPERATIONS', 'BD-ACCOUNTS', 'CONTAINS', NULL),
 ('BA-OPERATIONS', 'BD-PAYMENTS', 'CONTAINS', NULL),
 ('BA-OPERATIONS', 'BD-CARDS', 'CONTAINS', NULL),
 ('BA-OPERATIONS', 'BD-LENDING', 'CONTAINS', NULL),
 ('BA-RISK-REFERENCE', 'BD-RISK', 'CONTAINS', NULL),
 ('BA-RISK-REFERENCE', 'BD-REFERENCE', 'CONTAINS', NULL),
 ('BD-CHANNELS', 'SD-SESSION-DIALOGUE', 'CONTAINS', NULL),
 ('BD-CHANNELS', 'SD-PARTY-AUTHENTICATION', 'CONTAINS', NULL),
 ('BD-CUSTOMER', 'SD-CUSTOMER-AGREEMENT', 'CONTAINS', NULL),
 ('BD-CUSTOMER', 'SD-CUSTOMER-OFFER', 'CONTAINS', NULL),
 ('BD-CUSTOMER', 'SD-CUSTOMER-EVENT-HISTORY', 'CONTAINS', NULL),
 ('BD-REFERENCE', 'SD-PARTY-REFERENCE-DATA-DIRECTORY', 'CONTAINS', NULL),
 ('BD-REFERENCE', 'SD-PRODUCT-DIRECTORY', 'CONTAINS', NULL),
 ('BD-ACCOUNTS', 'SD-CURRENT-ACCOUNT', 'CONTAINS', NULL),
 ('BD-ACCOUNTS', 'SD-SAVINGS-ACCOUNT', 'CONTAINS', NULL),
 ('BD-ACCOUNTS', 'SD-POSITION-KEEPING', 'CONTAINS', NULL),
 ('BD-PAYMENTS', 'SD-PAYMENT-ORDER', 'CONTAINS', NULL),
 ('BD-PAYMENTS', 'SD-PAYMENT-EXECUTION', 'CONTAINS', NULL),
 ('BD-CARDS', 'SD-CREDIT-CARD', 'CONTAINS', NULL),
 ('BD-CARDS', 'SD-CARD-AUTHORIZATION', 'CONTAINS', NULL),
 ('BD-CARDS', 'SD-ISSUED-DEVICE-ADMINISTRATION', 'CONTAINS', NULL),
 ('BD-LENDING', 'SD-CONSUMER-LOAN', 'CONTAINS', NULL),
 ('BD-RISK', 'SD-CUSTOMER-CREDIT-RATING', 'CONTAINS', NULL),
 ('BD-RISK', 'SD-FRAUD-EVALUATION', 'CONTAINS', NULL),

 ('SD-PARTY-REFERENCE-DATA-DIRECTORY', 'BO-PARTY', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-PARTY-AUTHENTICATION', 'BO-PARTY', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-CURRENT-ACCOUNT', 'BO-ACCOUNT', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-SAVINGS-ACCOUNT', 'BO-ACCOUNT', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-POSITION-KEEPING', 'BO-TRANSACTION', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-PAYMENT-ORDER', 'BO-PAYMENT-INSTRUCTION', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-PAYMENT-EXECUTION', 'BO-PAYMENT-INSTRUCTION', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-PAYMENT-EXECUTION', 'BO-TRANSACTION', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-CREDIT-CARD', 'BO-CARD', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-CARD-AUTHORIZATION', 'BO-CARD', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-ISSUED-DEVICE-ADMINISTRATION', 'BO-CARD', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-CONSUMER-LOAN', 'BO-LOAN', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-CUSTOMER-CREDIT-RATING', 'BO-CREDIT-ASSESSMENT', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-CUSTOMER-AGREEMENT', 'BO-AGREEMENT', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-CUSTOMER-OFFER', 'BO-PRODUCT', 'REFERENCES_BUSINESS_OBJECT', NULL),
 ('SD-PRODUCT-DIRECTORY', 'BO-PRODUCT', 'REFERENCES_BUSINESS_OBJECT', NULL),

 -- Colaboraciones típicas en escenarios educativos (no son relaciones documentadas por BIAN)
 ('SD-SESSION-DIALOGUE', 'SD-PARTY-AUTHENTICATION', 'COLLABORATES_WITH', 'Ejemplo: el canal solicita verificar la identidad al iniciar sesión.'),
 ('SD-SESSION-DIALOGUE', 'SD-CURRENT-ACCOUNT', 'COLLABORATES_WITH', 'Ejemplo: consulta de saldo desde la aplicación móvil.'),
 ('SD-SESSION-DIALOGUE', 'SD-PAYMENT-ORDER', 'COLLABORATES_WITH', 'Ejemplo: el cliente inicia una transferencia desde el canal.'),
 ('SD-PAYMENT-ORDER', 'SD-CURRENT-ACCOUNT', 'COLLABORATES_WITH', 'Ejemplo: verificar disponibilidad de fondos del ordenante.'),
 ('SD-PAYMENT-ORDER', 'SD-FRAUD-EVALUATION', 'COLLABORATES_WITH', 'Ejemplo: evaluar el riesgo de fraude antes de aprobar.'),
 ('SD-PAYMENT-ORDER', 'SD-PAYMENT-EXECUTION', 'COLLABORATES_WITH', 'Ejemplo: entregar la orden aprobada para su ejecución.'),
 ('SD-PAYMENT-EXECUTION', 'SD-POSITION-KEEPING', 'COLLABORATES_WITH', 'Ejemplo: registrar los movimientos resultantes.'),
 ('SD-CURRENT-ACCOUNT', 'SD-POSITION-KEEPING', 'COLLABORATES_WITH', 'Ejemplo: obtener movimientos para calcular el saldo.'),
 ('SD-CUSTOMER-OFFER', 'SD-PARTY-REFERENCE-DATA-DIRECTORY', 'COLLABORATES_WITH', 'Ejemplo: obtener los datos del solicitante.'),
 ('SD-CUSTOMER-OFFER', 'SD-CUSTOMER-CREDIT-RATING', 'COLLABORATES_WITH', 'Ejemplo: solicitar la evaluación crediticia en una originación.'),
 ('SD-CUSTOMER-OFFER', 'SD-PRODUCT-DIRECTORY', 'COLLABORATES_WITH', 'Ejemplo: consultar las condiciones del producto ofrecido.'),
 ('SD-CUSTOMER-OFFER', 'SD-CONSUMER-LOAN', 'COLLABORATES_WITH', 'Ejemplo: crear el préstamo tras la aceptación.'),
 ('SD-CUSTOMER-OFFER', 'SD-CURRENT-ACCOUNT', 'COLLABORATES_WITH', 'Ejemplo: abrir la cuenta tras la aceptación.'),
 ('SD-CUSTOMER-OFFER', 'SD-CUSTOMER-AGREEMENT', 'COLLABORATES_WITH', 'Ejemplo: vincular el nuevo producto al acuerdo del cliente.'),
 ('SD-CARD-AUTHORIZATION', 'SD-CREDIT-CARD', 'COLLABORATES_WITH', 'Ejemplo: verificar el límite disponible.'),
 ('SD-CARD-AUTHORIZATION', 'SD-FRAUD-EVALUATION', 'COLLABORATES_WITH', 'Ejemplo: evaluar la transacción en tiempo real.'),
 ('SD-SESSION-DIALOGUE', 'SD-ISSUED-DEVICE-ADMINISTRATION', 'COLLABORATES_WITH', 'Ejemplo: bloqueo de tarjeta solicitado por el cliente.'),
 ('SD-PAYMENT-EXECUTION', 'SD-CUSTOMER-EVENT-HISTORY', 'COLLABORATES_WITH', 'Ejemplo: registrar el evento de pago realizado.');

INSERT INTO demo_rel
SELECT sd.ref, op.ref, 'HAS_OPERATION', NULL
FROM demo_el sd JOIN demo_el op ON op.type = 'SERVICE_OPERATION' AND op.ref LIKE sd.ref || '-OP-%'
WHERE sd.type = 'SERVICE_DOMAIN';

INSERT INTO demo_rel
SELECT sd.ref, sd.ref || '-CR', 'HAS_CONTROL_RECORD', NULL
FROM demo_el sd WHERE sd.type = 'SERVICE_DOMAIN';

-- ---------------------------------------------------------------- volcado idempotente
INSERT INTO catalog_element (id, catalog_version_id, external_ref, element_type, name, description,
                             functional_responsibility, category, validation_status, source_reference,
                             attributes, created_at, updated_at)
SELECT md5('DEMO-EDU-1:' || e.ref)::uuid, md5('DEMO-EDU-1')::uuid, e.ref, e.type, e.name, e.description,
       e.responsibility, e.category, 'UNVERIFIED', 'Contenido educativo del catálogo DEMO-EDU-1',
       NULL, now(), now()
FROM demo_el e
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description,
    functional_responsibility = EXCLUDED.functional_responsibility, category = EXCLUDED.category,
    updated_at = now();

INSERT INTO catalog_relationship (id, catalog_version_id, source_element_id, target_element_id,
                                  relationship_type, description, provenance, created_at)
SELECT md5('DEMO-EDU-1:' || r.src || '>' || r.tgt || ':' || r.type)::uuid, md5('DEMO-EDU-1')::uuid,
       md5('DEMO-EDU-1:' || r.src)::uuid, md5('DEMO-EDU-1:' || r.tgt)::uuid,
       r.type, r.description, 'EDUCATIONAL_EXAMPLE', now()
FROM demo_rel r
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description;
