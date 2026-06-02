import instance from './api'

export interface PaymentIntent {
  id: string
  merchantName?: string
  amount: number
  currency: string
  status: string
  description?: string
  createdAt: string
  updatedAt: string
}

export interface CreatePaymentIntentRequest {
  amount: number
  currency: string
  description?: string
}

export interface CheckoutSubmitRequest {
  checkoutId: string
  customerEmail: string
  customerName: string
}

export interface CheckoutSubmitResponse {
  transactionId: string
  status: string
  processorReference?: string
}

/**
 * Create a new payment intent from merchant dashboard (JWT)
 * @param request - Payment intent creation data
 */
export async function createPaymentIntent(request: CreatePaymentIntentRequest): Promise<PaymentIntent> {
  const response = await instance.post<PaymentIntent>('/api/v1/admin/payments/intents', request)
  return response.data
}

/**
 * Fetch public payment intent information for checkout display
 * @param intentId - Payment intent ID (UUID)
 */
export async function getPaymentIntent(intentId: string): Promise<PaymentIntent> {
  const response = await instance.get<PaymentIntent>(`/checkout/intents/${intentId}`)
  return response.data
}

/**
 * Submit payment and create transaction
 * @param request - Checkout submission data with customer info
 */
export async function submitCheckoutPayment(request: CheckoutSubmitRequest): Promise<CheckoutSubmitResponse> {
  const response = await instance.post<CheckoutSubmitResponse>('/checkout/submit', request)
  return response.data
}

/**
 * Create a unique idempotency key for payment submission
 */
export function generateIdempotencyKey(): string {
  return `checkout_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
}

/**
 * Generate a shareable checkout link for a payment intent
 */
export function generateCheckoutLink(intentId: string, baseUrl: string = window.location.origin): string {
  return `${baseUrl}/pay/${intentId}`
}


