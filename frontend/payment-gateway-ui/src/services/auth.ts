import api from './api'
import { LoginResponse, RegisterRequest, RegisterResponse, User } from '@/types'

export const authService = {
  login: async (email: string, password: string): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/api/v1/auth/login', {
      email,
      password,
    })
    return response.data
  },

  register: async (payload: RegisterRequest): Promise<RegisterResponse> => {
    const response = await api.post<RegisterResponse>('/api/v1/auth/register', payload)
    return response.data
  },

  logout: async (): Promise<void> => {
    try {
      await api.post('/api/v1/auth/logout')
    } catch (error) {
      console.error('Logout error:', error)
    }
  },

  refreshToken: async (refreshToken: string): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/api/v1/auth/refresh', {
      refreshToken,
    })
    return response.data
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<User>('/api/v1/auth/me')
    return response.data
  },
}
