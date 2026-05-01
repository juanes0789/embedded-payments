# 🎨 PLAN DETALLADO: Frontend Pasarela de Pagos

## Tabla de Contenidos
1. [Estructura del Proyecto](#estructura-del-proyecto)
2. [Stack Tecnológico](#stack-tecnológico)
3. [Arquitectura de Componentes](#arquitectura-de-componentes)
4. [Plan de Implementación](#plan-de-implementación)
5. [Configuración Inicial](#configuración-inicial)
6. [Deploy](#deploy)

---

## Estructura del Proyecto

### Directorio Principal

```
frontend/payment-gateway-ui/
├── public/
│   ├── favicon.svg
│   └── logo.svg
│
├── src/
│   ├── assets/
│   │   ├── images/
│   │   ├── styles/
│   │   │   └── tailwind.css
│   │   └── icons/
│   │
│   ├── components/
│   │   ├── shared/
│   │   │   ├── Navbar.vue
│   │   │   ├── Sidebar.vue
│   │   │   ├── Modal.vue
│   │   │   ├── Alert.vue
│   │   │   ├── Button.vue
│   │   │   ├── Input.vue
│   │   │   └── Loading.vue
│   │   │
│   │   ├── auth/
│   │   │   ├── LoginForm.vue
│   │   │   ├── RegisterForm.vue
│   │   │   └── ForgotPassword.vue
│   │   │
│   │   ├── dashboard/
│   │   │   ├── StatsCard.vue
│   │   │   ├── RecentTransactions.vue
│   │   │   ├── Chart.vue
│   │   │   └── QuickActions.vue
│   │   │
│   │   ├── merchant/
│   │   │   ├── ContactForm.vue      (HU 1.2)
│   │   │   ├── BankAccountForm.vue  (HU 1.3)
│   │   │   ├── StatusBadge.vue      (HU 1.4/1.5)
│   │   │   ├── MerchantCard.vue     (HU 1.6)
│   │   │   └── ProfileSettings.vue
│   │   │
│   │   ├── payment/
│   │   │   ├── PaymentForm.vue
│   │   │   ├── PaymentWidget.vue
│   │   │   ├── PaymentStatus.vue
│   │   │   └── TransactionList.vue
│   │   │
│   │   ├── reports/
│   │   │   ├── SalesChart.vue
│   │   │   ├── TransactionReport.vue
│   │   │   ├── ExportButton.vue
│   │   │   └── DateRangeFilter.vue
│   │   │
│   │   └── errors/
│   │       ├── ErrorBoundary.vue
│   │       ├── NotFound.vue
│   │       └── Unauthorized.vue
│   │
│   ├── layouts/
│   │   ├── AppLayout.vue
│   │   ├── AuthLayout.vue
│   │   └── BlankLayout.vue
│   │
│   ├── pages/
│   │   ├── index.vue                (/)
│   │   ├── login.vue                (/login)
│   │   ├── register.vue             (/register)
│   │   ├── dashboard.vue            (/dashboard)
│   │   │
│   │   ├── settings/
│   │   │   ├── index.vue            (/settings)
│   │   │   ├── contact.vue          (/settings/contact - HU 1.2)
│   │   │   ├── bank.vue             (/settings/bank - HU 1.3)
│   │   │   ├── profile.vue          (/settings/profile)
│   │   │   └── security.vue         (/settings/security)
│   │   │
│   │   ├── transactions/
│   │   │   ├── index.vue            (/transactions)
│   │   │   └── [id].vue             (/transactions/:id)
│   │   │
│   │   ├── reports/
│   │   │   ├── index.vue            (/reports)
│   │   │   ├── sales.vue            (/reports/sales)
│   │   │   └── transactions.vue     (/reports/transactions)
│   │   │
│   │   └── admin/                   (Futuro)
│   │       ├── merchants.vue
│   │       └── users.vue
│   │
│   ├── services/
│   │   ├── api.ts                   (Configuración Axios)
│   │   ├── auth.ts                  (Autenticación)
│   │   ├── merchants.ts             (Gestión comercios)
│   │   ├── transactions.ts          (Transacciones)
│   │   ├── reports.ts               (Reportes)
│   │   ├── storage.ts               (LocalStorage wrapper)
│   │   └── logger.ts                (Logging)
│   │
│   ├── stores/                      (Pinia State Management)
│   │   ├── index.ts
│   │   ├── modules/
│   │   │   ├── auth.ts              (JWT, usuario actual)
│   │   │   ├── merchant.ts          (Datos comercio)
│   │   │   ├── transactions.ts      (Transacciones)
│   │   │   ├── ui.ts                (Estado UI)
│   │   │   └── notifications.ts     (Alertas)
│   │   └── composables/
│   │       ├── useAuth.ts
│   │       ├── useMerchant.ts
│   │       ├── useNotification.ts
│   │       └── useApi.ts
│   │
│   ├── types/
│   │   ├── index.ts
│   │   ├── api.ts                   (Respuestas API)
│   │   ├── merchant.ts              (Comercios)
│   │   ├── transaction.ts           (Transacciones)
│   │   ├── auth.ts                  (Autenticación)
│   │   └── enums.ts                 (Enumeraciones)
│   │
│   ├── utils/
│   │   ├── validators.ts            (Validaciones)
│   │   ├── formatters.ts            (Formato moneda, fecha)
│   │   ├── helpers.ts               (Funciones helpers)
│   │   ├── constants.ts             (Constantes)
│   │   └── errors.ts                (Manejo de errores)
│   │
│   ├── middleware/
│   │   ├── auth.ts                  (Guard de autenticación)
│   │   └── admin.ts                 (Guard de admin)
│   │
│   ├── plugins/
│   │   ├── notifications.ts
│   │   ├── http-client.ts
│   │   └── i18n.ts                  (Internacionalización - Futuro)
│   │
│   ├── App.vue
│   ├── main.ts
│   └── router.ts
│
├── tests/
│   ├── unit/
│   │   ├── components/
│   │   ├── stores/
│   │   ├── services/
│   │   └── utils/
│   │
│   ├── e2e/
│   │   ├── login.spec.ts
│   │   ├── merchant-settings.spec.ts
│   │   ├── transactions.spec.ts
│   │   └── payments.spec.ts
│   │
│   └── fixtures/
│       └── mocks.ts
│
├── .env.example
├── .env.development
├── .env.staging
├── .env.production
│
├── .eslintrc.json
├── .prettierrc.json
├── tsconfig.json
├── vite.config.ts
├── vitest.config.ts
│
├── package.json
├── package-lock.json
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
│
├── ARCHITECTURE.md
├── DEVELOPMENT.md
├── DEPLOYMENT.md
├── README.md
└── CHANGELOG.md
```

---

## Stack Tecnológico

### package.json

```json
{
  "name": "payment-gateway-ui",
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx,.cts,.mts --fix",
    "type-check": "vue-tsc --noEmit",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:e2e": "playwright test",
    "test:coverage": "vitest --coverage"
  },
  "dependencies": {
    "vue": "^3.3.4",
    "vue-router": "^4.2.4",
    "pinia": "^2.1.3",
    "axios": "^1.5.0",
    "tailwindcss": "^3.3.0",
    "lucide-vue-next": "^0.263.1",
    "zod": "^3.22.2",
    "vee-validate": "^4.11.0",
    "date-fns": "^2.30.0",
    "uuid": "^9.0.0",
    "dompurify": "^3.0.6",
    "tweetnacl": "^1.0.3",
    "chart.js": "^4.4.0",
    "vue-chartjs": "^5.2.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.4.0",
    "@vue/test-utils": "^2.4.1",
    "@vitest/ui": "^0.33.0",
    "typescript": "^5.1.6",
    "vue-tsc": "^1.8.9",
    "vite": "^4.4.9",
    "vitest": "^0.33.0",
    "@testing-library/vue": "^8.0.0",
    "@typescript-eslint/eslint-plugin": "^6.4.1",
    "@typescript-eslint/parser": "^6.4.1",
    "eslint": "^8.47.0",
    "eslint-plugin-vue": "^9.17.0",
    "prettier": "^3.0.3",
    "tailwindcss": "^3.3.0",
    "postcss": "^8.4.30",
    "autoprefixer": "^10.4.15",
    "@playwright/test": "^1.38.1"
  }
}
```

---

## Arquitectura de Componentes

### Jerarquía y Flujo

```
App.vue (Root)
│
├─ Router
│  └─ Layout (AppLayout / AuthLayout / BlankLayout)
│     └─ Pages (Vistas)
│        └─ Components (Componentes)
│
└─ Stores (Pinia)
   ├─ auth.ts        (JWT, usuario)
   ├─ merchant.ts    (Datos comercio)
   ├─ transactions.ts (Transacciones)
   ├─ ui.ts          (Estado UI)
   └─ notifications.ts (Alertas)

Services (Llamadas API)
│
├─ auth.ts           (Login, logout, refresh)
├─ merchants.ts      (CRUD comercios)
├─ transactions.ts   (Listar, detalle)
├─ reports.ts        (Reportes, exportar)
└─ api.ts            (Configuración Axios)
```

### Composables (Hooks)

```typescript
// useAuth.ts
export const useAuth = () => {
  const authStore = useAuthStore();
  
  const login = async (email: string, password: string) => { ... };
  const logout = async () => { ... };
  const refreshToken = async () => { ... };
  const isAuthenticated = computed(() => !!authStore.token);
  const user = computed(() => authStore.user);
  
  return {
    login,
    logout,
    refreshToken,
    isAuthenticated,
    user,
    ...
  };
};

// useMerchant.ts
export const useMerchant = () => {
  const merchantStore = useMerchantStore();
  const { data, isLoading, error } = useApi();
  
  const fetchMerchant = async (id: string) => { ... };
  const updateContact = async (data) => { ... };
  const registerBank = async (data) => { ... };
  const activate = async () => { ... };
  
  return {
    fetchMerchant,
    updateContact,
    registerBank,
    activate,
    merchant: computed(() => merchantStore.current),
    ...
  };
};
```

---

## Plan de Implementación

### Sprint 2 (Backend soporte)
```
Backlog:
[ ] Endpoint POST /api/v1/auth/refresh
[ ] Endpoint POST /api/v1/auth/logout
[ ] Rate limiting middleware
[ ] CORS headers finales
[ ] Documentación Swagger actualizada

Horas: 16 (40% del sprint)
```

### Sprint 3 (Frontend MVP)
```
Backlog:
[ ] Setup Vite + Vue 3 + TypeScript + Tailwind
[ ] Configuración Pinia state management
[ ] Axios interceptor + JWT handling
[ ] Autenticación (Login/Logout)
[ ] Dashboard básico
[ ] HU 1.2: Actualizar contacto
[ ] HU 1.3: Registrar datos bancarios
[ ] HU 1.6: Ver detalles
[ ] Listado de transacciones

Horas: 64 (100% del sprint)
```

### Sprint 4
```
Backlog:
[ ] HU 1.4: Activar comercio (UI)
[ ] HU 1.5: Desactivar comercio (UI)
[ ] Reportes básicos
[ ] Exportar CSV
[ ] Tests unitarios (Vitest)
[ ] Tests E2E (Playwright)

Horas: 64 (100% del sprint)
```

---

## Configuración Inicial

### vite.config.ts

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  build: {
    outDir: 'dist',
    minify: 'terser',
    sourcemap: true,
  }
})
```

### tsconfig.json

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```

### .env.example

```env
VITE_API_URL=http://localhost:8080
VITE_APP_NAME="Payment Gateway"
VITE_APP_VERSION=0.0.1
VITE_LOG_LEVEL=info
```

---

## Deploy

### Opción 1: Docker

**Dockerfile**:
```dockerfile
FROM node:21-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  payment-ui:
    build: ./frontend/payment-gateway-ui
    ports:
      - "3000:80"
    environment:
      VITE_API_URL: http://backend:8080
    depends_on:
      - backend
```

### Opción 2: Vercel

```json
{
  "buildCommand": "npm run build",
  "outputDirectory": "dist",
  "env": {
    "VITE_API_URL": "@api_url"
  }
}
```

---

**Documento**: Frontend Implementation Plan  
**Versión**: 1.0  
**Estado**: ✅ LISTO PARA INICIAR SPRINT 3


