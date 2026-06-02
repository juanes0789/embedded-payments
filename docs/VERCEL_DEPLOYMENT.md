# 🚀 Guía de Despliegue en Vercel

## Arquitectura
```
Vercel (Frontend)
    ↓ HTTPS
Render (Backend API)
    ↓
Base de Datos H2
```

## Paso 1: Preparar el Repositorio

### Estructura esperada en GitHub:
```
embedded-payments/
├── frontend/payment-gateway-ui/  ← Frontend deployable en Vercel
├── src/                           ← Backend (en Render)
└── docs/
```

**El frontend ya está en:** `/frontend/payment-gateway-ui/`

## Paso 2: Crear Proyecto en Vercel

### Opción A: Desde la CLI (Recomendado)
```bash
cd frontend/payment-gateway-ui
npm i -g vercel
vercel login  # Autenticarse con GitHub
vercel        # Deploy interactivo
```

### Opción B: Desde Dashboard Vercel
1. Ir a [vercel.com](https://vercel.com/)
2. Conectar GitHub
3. Seleccionar `embedded-payments`
4. Framework: **Vue.js**
5. Root Directory: **./frontend/payment-gateway-ui**

## Paso 3: Configurar Variables de Entorno en Vercel

En el dashboard de Vercel, ir a **Settings → Environment Variables**

### Variables Necesarias:

| Variable | Producción | Development |
|----------|-----------|-------------|
| `VITE_API_URL` | `https://embedded-payments-1.onrender.com` | `http://localhost:8085` |
| `VITE_FALLBACK_API_URL` | `https://embedded-payments-1.onrender.com` | `https://embedded-payments-1.onrender.com` |
| `VITE_APP_NAME` | `Payment Gateway` | `Payment Gateway (Dev)` |
| `VITE_APP_VERSION` | `1.0.0` | `0.1.0` |
| `VITE_LOG_LEVEL` | `error` | `debug` |

### Cómo agregar en Vercel:
```yaml
Para Producción (main branch):
  VITE_API_URL: https://embedded-payments-1.onrender.com
  VITE_FALLBACK_API_URL: https://embedded-payments-1.onrender.com
  VITE_APP_NAME: Payment Gateway
  VITE_LOG_LEVEL: error

Para Preview (develop branch):
  VITE_API_URL: https://embedded-payments-1.onrender.com
  VITE_FALLBACK_API_URL: https://embedded-payments-1.onrender.com
  VITE_APP_NAME: Payment Gateway (Preview)
  VITE_LOG_LEVEL: debug
```

## Paso 4: Configurar vercel.json

Crear/verificar `frontend/payment-gateway-ui/vercel.json`:

```json
{
  "buildCommand": "npm run build",
  "outputDirectory": "dist",
  "framework": "vue",
  "env": {
    "VITE_API_URL": "https://embedded-payments-1.onrender.com",
    "VITE_FALLBACK_API_URL": "https://embedded-payments-1.onrender.com"
  },
  "routes": [
    {
      "src": "^/api/(.*)",
      "dest": "https://embedded-payments-1.onrender.com/api/$1"
    },
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ]
}
```

## Paso 5: Configurar CORS en Backend (Render)

En el backend (`src/main/resources/application.properties`):

```properties
# CORS Configuration for Vercel Frontend
frontend.urls=https://your-app.vercel.app,https://preview-your-app.vercel.app,http://localhost:5173
cors.allowed.origins=${frontend.urls}
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
cors.allowed.headers=*
cors.allow.credentials=true
```

Implementar en `SecurityConfig.java`:

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            String[] allowedOrigins = environment.getProperty("cors.allowed.origins", "").split(",");
            registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        }
    };
}
```

## Paso 6: Configurar Reescrituras de URLs (si es necesario)

En `vercel.json`, agregar reescrituras para APIs:

```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://embedded-payments-1.onrender.com/api/:path*"
    }
  ]
}
```

O en frontend `src/services/api.ts`, usar la URL completa:

```typescript
const apiBaseUrl = import.meta.env.VITE_API_URL?.trim() || 
                   'https://embedded-payments-1.onrender.com'
```

## Paso 7: Frontend Build Configuration

Verificar `frontend/payment-gateway-ui/package.json`:

```json
{
  "scripts": {
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  }
}
```

## Paso 8: Deploy

### Primer deploy:
```bash
vercel --prod
```

### Deploys automáticos:
- Cualquier push a `main` → Producción
- Cualquier push a otra rama → Preview

## Paso 9: Verificar Configuración

### URL del Frontend:
```
https://your-app.vercel.app
https://preview-your-app.vercel.app
```

### URLs del Backend (Render):
```
https://embedded-payments-1.onrender.com
API: https://embedded-payments-1.onrender.com/api/v1/*
```

## Paso 10: Testing Post-Deploy

```bash
# Test de conectividad
curl https://embedded-payments-1.onrender.com/api/v1/health

# Test de CORS
curl -H "Origin: https://your-app.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     https://embedded-payments-1.onrender.com/api/v1/health
```

## Troubleshooting

### Error: CORS policy blocked

**Solución:** Verificar en Render que CORS esté configurado:

```bash
# En application.properties (Render)
server.servlet.context-path=/
spring.web.cors.allowed-origins=https://your-app.vercel.app
```

### Error: API no responde

1. Verificar que Render backend está corriendo
2. Verificar VITE_API_URL en Vercel ambiente variables
3. Revisar logs en Vercel: `Deployments → Logs`

### Error: Build falla

```bash
# Revisar logs localmente
npm run build

# Limpiar cache
rm -rf dist node_modules
npm install
npm run build
```

## Monitoreo

### Vercel Analytics:
- Dashboard → Analytics
- Monitorar performance, Core Web Vitals

### Render Logs:
- Dashboard → selected service → Logs
- Verificar errores del backend

## Variables de Producción Final

Para producción completa, asegurar:

```env
# Vercel
VITE_API_URL=https://embedded-payments-1.onrender.com
VITE_FALLBACK_API_URL=https://embedded-payments-1.onrender.com
NODE_ENV=production

# Render (backend)
SPRING_PROFILES_ACTIVE=production
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

---

**Resumen de URLs:**
- 🌐 Frontend: `https://your-app.vercel.app`
- 🔌 Backend: `https://embedded-payments-1.onrender.com`
- 📱 API Base: `https://embedded-payments-1.onrender.com/api/v1`

