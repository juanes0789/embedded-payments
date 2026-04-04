# HU 1.1 - Bitacora de implementacion

## Fecha

2026-03-31

## Objetivo

Implementar la HU 1.1 (registro de comercio) con enfoque en mantenibilidad y trazabilidad.

## Cambios realizados por capa

### API

Archivo:
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/api/MerchantController.java`

Cambios:
1. Se mantiene `POST /api/v1/merchants`.
2. Se ajusta respuesta para devolver estado textual (`ACTIVE`/`INACTIVE`).
3. Se refuerza validacion de email con `@NotBlank` y `@Email`.

### Application

Archivo:
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/application/RegisterMerchantUseCase.java`

Cambios:
1. Se integra `AuditEventService` para registrar evento de alta.
2. Se determina origen de registro (`SELF_REGISTRATION` o `ADMIN_REGISTRATION`) desde `SecurityContext`.
3. Se registra actor (`authentication.getName()` o `anonymous`).

### Domain

Archivo:
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/domain/services/MerchantDomainService.java`

Cambios:
1. El email se normaliza (`trim` + `lowercase`).
2. Se valida duplicado por email.
3. En duplicado se lanza `ConflictException` con `MERCHANT_ALREADY_EXISTS`.
4. El comercio se crea con estado inicial inactivo (`active=false`).

### Infrastructure / Persistencia de auditoria

Archivos nuevos:
- `src/main/java/com/paymentplatform/embeddedpayments/shared/audit/AuditEvent.java`
- `src/main/java/com/paymentplatform/embeddedpayments/shared/audit/AuditEventRepository.java`
- `src/main/java/com/paymentplatform/embeddedpayments/shared/audit/AuditEventService.java`

Cambios:
1. Se crea entidad `audit_event` con timestamp, actor y origen.
2. Se crea repositorio para persistencia de eventos.
3. Se implementa servicio para registrar `merchant_registered`.

### Shared / Manejo de errores

Archivos:
- `src/main/java/com/paymentplatform/embeddedpayments/shared/exception/DomainException.java`
- `src/main/java/com/paymentplatform/embeddedpayments/shared/exception/GlobalExceptionHandler.java`
- `src/main/java/com/paymentplatform/embeddedpayments/shared/exception/ConflictException.java` (nuevo)

Cambios:
1. `DomainException` ahora soporta `status`, `errorCode`, `details`.
2. Se crea `ConflictException` para mapear `409 Conflict`.
3. Se estandariza respuesta de error con:
   - `errorCode`
   - `message`
   - `details`
   - `traceId`

### Testing

Archivo nuevo:
- `src/test/java/com/paymentplatform/embeddedpayments/merchant/api/MerchantRegistrationApiTests.java`

Escenarios implementados:
1. Registro exitoso por comercio potencial (`201`, estado `INACTIVE`, auditoria creada).
2. Registro exitoso por administrador (`201`, estado `INACTIVE`).
3. Payload invalido (`400`, `VALIDATION_ERROR`).
4. Duplicado (`409`, `MERCHANT_ALREADY_EXISTS`).

## Observaciones de mantenibilidad

- Se centralizo el formato de errores en un solo handler.
- Se separo la logica de auditoria en un servicio dedicado.
- Se definio una excepcion de conflicto explicita para evitar condicionales por mensaje.
- Se agregaron pruebas de API para proteger regresiones de contrato.

## Siguiente paso

- Implementar HU 1.4, 1.5 y 1.6 con la misma estrategia de trazabilidad por capa y pruebas de aceptacion.

## Evidencia de verificacion

Comando ejecutado:

`env -u SPRING_DATASOURCE_URL -u SPRING_DATASOURCE_USERNAME -u SPRING_DATASOURCE_PASSWORD ./mvnw -q test`

Resultado:

- Compilacion y pruebas en verde.
- Incluye ejecucion de `MerchantRegistrationApiTests` con escenarios de HU 1.1.


