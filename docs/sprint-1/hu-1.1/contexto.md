# HU 1.1 - Contexto

## Historia de usuario

Como comercio potencial o administrador de la plataforma,
quiero registrar un nuevo comercio enviando sus datos basicos,
para darlo de alta en el ecosistema de pagos con un estado inicial controlado.

## Problema que resuelve

Permite incorporar nuevos comercios de forma segura y trazable,
con validaciones de entrada y control de duplicados.

## Resultado esperado

- Alta de comercio con estado inicial `INACTIVE`.
- Respuesta HTTP `201 Created` con ID unico.
- Registro de auditoria con timestamp y origen.
- Errores estandarizados para payload invalido y duplicado.

## Actores

- Comercio potencial (auto-registro).
- Administrador (registro asistido).

## Alcance sprint 1 (congelado)

### Incluye

- Endpoint de registro de comercio.
- Validacion de payload.
- Deteccion de duplicados.
- Auditoria de evento de registro.

### No incluye

- Gestion completa de administradores (F1.1, F1.2).
- Datos financieros del comercio (HU 1.3).
- MFA y controles avanzados de identidad.

## Supuestos

- El identificador de duplicado para sprint 1 es el email.
- El evento de auditoria se registra con origen `SELF_REGISTRATION` o `ADMIN_REGISTRATION`.

