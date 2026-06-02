import api from './api'
import { Transaction, Refund, PaginatedResponse } from '@/types'

export const transactionService = {
  list: async (page = 1, pageSize = 10, status?: string): Promise<PaginatedResponse<Transaction>> => {
    const params: Record<string, any> = { page, pageSize }
    if (status && status !== 'ALL') {
      params.status = status
    }
    const response = await api.get<PaginatedResponse<Transaction>>(
      '/api/v1/transactions',
      { params }
    )
    return response.data
  },

  getById: async (id: string): Promise<Transaction> => {
    const response = await api.get<Transaction>(`/api/v1/transactions/${id}`)
    return response.data
  },

  refund: async (
    transactionId: string,
    amount: number,
    reason: string
  ): Promise<Refund> => {
    const response = await api.post<Refund>('/api/v1/refunds', {
      transactionId,
      amount,
      reason,
    })
    return response.data
  },
}

