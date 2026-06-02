# RESUMEN: Configuración de Vercel para Frontend + Render Backend

## ¿QUÉ SE CONFIGURÓ?

He preparado tu repositorio con toda la configuración necesaria para desplegar en Vercel:

### Archivos creados/modificados:

1. **vercel.json** - Configuración principal de Vercel
   - Build commands
   - API rewrites (redirige requests a Render)
   - Headers CORS

2. **.vercelignore** - Optimiza el build
   - Excluye src/, db/, docs/
   - Solo sube frontend

3. **.env.production** - Variables para producción
   - VITE_API_URL = https://embedded-payments-1.onrender.com
   - VITE_FALLBACK_API_URL = https://embedded-payments-1.onrender.com

4. **VERCEL_DEPLOYMENT.md** - Guía completa en docs/


## PASOS PARA DESPLEGAR:

### Paso 1: Registrarse en Vercel
- Ir a https://vercel.com
- Click "Sign Up" → seleccionar GitHub
- Autorizar acceso al repositorio

### Paso 2: Crear proyecto en Vercel
- Dashboard → "Add new" → "Project"
- Seleccionar: embedded-payments (tu repo)
- Framework: Vue.js
- Root Directory: ./frontend/payment-gateway-ui
- Click "Deploy"

### Paso 3: Configurar variables de entorno
En el dashboard de Vercel:
- Settings → Environment Variables
- Agregar estas variables para PRODUCTION:

  VITE_API_URL = https://embedded-payments-1.onrender.com
  VITE_FALLBACK_API_URL = https://embedded-payments-1.onrender.com
  VITE_APP_NAME = Payment Gateway
  VITE_LOG_LEVEL = error

### Paso 4: Esperar deployment
- Vercel va a compilar (2-3 minutos)
- Se asignará una URL automáticamente: https://[nombre]-vercel.app

### Paso 5: Habilitar despliegues automáticos
Vercel automatiza esto por defecto:
- main → Producción
- develop → Preview
- Otros branches → Preview


## FLUJO DE CONEXION:

1. Usuario accede → https://[tu-app].vercel.app
2. Frontend Vue.js hace request a /api/v1/...
3. vercel.json reescribe → https://embedded-payments-1.onrender.com/api/v1/...
4. Backend responde desde Render


## VERIFICAR DESPUÉS DEL DEPLOY:

1. Visitar https://[tu-app].vercel.app
2. Hacer login
3. Revisar que no hay errores de CORS en la consola
4. Probar endpoints:
   - POST /api/v1/auth/login
   - GET /api/v1/transactions
   - POST /api/v1/payments

Logs:
- Vercel: Dashboard → Deployments → Logs
- Render: Dashboard → Service → Logs


## SI NO FUNCIONA:

### Error CORS:
- Verificar CORS en backend (application.properties)
- Render debería permitir tu dominio de Vercel

### API no responde:
- Verificar que Render backend está corriendo
- Revisar that VITE_API_URL está correcta en Vercel env vars

### Build falla:
- npm run build (ejecutar localmente)
- Revisar logs en Vercel después de desplegar


## URLS FINALES:

Frontend: https://[tu-app].vercel.app
API Backend: https://embedded-payments-1.onrender.com/api/v1
Base URL: https://embedded-payments-1.onrender.com


## NEXT STEPS:

1. Commit y push (ya está hecho: git push origin develop)
2. Ir a https://vercel.com y conectar
3. Seleccionar proyecto (embedded-payments)
4. Esperar deployment
5. Configurar variables en Vercel dashboard
6. Hacer redeploy desde dashboard

¡LISTO! El frontend se desplegará automáticamente cuando hagas push a main/develop.

