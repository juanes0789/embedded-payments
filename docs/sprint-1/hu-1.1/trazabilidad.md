# HU 1.1 - Trazabilidad tecnica

| Criterio | Endpoint | Capa API | Capa Application | Capa Domain | Capa Infrastructure |
|---|---|---|---|---|---|
| Registro exitoso | `POST /api/v1/merchants` | `MerchantController` | `RegisterMerchantUseCase` | `MerchantDomainService` | `MerchantRepositoryImpl` |
| Payload invalido | `POST /api/v1/merchants` | `MerchantController` + validaciones | - | - | - |
| Registro duplicado | `POST /api/v1/merchants` | `MerchantController` | `RegisterMerchantUseCase` | regla de unicidad por email | `findByEmail` |
| Auditoria de registro | `POST /api/v1/merchants` | `MerchantController` | `RegisterMerchantUseCase` | - | repositorio de auditoria |

## Nota

Esta trazabilidad se actualiza en paralelo con la implementacion para soportar la evidencia de sprint.

