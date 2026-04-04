# HU 1.1 - Plan de implementacion por capas

## Objetivo tecnico

Implementar HU 1.1 de forma vertical, respetando arquitectura modular por capas:
`api -> application -> domain -> infrastructure`.

## API

Archivo base:
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/api/MerchantController.java`

Tareas:
1. Ajustar contrato de `POST /api/v1/merchants`.
2. Retornar `201 Created` con `id` y `status`.
3. Mapear errores de validacion a `400`.
4. Mapear duplicado a `409`.

## Application

Archivo base:
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/application/RegisterMerchantUseCase.java`

Tareas:
1. Orquestar registro desde controller.
2. Determinar origen de registro (self/admin).
3. Invocar auditoria al finalizar alta.

## Domain

Archivos base:
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/domain/entity/Merchant.java`
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/domain/services/MerchantDomainService.java`

Tareas:
1. Forzar estado inicial `INACTIVE`.
2. Validar unicidad por email.
3. Definir excepcion de negocio para duplicado.

## Infrastructure

Archivos base:
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/infrastructure/repository/MerchantJpaRepository.java`
- `src/main/java/com/paymentplatform/embeddedpayments/merchant/infrastructure/repository/MerchantRepositoryImpl.java`

Tareas:
1. Confirmar consulta `findByEmail`.
2. Persistir entidad con estado inactivo.
3. Crear persistencia de auditoria de registro.

## Seguridad (minimo para HU 1.1)

Archivo base:
- `src/main/java/com/paymentplatform/embeddedpayments/shared/security/SecurityConfig.java`

Tarea:
- Permitir registro publico sin romper restricciones de endpoints administrativos.

## Definition of Done

- Endpoint funcional con `201`, `400` y `409`.
- Comercio creado en estado `INACTIVE`.
- Auditoria registrada.
- Pruebas automatizadas para los escenarios de HU 1.1 en verde.

