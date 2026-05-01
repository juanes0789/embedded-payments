# 🏗️ ARQUITECTURA FRONTEND: Diagrama y Especificación

## Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────────┐
│                      USUARIO FINAL                              │
│            (Comercio / Administrador / Cliente)                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (Vue 3 + TypeScript)                │
│                    (Módulo Independiente)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           ROUTER (vue-router)                            │  │
│  │   /login | /dashboard | /settings | /transactions        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         LAYOUTS (AppLayout / AuthLayout)                 │  │
│  │         └─ Navbar, Sidebar, Theme                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            PAGES & COMPONENTS                            │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │ Dashboard  │ Settings │ Transactions │ Reports   │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      STATE MANAGEMENT (Pinia)                            │  │
│  │  ┌─────────────────────────────────────────────────┐    │  │
│  │  │ authStore  │ merchantStore │ transactionStore │    │  │
│  │  │ uiStore    │ notificationStore                  │    │  │
│  │  └─────────────────────────────────────────────────┘    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      SERVICES (API Communication)                        │  │
│  │  ┌─────────────────────────────────────────────────┐    │  │
│  │  │ authService │ merchantService │ reportService  │    │  │
│  │  │ transactionService │ api (Axios)               │    │  │
│  │  └─────────────────────────────────────────────────┘    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
        ┌───────────────────────────────────────┐
        │   INTERCEPTOR HTTP (JWT + Token)     │
        │   ┌─────────────────────────────────┐│
        │   │ Request: add Authorization      ││
        │   │ Response: handle 401/403/refresh││
        │   └─────────────────────────────────┘│
        └───────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                         │
│                 (Módulo Independiente)                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  REST API Endpoints:                                            │
│  ├─ POST   /api/v1/auth/login                                  │
│  ├─ POST   /api/v1/auth/refresh                                │
│  ├─ POST   /api/v1/auth/logout                                 │
│  │                                                              │
│  ├─ GET    /api/v1/merchants/{id}                              │
│  ├─ PUT    /api/v1/merchants/{id}/contact         (HU 1.2)    │
│  ├─ PUT    /api/v1/merchants/{id}/bank-account    (HU 1.3)    │
│  ├─ PATCH  /api/v1/merchants/{id}/activate        (HU 1.4)    │
│  ├─ PATCH  /api/v1/merchants/{id}/deactivate      (HU 1.5)    │
│  │                                                              │
│  ├─ GET    /api/v1/transactions                                │
│  ├─ GET    /api/v1/transactions/{id}                           │
│  ├─ POST   /api/v1/refunds                                     │
│  │                                                              │
│  ├─ GET    /api/v1/reports/summary                             │
│  ├─ GET    /api/v1/reports/transactions                        │
│  └─ GET    /api/v1/reports/export                              │
│                                                                 │
│  Security:                                                       │
│  ├─ JWT Authentication                                          │
│  ├─ Role-based Authorization (ROLE_ADMIN, ROLE_MERCHANT)       │
│  ├─ CORS enabled                                                │
│  ├─ Rate limiting                                               │
│  └─ Data encryption (AES-256-GCM)                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   DATABASE (PostgreSQL)                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ merchants │ transactions │ refunds │ audit_logs         │   │
│  │ audit_event │ merchant_status_history │ merchant_audit  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Flujo de Autenticación

```
┌────────────────────────────────┐
│   1. Usuario ingresa credenciales  │
│      email + password              │
└────────────────────────────────┘
              ↓
┌────────────────────────────────┐
│   2. Validación en Frontend     │
│      (Zod + VeeValidate)        │
└────────────────────────────────┘
              ↓
┌────────────────────────────────┐
│   3. POST /api/v1/auth/login   │
│      (sin JWT)                  │
└────────────────────────────────┘
              ↓
┌────────────────────────────────┐
│   4. Backend valida             │
│      - Existe usuario           │
│      - Password válido          │
│      - Status activo            │
└────────────────────────────────┘
              ↓
┌────────────────────────────────┐
│   5. Backend genera JWT         │
│      - Firma con secret         │
│      - TTL: 15 minutos          │
└────────────────────────────────┘
              ↓
┌────────────────────────────────┐
│   6. Frontend recibe JWT        │
│      - Guardar en localStorage  │
│      - Update authStore         │
└────────────────────────────────┘
              ↓
┌────────────────────────────────┐
│   7. Redireccionar a /dashboard │
└────────────────────────────────┘

PARA REQUESTS POSTERIORES:
┌────────────────────────────────┐
│   Interceptor agrega:           │
│   Authorization: Bearer {token} │
└────────────────────────────────┘
```

---

## Flujo de Datos: Actualizar Contacto (HU 1.2)

```
USER INPUT
    ↓
┌──────────────────────────────┐
│ Component: ContactForm.vue   │
│ ┌────────────────────────────┤
│ │ name: "Juan Mosquera"      │
│ │ email: "juan@example.com"  │
│ └────────────────────────────┤
└──────────────────────────────┘
    ↓
┌──────────────────────────────┐
│ Validación (VeeValidate)    │
│ ✓ Nombre no vacío            │
│ ✓ Email válido (RFC 5322)   │
└──────────────────────────────┘
    ↓
┌──────────────────────────────┐
│ Call Service Method:         │
│ merchantService              │
│  .updateContact({...})       │
└──────────────────────────────┘
    ↓
┌──────────────────────────────┐
│ axios.put(...)               │
│ + JWT automático en header   │
│ + Error handling             │
└──────────────────────────────┘
    ↓
BACKEND PROCESSING
    ↓
┌──────────────────────────────┐
│ Response:                    │
│ {                            │
│   id: uuid,                  │
│   name: "Juan Mosquera",     │
│   email: "...",              │
│   contact_name: "Juan",      │
│   contact_email: "juan@...", │
│   status: "ACTIVE",          │
│   updated_at: timestamp,     │
│   message: "Success"         │
│ }                            │
└──────────────────────────────┘
    ↓
┌──────────────────────────────┐
│ Store Update (Pinia):        │
│ merchantStore.update({...})  │
└──────────────────────────────┘
    ↓
┌──────────────────────────────┐
│ Show Notification:           │
│ "Contacto actualizado"       │
│ (Toast / Alert)              │
└──────────────────────────────┘
    ↓
┌──────────────────────────────┐
│ Update UI:                   │
│ Form values → response       │
│ Refresh component            │
└──────────────────────────────┘
```

---

## Estructura de Estado (Pinia)

### auth.ts

```typescript
interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: User | null;
  isLoading: boolean;
  error: string | null;
}

interface User {
  id: string;
  email: string;
  role: "ROLE_ADMIN" | "ROLE_MERCHANT";
  createdAt: Date;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('token'),
    refreshToken: localStorage.getItem('refreshToken'),
    user: null,
    isLoading: false,
    error: null,
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === "ROLE_ADMIN",
    isMerchant: (state) => state.user?.role === "ROLE_MERCHANT",
  },
  
  actions: {
    async login(email: string, password: string) {
      this.isLoading = true;
      try {
        const { token, refreshToken, user } = await authService.login(...);
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
        localStorage.setItem('token', token);
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    async logout() {
      this.token = null;
      this.refreshToken = null;
      this.user = null;
      localStorage.removeItem('token');
      await authService.logout();
    },
  }
});
```

### merchant.ts

```typescript
interface MerchantState {
  current: Merchant | null;
  list: Merchant[];
  isLoading: boolean;
  error: string | null;
}

interface Merchant {
  id: string;
  name: string;
  email: string;
  contactName?: string;
  contactEmail?: string;
  status: "ACTIVE" | "INACTIVE" | "SUSPENDED";
  bankAccountData?: string;
  updatedAt: Date;
}

export const useMerchantStore = defineStore('merchant', {
  state: (): MerchantState => ({
    current: null,
    list: [],
    isLoading: false,
    error: null,
  }),
  
  actions: {
    async fetchById(id: string) {
      this.isLoading = true;
      try {
        this.current = await merchantService.getById(id);
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    updateContact(data: { name: string; email: string }) {
      if (this.current) {
        this.current.contactName = data.name;
        this.current.contactEmail = data.email;
      }
    },
  }
});
```

---

## Seguridad en Frontend

```
┌──────────────────────────────────────┐
│    1. ALMACENAMIENTO DE TOKEN        │
├──────────────────────────────────────┤
│ localStorage.setItem('token', jwt)   │
│                                      │
│ ⚠️  Nota: Futuro usar HttpOnly       │
│     cookies para mayor seguridad     │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│   2. VALIDACIÓN DE INPUT             │
├──────────────────────────────────────┤
│ • Email: RFC 5322 (Zod)              │
│ • IBAN: ISO 7064 (Custom)            │
│ • Longitud máxima                    │
│ • Caracteres permitidos              │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│   3. SANITIZACIÓN HTML               │
├──────────────────────────────────────┤
│ import DOMPurify from 'dompurify';   │
│ const clean = DOMPurify.sanitize(...) │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│   4. RATE LIMITING CLIENTE           │
├──────────────────────────────────────┤
│ • Debounce en búsquedas (300ms)      │
│ • Throttle en scroll (1s)            │
│ • Cooldown en botones (3s)           │
│ • Max 5 login attempts / 15 min      │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│   5. CSP (Content Security Policy)   │
├──────────────────────────────────────┤
│ default-src 'self';                  │
│ script-src 'self' 'unsafe-inline';   │
│ style-src 'self' 'unsafe-inline';    │
│ img-src 'self' data: https:;         │
│ connect-src 'self' api.example.com;  │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│   6. HTTPS ENFORCED                  │
├──────────────────────────────────────┤
│ • Siempre usar https://              │
│ • HSTS headers                       │
│ • Certificados válidos               │
└──────────────────────────────────────┘
```

---

## Integración Módulos

```
Módulo Frontend (Independiente)
├─ Repositorio: frontend/payment-gateway-ui
├─ Build: npm run build → dist/
├─ Deploy: Vercel / Docker / S3 + CloudFront
├─ Versionado: Semver (0.1.0 → 1.0.0)
└─ Independencia:
   - No requiere código backend
   - Solo consume API REST
   - Puede deployar sin backend
   - Puede cambiar independientemente

Módulo Backend (Independiente)
├─ Repositorio: backend/
├─ Build: mvn build → jar
├─ Deploy: Docker / EC2 / K8s
├─ Versionado: Semver
└─ Independencia:
   - API estable
   - Versionamiento de endpoints
   - Backward compatible
   - Puede evolucionar sin afectar frontend

Comunicación:
├─ Protocol: HTTP/HTTPS
├─ Format: JSON
├─ Auth: JWT
├─ Rate Limit: 100 req/min por IP
└─ Monitoring: CloudWatch / Datadog
```

---

**Documento**: Frontend Architecture  
**Versión**: 1.0  
**Estado**: ✅ LISTO PARA IMPLEMENTACIÓN


