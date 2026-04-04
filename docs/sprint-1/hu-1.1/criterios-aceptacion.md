# HU 1.1 - Criterios de aceptacion (checklist)

## Escenarios comprometidos

- [x] Registro exitoso por comercio potencial (payload valido, `201`, estado `INACTIVE`).
- [x] Registro exitoso por administrador (`201`, estado `INACTIVE`).
- [x] Registro con payload invalido (`400 Bad Request`).
- [x] Registro duplicado por identificador (`409 Conflict`).
- [x] Evento de auditoria en registro (timestamp y origen).

## Estandar minimo de respuesta de error

- Codigo HTTP correcto.
- Cuerpo uniforme con:
  - `errorCode`
  - `message`
  - `details`
  - `traceId`

