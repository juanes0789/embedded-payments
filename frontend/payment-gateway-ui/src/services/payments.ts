import api from './api'

export interface PaymentOrder {
  id: string
  merchantId?: string
  amount: number
  currency: string
  status: string
  description?: string
  createdAt: string
  updatedAt?: string
  transactionId?: string | null
}

export const paymentsService = {
  listIntents: async (): Promise<PaymentOrder[]> => {
    const response = await api.get<PaymentOrder[]>('/api/v1/admin/payments/intents')
    return response.data
  },
}

