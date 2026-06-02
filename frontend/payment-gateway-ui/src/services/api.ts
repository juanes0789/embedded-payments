import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'

const apiBaseUrl = import.meta.env.VITE_API_URL?.trim() || ''
const fallbackApiBaseUrl = import.meta.env.VITE_FALLBACK_API_URL?.trim() || 'https://embedded-payments-1.onrender.com'

let fallbackClient: AxiosInstance | null = null

function createApiClient(baseURL: string, enableFallbackRetry = false): AxiosInstance {
  const client: AxiosInstance = axios.create({
    baseURL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  client.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const authStore = useAuthStore()
      // Ensure headers object exists (some axios internals may replace it)
      if (!config.headers) {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        config.headers = {}
      }

      if (authStore.token) {
        config.headers.Authorization = `Bearer ${authStore.token}`
        // Log for debugging: token present and header set (partial token shown)
        try {
          const short = authStore.token.length > 8 ? authStore.token.substring(0, 8) + '...' : authStore.token
          // eslint-disable-next-line no-console
          console.debug('[api] Setting Authorization header:', `Bearer ${short}`)
        } catch (e) {
          // ignore
        }
      } else {
        // eslint-disable-next-line no-console
        console.debug('[api] No auth token present for request', config.url)
      }

      try {
        const apiKey = localStorage.getItem('apiKey')
        const requestUrl = (config.url || '').toString()
        if (
          apiKey &&
          (requestUrl.includes('/api/v1/payments') ||
            requestUrl.includes('/api/v1/transactions') ||
            requestUrl.includes('/api/v1/refunds') ||
            requestUrl.includes('/api/v1/admin/payments'))
        ) {
          config.headers['X-API-Key'] = apiKey
          // eslint-disable-next-line no-console
          console.debug('[api] X-API-Key header set for', requestUrl)
        }
      } catch (_e) {
        // ignore storage errors
      }

      return config
    },
    (error) => Promise.reject(error)
  )

  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const notificationStore = useNotificationStore()
      const requestUrl = (error.config as any)?.url || ''
      const isAuthLoginRequest = requestUrl.includes('/api/v1/auth/login')

      if (error.response) {
        const status = error.response.status
        if (status === 401 && !isAuthLoginRequest) {
          const authStore = useAuthStore()
          authStore.logout()
          window.location.href = '/login'
          return Promise.reject(error)
        }

        if (status === 403) {
          notificationStore.error('Acceso denegado (403). Verifica tu API Key o permisos.')
        }
        if (status >= 500) {
          notificationStore.error('Error del servidor. Intenta nuevamente más tarde.')
        }

        return Promise.reject(error)
      }

      // Network / CORS / backend unavailable.
      // If the primary URL is not reachable, retry once against the fallback remote backend.
      if (enableFallbackRetry && baseURL !== fallbackApiBaseUrl) {
        try {
          if (!fallbackClient) {
            fallbackClient = createApiClient(fallbackApiBaseUrl, false)
          }
          const retryConfig = {
            ...error.config,
            baseURL: fallbackApiBaseUrl,
          }
          return await fallbackClient.request(retryConfig)
        } catch (retryError) {
          notificationStore.error('No se pudo conectar con el backend local ni con el remoto de respaldo.')
          return Promise.reject(retryError)
        }
      }

      notificationStore.error('No se pudo conectar con la API. Revisa que el backend esté levantado y la URL sea correcta.')
      return Promise.reject(error)
    }
  )

  return client
}

const instance = createApiClient(apiBaseUrl, true)

export default instance
