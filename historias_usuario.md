F1. Registro de transacciones
El enfoque aquí es la captura inicial y segura del intento de pago y su información básica.

HU 1.1: Creación del registro de pago

Descripción: Como sistema de pagos, quiero registrar de forma inmutable cada intento de transacción con un identificador único (ID), para asegurar que no se pierda información de las operaciones procesadas.

Criterios de Aceptación:

Debe generarse un ID de transacción alfanumérico único.

El registro debe contener: monto, moneda, ID del comercio/usuario origen, método de pago y fecha/hora (timestamp).

HU 1.2: Control de Idempotencia (Prevención de duplicados)

Descripción: Como API de pagos, quiero validar un idempotency key en cada solicitud de transacción, para evitar que un problema de red o doble clic genere cobros duplicados al cliente.

Criterios de Aceptación:

Si se recibe un request con un idempotency key ya procesado en las últimas 24 horas, el sistema debe devolver el resultado de la transacción original sin cobrar de nuevo.

F2. Historial de operaciones
Esta funcionalidad está orientada a la visualización y consulta de las transacciones ya registradas.

HU 2.1: Visualización de transacciones por comercio/usuario

Descripción: Como usuario/comercio, quiero ver un listado de mis transacciones ordenadas de la más reciente a la más antigua, para poder monitorear el flujo de mi dinero.

Criterios de Aceptación:

El listado debe mostrar: Fecha, ID de transacción, Monto, Estado (Aprobado, Pendiente, Fallido) y Concepto.

La vista debe estar paginada (ej. 20 registros por página).

HU 2.2: Filtros y búsqueda de operaciones

Descripción: Como usuario/comercio, quiero poder filtrar mis operaciones por rango de fechas y estado, para facilitar mi conciliación y atención a mis clientes.

Criterios de Aceptación:

Permitir filtrar por estado: Éxito, Rechazado, Reembolsado, Pendiente.

Permitir buscar por el ID exacto de la transacción.

F3. Ledger financiero
El corazón del sistema. Un ledger en pagos embebidos no es solo una lista, es un sistema de contabilidad de partida doble (débitos y créditos) para mantener los saldos perfectos.

HU 3.1: Generación de asientos contables (Partida doble)

Descripción: Como motor financiero, quiero que cada transacción exitosa genere múltiples movimientos (asientos) de débito y crédito, para separar el saldo del comercio, la comisión de la plataforma y los impuestos.

Criterios de Aceptación:

Una transacción de pago de $100 con 3% de comisión debe generar: +$100 en cuenta "Fondos Recibidos", -$3 en cuenta "Comisiones", y +$97 en cuenta "Saldo por Pagar al Comercio".

La suma de los créditos y débitos de un evento debe ser siempre cero.

HU 3.2: Cálculo de saldo disponible y en retención

Descripción: Como comercio, quiero ver mi saldo total separado en "Disponible para retirar" y "En retención (Pendiente)", para tener claridad sobre la liquidez real de mi negocio.

Criterios de Aceptación:

Los fondos de transacciones recién aprobadas deben sumarse al "Saldo en retención".

Después de X días (según la regla del negocio), los fondos deben pasar automáticamente al "Saldo Disponible".

F4. Auditoría de cambios de estado
Los pagos nunca son estáticos; pasan de Creado > Procesando > Autorizado > Capturado / Reembolsado.

HU 4.1: Bitácora (Timeline) de estados del pago

Descripción: Como agente de soporte o administrador, quiero visualizar la línea de tiempo completa de los cambios de estado de una transacción, para poder diagnosticar rápidamente problemas o reclamos de clientes.

Criterios de Aceptación:

Cada cambio de estado debe quedar registrado de forma inmutable con su timestamp exacto (hasta milisegundos).

Debe registrarse qué actor originó el cambio (ej. Webhook del procesador, Acción manual del admin, Sistema interno).

HU 4.2: Registro de motivos de rechazo

Descripción: Como analista de riesgos o soporte, quiero que cuando un pago cambie a estado "Fallido" se registre el código de error o motivo del rechazo del banco, para dar retroalimentación útil al cliente.

Criterios de Aceptación:

El cambio al estado Fallido debe incluir un campo reason_code (ej. Fondos insuficientes, Tarjeta expirada, Fraude sospechoso).