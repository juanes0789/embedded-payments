# 📮 Guía: Colección Postman HU 1.2-1.6

## 📥 Importar la Colección

### Opción 1: Desde el archivo (Recomendado)
1. Abre Postman
2. Click en **Import**
3. Selecciona el archivo: `embedded-payments-hu1.2-1.6.postman_collection.json`
4. Click en **Import**

### Opción 2: Desde URL (Si está en repositorio)
```
Copiar URL del archivo en GitHub/GitLab y pegarlo en Import
```

## 📋 Importar Variables de Entorno

1. Click en el icono de **Engranaje** (Settings) en esquina superior derecha
2. Click en **Manage Environments**
3. Click en **Import**
4. Selecciona: `embedded-payments-hu1.2-1.6-environment.json`
5. Selecciona el environment en el dropdown superior derecho

---

## 🔧 Configuración Inicial

### 1. Establecer Base URL
```
En el environment, actualizar:
- baseUrl = http://localhost:8080 (o tu URL de servidor)
```

### 2. Obtener Tokens JWT
El proceso depende de tu implementación de autenticación. Opciones:

#### Opción A: Si tienes endpoint de login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password"}'

# Copiar el token a adminToken en variables
```

#### Opción B: Si usas JWT predefinidos para testing
```
Contactar a DevOps/Security para tokens de test
```

#### Opción C: Generar tokens con Spring Security Test
```java
// En test class con Spring Boot
@SpringBootTest
class JwtTestUtils {
    @Autowired
    private JwtTokenService jwtTokenService;
    
    public void testTokenGeneration() {
        UUID merchantId = UUID.randomUUID();
        String token = jwtTokenService.generateMerchantToken(merchantId);
        System.out.println(token);
    }
}
```

---

## 🚀 Flujo de Testing Recomendado

### Paso 0: Setup
```
Variables necesarias:
✓ baseUrl = http://localhost:8080
✓ adminToken = token JWT de admin
✓ merchantToken = token JWT de merchant
```

### Paso 1: Registrar Comercio
```
Request: POST /api/v1/merchants
├─ Variables:
│  ├─ merchantName = "Tienda Tech S.L."
│  └─ merchantEmail = "tienda.tech@example.com"
└─ Output:
   └─ Guarda el `id` en variable `merchantId`
```

**Ejecutar**: Folder "0. SETUP" → "1.1 POST - Registrar nuevo comercio"

### Paso 2: Actualizar Contacto (HU 1.2)
```
Request: PUT /api/v1/merchants/{merchantId}/contact
├─ Requiere: merchantToken
├─ Body:
│  ├─ contact_name = "Juan Mosquera García"
│  └─ contact_email = "juan@tiendatech.es"
└─ Expected: HTTP 200 + Auditoría registrada
```

**Ejecutar**: Folder "HU 1.2" → "1.2.1 PUT - Actualizar contacto (exitoso)"

### Paso 3: Activar Comercio (HU 1.4) - ⚠️ ANTES de banco
```
Request: PATCH /api/v1/merchants/{merchantId}/activate
├─ Requiere: adminToken
├─ Body:
│  └─ reason = "Documentos verificados"
└─ Expected: HTTP 200 + status = ACTIVE
```

**Ejecutar**: Folder "HU 1.4" → "1.4 PATCH - Activar comercio"

### Paso 4: Registrar Datos Bancarios (HU 1.3)
```
Request: PUT /api/v1/merchants/{merchantId}/bank-account
├─ Requiere: merchantToken + ACTIVO
├─ Body:
│  ├─ iban = "ES9121000418450200051332"
│  ├─ routing_number = "021000021"
│  └─ account_holder_name = "Tienda Tech S.L."
└─ Expected: HTTP 200 + Datos cifrados
```

**Ejecutar**: Folder "HU 1.3" → "1.3.1 PUT - Registrar cuenta bancaria"

### Paso 5: Consultar Detalles (HU 1.6)
```
Request: GET /api/v1/merchants/{merchantId}
├─ Como ADMIN: Ve todo desencriptado
├─ Como PROPIETARIO: Ve todo desencriptado
└─ Como OTRO: Email enmascarado + Banco [RESTRICTED]
```

**Ejecutar múltiples requests**:
- "1.6.1 GET - Como ADMIN" (con adminToken)
- "1.6.2 GET - Como PROPIETARIO" (con merchantToken)
- "1.6.3 GET - Como OTRO" (con otro token)

### Paso 6: Desactivar Comercio (HU 1.5)
```
Request: PATCH /api/v1/merchants/{merchantId}/deactivate
├─ Requiere: adminToken
├─ Body:
│  └─ reason = "Incumplimiento de términos"
└─ Expected: HTTP 200 + status = INACTIVE
```

**Ejecutar**: Folder "HU 1.5" → "1.5 PATCH - Desactivar comercio"

---

## 🧪 Testing por Caso

### ✅ Happy Path (Todo Exitoso)

```
1. Registrar comercio
2. Actualizar contacto (HU 1.2)
3. Activar (HU 1.4)
4. Registrar banco (HU 1.3)
5. Consultar como admin (HU 1.6)
6. Desactivar (HU 1.5)
```

Cada request debe retornar HTTP 200/201 sin errores.

### ⚠️ Validación: Email Inválido

```
1.2.2 - Enviar email formato incorrecto
   └─ Expected: HTTP 400 + error_code = VALIDATION_ERROR
```

### ⚠️ Validación: IBAN Inválido

```
1.3.2 - Enviar IBAN con checksum incorrecto
   └─ Expected: HTTP 400 + error menciona "IBAN"
```

### ⚠️ Validación: Routing Inválido

```
1.3.3 - Enviar routing number con < 9 dígitos
   └─ Expected: HTTP 400 + error menciona "routing"
```

### ⚠️ Autorización: No Admin

```
1.4.1 - Intentar activar con merchantToken
   └─ Expected: HTTP 403 Forbidden
```

```
1.5.1 - Intentar desactivar con merchantToken
   └─ Expected: HTTP 403 Forbidden
```

### 🔍 Enmascaramiento: Otro Comercio

```
1.6.3 - GET como otro merchant
   └─ contact_email: ***@domain.com (enmascarado)
   └─ bank_account_data: [RESTRICTED] (oculto)
```

### 🔓 Sin Autenticación

```
1.2.3 - PUT contacto sin JWT
   └─ Expected: HTTP 401/403 Unauthorized
```

```
1.6.5 - GET detalles sin JWT
   └─ Expected: HTTP 401/403 Unauthorized
```

### 🔎 No Encontrado

```
1.6.4 - GET con ID inexistente
   └─ Expected: HTTP 404 Not Found
```

---

## 📊 Validar Auditoría en BD

### Tests SQL para ejecutar en psql

#### Ver cambios de contacto
```sql
SELECT 
    merchant_id,
    event_type,
    field_name,
    old_value,
    new_value,
    changed_by,
    created_at
FROM merchant_audit_detail 
WHERE merchant_id = 'TU-MERCHANT-ID-AQUI'
  AND event_type = 'MERCHANT_CONTACT_UPDATED'
ORDER BY created_at DESC;
```

**Expected**: Dos filas (contact_name y contact_email actualizados)

#### Ver historial de estados
```sql
SELECT 
    merchant_id,
    previous_status,
    new_status,
    changed_by,
    reason,
    created_at
FROM merchant_status_history 
WHERE merchant_id = 'TU-MERCHANT-ID-AQUI'
ORDER BY created_at DESC;
```

**Expected**: Dos cambios (INACTIVE→ACTIVE, ACTIVE→INACTIVE)

#### Ver todos los eventos
```sql
SELECT 
    event_type,
    entity_type,
    actor,
    happened_at
FROM audit_event 
WHERE entity_id = 'TU-MERCHANT-ID-AQUI'
ORDER BY happened_at DESC;
```

**Expected**: Eventos: CONTACT_UPDATED, ACTIVATED, BANK_REGISTERED, DEACTIVATED

#### Confirmar datos bancarios cifrados
```sql
SELECT 
    id,
    bank_account_hash,
    LENGTH(bank_account_encrypted) as encrypted_length,
    encrypted_bank_data IS NOT NULL as has_encrypted_bytes
FROM merchants 
WHERE id = 'TU-MERCHANT-ID-AQUI';
```

**Expected**: 
- bank_account_hash: SHA-256 hash (no legible)
- encrypted_length: > 100
- has_encrypted_bytes: true

---

## 🔄 Usar Scripts Pre-Request

La colección incluye scripts que automáticamente:
- Salvan tokens en variables
- Extraen IDs para requests posteriores
- Validan responses

**Ver**: En cada request, tab "Tests" para ver validaciones

---

## 💾 Guardar Resultados

### Exportar Resultados
```
Postman → File → Export → Colección
```

### Documentar Hallazgos
Usar la sección "Descripción" de cada request para:
- Notas de testing
- Resultados
- Bugs encontrados

---

## 🐛 Troubleshooting

### Error: "Invalid token"
```
✓ Verificar que adminToken/merchantToken son válidos
✓ Token no ha expirado
✓ Format: "Bearer {token}"
```

### Error: "Merchant not found"
```
✓ Variable merchantId está configurada
✓ Comercio fue registrado exitosamente
✓ No fue eliminado en otro test
```

### Error: "Email already exists"
```
✓ Cambiar merchantEmail a email único
✓ O usar timestamp: "test-{{$timestamp}}@example.com"
```

### Error: "IBAN invalid"
```
✓ Usar IBAN válido: ES9121000418450200051332
✓ Checksum ISO 7064 debe ser correcto
✓ No puede ser modificado
```

### Error: "Routing invalid"
```
✓ Debe ser exactamente 9 dígitos
✓ Usar: 021000021 (válido)
```

### Error: "Merchant inactive"
```
✓ Primero ejecutar "Activar comercio" (HU 1.4)
✓ Luego registrar datos bancarios (HU 1.3)
```

---

## 📝 Notas Importantes

1. **Orden de Ejecución**: Seguir secuencia propuesta
2. **Tokens Frescos**: Generar nuevos si expiran (default 15 min)
3. **Una Colección por Ambiente**: Crear copias para staging/prod
4. **Variables Sensibles**: No commitear tokens reales
5. **Documentación Automática**: Postman genera docs desde colección

---

## 🚀 Próximos Pasos

### Tests Automatizados
```
Usar Newman (CLI de Postman):
newman run embedded-payments-hu1.2-1.6.postman_collection.json \
  -e embedded-payments-hu1.2-1.6-environment.json \
  -r cli,json
```

### Integración CI/CD
```
Agregar a pipeline:
- Pre-deployment testing
- Smoke tests
- Regression tests
```

### Monitoreo
```
Postman Monitors:
- Testing automático cada X minutos
- Alertas por fallos
- Reportes históricos
```

---

**Versión**: 1.0  
**Última actualización**: 30 de Abril de 2026  
**Autor**: GitHub Copilot


