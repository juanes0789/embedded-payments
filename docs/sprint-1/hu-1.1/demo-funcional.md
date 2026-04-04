# HU 1.1 - Demo funcional

## Objetivo de la demo

Mostrar que el sistema permite registrar un comercio de forma confiable,
controlando validaciones, duplicados y trazabilidad.

## Flujo de demo

1. Enviar `POST /api/v1/merchants` con payload valido.
2. Validar respuesta `201` con `id` y estado `INACTIVE`.
3. Repetir registro con mismo email y validar `409`.
4. Enviar payload invalido y validar `400`.
5. Mostrar evidencia de auditoria del registro exitoso.

## Evidencia a mostrar

- Request/response de cada escenario.
- Registro persistido del comercio.
- Registro de auditoria con timestamp y origen.

