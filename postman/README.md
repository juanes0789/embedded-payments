# 📮 Colecciones Postman - Embedded Payments

## Archivos en este Directorio

### 1. **embedded-payments-hu1.2-1.6.postman_collection.json** ⭐ NUEVO
Colección completa para testing de HU 1.2-1.6 (Gestión Avanzada de Comercios)

**Contenidos**:
- ✅ Setup: Registrar comercio (HU 1.1)
- ✅ HU 1.2: Actualizar información de contacto
- ✅ HU 1.4: Activación de comercio (ADMIN)
- ✅ HU 1.3: Registrar datos bancarios (cifrados)
- ✅ HU 1.6: Consultar detalles con enmascaramiento
- ✅ HU 1.5: Desactivación de comercio
- ✅ Validaciones: Email, IBAN, Routing, Autorización
- ✅ Auditoría: Queries SQL para verificar

**15+ Tests con validaciones**:
- HTTP status codes
- Campos en response
- Enmascaramiento correcto
- Encriptación activa
- Auditoría registrada

### 2. **embedded-payments-hu1.2-1.6-environment.json** ⭐ NUEVO
Variables de entorno para HU 1.2-1.6

**Variables incluidas**:
```
baseUrl              http://localhost:8080
merchantId           (se llena automáticamente)
merchantEmail        tienda.tech@example.com
merchantName         Tienda Tech S.L.
adminToken           (generar en login)
merchantToken        (generar en login)
contactName          Juan Mosquera García
contactEmail         juan.mosquera@tiendatech.es
iban                 ES9121000418450200051332
routingNumber        021000021
accountHolder        Tienda Tech S.L.
```

### 3. **GUIA_POSTMAN_HU1.2-1.6.md** ⭐ NUEVO
Guía paso a paso para usar la colección

**Secciones**:
- Cómo importar colección y environment
- Configuración inicial (obtener tokens JWT)
- Flujo de testing recomendado (6 pasos)
- Testing por caso (happy path, validaciones, errores)
- Validar auditoría en BD (queries SQL)
- Troubleshooting

---

## 📥 Quick Start

### 1. Importar Colección
```
Postman → Import → embedded-payments-hu1.2-1.6.postman_collection.json
```

### 2. Importar Environment
```
Postman → Manage Environments → Import → embedded-payments-hu1.2-1.6-environment.json
```

### 3. Seleccionar Environment
```
Dropdown superior derecho → Embedded Payments - HU 1.2-1.6 Environment
```

### 4. Obtener Tokens JWT
Ver **GUIA_POSTMAN_HU1.2-1.6.md** sección "Configuración Inicial"

### 5. Ejecutar Tests
```
Folder "0. SETUP" → Registrar comercio
Folder "HU 1.2" → Actualizar contacto
Folder "HU 1.4" → Activar comercio
Folder "HU 1.3" → Registrar datos bancarios
Folder "HU 1.6" → Consultar detalles
Folder "HU 1.5" → Desactivar comercio
```

---

## 🧪 Tests Incluidos

### HU 1.2: Actualizar Contacto
- ✅ 1.2.1: Actualizar exitoso (200)
- ✅ 1.2.2: Email inválido (400)
- ✅ 1.2.3: Sin autenticación (403)

### HU 1.3: Registrar Datos Bancarios
- ✅ 1.3.1: Registrar exitoso (200)
- ✅ 1.3.2: IBAN inválido (400)
- ✅ 1.3.3: Routing inválido (400)
- ✅ 1.3.4: Comercio inactivo (403)

### HU 1.4: Activar Comercio
- ✅ 1.4: Activar exitoso (200) - ADMIN
- ✅ 1.4.1: Sin permisos (403) - No admin

### HU 1.5: Desactivar Comercio
- ✅ 1.5: Desactivar exitoso (200) - ADMIN
- ✅ 1.5.1: Sin permisos (403) - No admin

### HU 1.6: Consultar Detalles
- ✅ 1.6.1: Como ADMIN (ve todo)
- ✅ 1.6.2: Como PROPIETARIO (ve todo)
- ✅ 1.6.3: Como OTRO (enmascarado)
- ✅ 1.6.4: No encontrado (404)
- ✅ 1.6.5: Sin autenticación (401)

---

## 🔐 Casos de Seguridad Validados

### Autenticación
- ✅ JWT requerido en endpoints protegidos
- ✅ Sin token → 401 Unauthorized

### Autorización
- ✅ ROLE_ADMIN requerido para cambiar estado
- ✅ Merchant normal → 403 Forbidden

### Encriptación
- ✅ Datos bancarios cifrados (AES-256-GCM)
- ✅ Hash SHA-256 para búsquedas
- ✅ No visible en responses normales

### Enmascaramiento (OWASP)
- ✅ Email: `***@domain.com` (otros)
- ✅ IBAN: `****...1234` (otros)
- ✅ Datos bancarios: `[RESTRICTED]` (otros)
- ✅ Admin: Acceso total

### Validación
- ✅ Email RFC 5322
- ✅ IBAN ISO 7064 checksum
- ✅ Routing Number 9 dígitos
- ✅ Estados válidos (máquina)

---

## 📊 Verificar Auditoría

Después de ejecutar tests, validar en base de datos:

### Cambios de Contacto
```sql
SELECT * FROM merchant_audit_detail 
WHERE event_type = 'MERCHANT_CONTACT_UPDATED'
ORDER BY created_at DESC LIMIT 5;
```

### Historial de Estados
```sql
SELECT * FROM merchant_status_history 
WHERE merchant_id = 'TU-MERCHANT-ID'
ORDER BY created_at DESC;
```

### Todos los Eventos
```sql
SELECT * FROM audit_event 
WHERE entity_id = 'TU-MERCHANT-ID'
ORDER BY happened_at DESC;
```

### Datos Bancarios Cifrados
```sql
SELECT id, bank_account_hash, 
       LENGTH(bank_account_encrypted) as encrypted_length
FROM merchants 
WHERE id = 'TU-MERCHANT-ID';
```

---

## 🔧 Configuración Avanzada

### Usar en Diferentes Ambientes
```
Crear nuevo Environment para cada:
- localhost (development)
- staging.example.com
- api.example.com (producción)
```

### Generar Reportes
```bash
newman run embedded-payments-hu1.2-1.6.postman_collection.json \
  -e embedded-payments-hu1.2-1.6-environment.json \
  -r cli,html --reporter-html-export report.html
```

### CI/CD Integration
```bash
# En pipeline (GitHub Actions, GitLab CI, etc.)
newman run embedded-payments-hu1.2-1.6.postman_collection.json \
  -e embedded-payments-hu1.2-1.6-environment.json \
  --bail
```

### Postman Monitors (Cloud)
```
Postman Cloud → Create Monitor
- Ejecutar colección cada X minutos
- Recibir alertas por fallos
- Dashboard con historico
```

---

## 📝 Variables de Entorno

### Obligatorias
```
baseUrl        = URL del servidor API
adminToken     = JWT válido de admin
merchantToken  = JWT válido de merchant
```

### Opcionales (tienen defaults)
```
merchantEmail  = Email único para cada test
merchantName   = Nombre del comercio
contactName    = Nombre del contacto
contactEmail   = Email de contacto
iban           = IBAN válido (predeterminado)
routingNumber  = Routing válido (predeterminado)
accountHolder  = Titular de cuenta
```

---

## 🚀 Flujo Recomendado

```
1. Importar colección
2. Importar environment
3. Obtener tokens JWT
4. Ejecutar "0. SETUP" → Registrar comercio
5. Ejecutar HU 1.2 → HU 1.3 → HU 1.4 → HU 1.5 → HU 1.6
6. Validar auditoría con queries SQL
7. Revisar logs en servidor
```

---

## 📚 Documentación Relacionada

- `GUIA_POSTMAN_HU1.2-1.6.md`: Guía detallada
- `/docs/sprint-1/hu-1.2-a-1.6/ESPECIFICACION_ENDPOINTS.md`: API spec
- `/docs/sprint-1/hu-1.2-a-1.6/GUIA_EJECUCION_TESTING.md`: Testing manual
- `/docs/sprint-1/hu-1.2-a-1.6/REFERENCIA_RAPIDA.md`: Quick ref

---

## ❓ Preguntas Frecuentes

### ¿Cómo obtengo tokens JWT?
Ver **GUIA_POSTMAN_HU1.2-1.6.md** → "Configuración Inicial"

### ¿Qué hacer si un test falla?
Ver **GUIA_POSTMAN_HU1.2-1.6.md** → "Troubleshooting"

### ¿Cómo valido auditoría?
Ver **GUIA_POSTMAN_HU1.2-1.6.md** → "Verificar Auditoría en BD"

### ¿Puedo usar en producción?
⚠️ **NO**: Crear environment separado para prod con datos reales

### ¿Cómo exporto resultados?
```
Postman → File → Export → Seleccionar formato
```

---

## 📋 Checklist antes de Testing

- [ ] Servidor API corriendo en http://localhost:8080
- [ ] Base de datos accesible
- [ ] Colección importada
- [ ] Environment importado
- [ ] Tokens JWT obtenidos y configurados
- [ ] Variable baseUrl correcta
- [ ] Migrations ejecutadas

---

## 🎯 Cobertura de Testing

Esta colección cubre:
- ✅ 5 Historias de Usuario (HU 1.2-1.6)
- ✅ 15+ Casos de test
- ✅ Happy path y error cases
- ✅ Validaciones de entrada
- ✅ Autorización y autenticación
- ✅ Encriptación y enmascaramiento
- ✅ Auditoría

---

**Versión**: 1.0  
**Última actualización**: 30 de Abril de 2026  
**Estado**: ✅ Lista para usar


