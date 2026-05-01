import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { User, LoginResponse } from '@/types'
import { authService } from '@/services/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const user = ref<User | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN')
  const isMerchant = computed(() => user.value?.role === 'ROLE_MERCHANT')

  async function login(email: string, password: string) {
    isLoading.value = true
    error.value = null
    try {
      const response = await authService.login(email, password)
      token.value = response.token
      refreshToken.value = response.refreshToken
      user.value = response.user
      localStorage.setItem('token', response.token)
      localStorage.setItem('refreshToken', response.refreshToken)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Login failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  function logout() {
    token.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    authService.logout()
  }

  return {
    token,
    refreshToken,
    user,
    isLoading,
    error,
    isAuthenticated,
    isAdmin,
    isMerchant,
    login,
    logout,
  }
})

