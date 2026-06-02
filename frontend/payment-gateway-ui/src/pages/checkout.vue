<template>
  <div class="checkout-shell">
    <div class="mx-auto flex min-h-screen w-full max-w-7xl flex-col px-4 py-6 sm:px-6 lg:px-8">
      <!-- Loading state -->
      <div v-if="isLoading" class="flex items-center justify-center min-h-screen">
        <div class="text-center">
          <div class="animate-spin h-12 w-12 border-4 border-slate-200 border-t-slate-950 rounded-full mx-auto mb-4"></div>
          <p class="text-slate-600">Loading payment details...</p>
        </div>
      </div>

      <!-- Error state -->
      <div v-else-if="!paymentIntent && errorMessage" class="flex items-center justify-center min-h-screen">
        <div class="max-w-md mx-auto">
          <div class="rounded-lg border border-rose-200 bg-rose-50 p-6 text-center">
            <CircleCheckBig class="h-12 w-12 text-rose-600 mx-auto mb-4" :style="{transform: 'rotate(45deg)'}" />
            <p class="text-rose-900 font-semibold mb-2">Error Loading Payment</p>
            <p class="text-rose-800 text-sm">{{ errorMessage }}</p>
          </div>
        </div>
      </div>

      <!-- Main checkout content -->
      <template v-else-if="paymentIntent">
      <header class="mb-6 flex items-center justify-between rounded-3xl border border-white/10 bg-white/5 px-5 py-4 text-white shadow-[0_20px_60px_-30px_rgba(15,23,42,0.65)] backdrop-blur">
        <div class="flex items-center gap-4">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-slate-950 shadow-lg shadow-black/20">
            <span class="text-sm font-black tracking-tight">EP</span>
          </div>
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.28em] text-white/60">Embedded Payments</p>
            <h1 class="text-lg font-semibold leading-tight">Secure checkout for {{ merchantName }}</h1>
          </div>
        </div>

        <div class="hidden items-center gap-3 md:flex">
          <span class="checkout-chip">
            <ShieldCheck class="h-4 w-4" />
            Encrypted session
          </span>
          <span class="checkout-chip">
            <LockKeyhole class="h-4 w-4" />
            Protected payment
          </span>
        </div>
      </header>

      <main class="grid flex-1 gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <section class="flex flex-col gap-6 text-white">
          <div class="rounded-4xl border border-white/10 bg-white/5 p-6 shadow-[0_30px_90px_-40px_rgba(15,23,42,0.8)] backdrop-blur">
            <div class="mb-6 flex flex-wrap items-center gap-3">
              <span class="inline-flex items-center rounded-full bg-emerald-400/15 px-3 py-1 text-xs font-semibold text-emerald-200 ring-1 ring-inset ring-emerald-400/25">
                <CircleCheckBig class="mr-2 h-4 w-4" />
                Checkout ready
              </span>
              <span class="inline-flex items-center rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-white/80 ring-1 ring-inset ring-white/10">
                {{ sessionLabel }}
              </span>
            </div>

            <div class="space-y-4">
              <p class="text-sm font-semibold uppercase tracking-[0.24em] text-white/55">Payment summary</p>
              <h2 class="max-w-xl text-4xl font-semibold tracking-tight sm:text-5xl">
                Pay in a clean, secure flow that feels familiar and fast.
              </h2>
              <p class="max-w-2xl text-base leading-7 text-white/75 sm:text-lg">
                Review your order, enter card details, and complete the payment in a few seconds.
                This experience is optimized for confidence, clarity, and conversion.
              </p>
            </div>

            <div class="mt-8 grid gap-4 sm:grid-cols-3">
              <article class="rounded-2xl border border-white/10 bg-white/8 p-4">
                <p class="text-xs font-semibold uppercase tracking-[0.2em] text-white/50">Merchant</p>
                <p class="mt-2 text-sm font-semibold text-white">{{ merchantName }}</p>
                <p class="mt-1 text-xs text-white/60">{{ description }}</p>
              </article>
              <article class="rounded-2xl border border-white/10 bg-white/8 p-4">
                <p class="text-xs font-semibold uppercase tracking-[0.2em] text-white/50">Amount</p>
                <p class="mt-2 text-sm font-semibold text-white">{{ formattedTotal }}</p>
                <p class="mt-1 text-xs text-white/60">Includes processing fee</p>
              </article>
              <article class="rounded-2xl border border-white/10 bg-white/8 p-4">
                <p class="text-xs font-semibold uppercase tracking-[0.2em] text-white/50">Support</p>
                <p class="mt-2 text-sm font-semibold text-white">Receipt via email</p>
                <p class="mt-1 text-xs text-white/60">Delivered instantly after payment</p>
              </article>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <article class="rounded-[1.75rem] border border-white/10 bg-white/5 p-5 text-white backdrop-blur">
              <div class="flex items-center gap-3">
                <div class="rounded-2xl bg-white/10 p-3">
                  <CreditCard class="h-5 w-5" />
                </div>
                <div>
                  <h3 class="font-semibold">Card supported</h3>
                  <p class="text-sm text-white/65">Visa, Mastercard, Amex, and more</p>
                </div>
              </div>
            </article>
            <article class="rounded-[1.75rem] border border-white/10 bg-white/5 p-5 text-white backdrop-blur">
              <div class="flex items-center gap-3">
                <div class="rounded-2xl bg-white/10 p-3">
                  <Sparkles class="h-5 w-5" />
                </div>
                <div>
                  <h3 class="font-semibold">Fast confirmation</h3>
                  <p class="text-sm text-white/65">See your receipt right away</p>
                </div>
              </div>
            </article>
          </div>
        </section>

        <aside class="checkout-panel overflow-hidden">
          <div class="border-b border-slate-200/80 bg-slate-50 px-6 py-5">
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">Secure payment</p>
                <h2 class="mt-1 text-2xl font-semibold text-slate-900">Complete your purchase</h2>
                <p class="mt-2 text-sm leading-6 text-slate-600">Review the details below and confirm the card payment.</p>
              </div>
              <div class="rounded-2xl bg-emerald-50 px-3 py-2 text-emerald-700 ring-1 ring-inset ring-emerald-200">
                <ShieldCheck class="h-5 w-5" />
              </div>
            </div>

            <div class="mt-5 grid grid-cols-3 gap-2 text-xs font-semibold text-slate-500">
              <div :class="stepClass(1)">1. Details</div>
              <div :class="stepClass(2)">2. Review</div>
              <div :class="stepClass(3)">3. Paid</div>
            </div>
          </div>

          <div class="p-6">
            <div v-if="paymentCompleted" class="space-y-6">
              <div class="rounded-3xl border border-emerald-200 bg-emerald-50 p-6 text-emerald-950">
                <div class="flex items-start gap-4">
                  <div class="rounded-2xl bg-white p-3 text-emerald-600 shadow-sm">
                    <CircleCheckBig class="h-6 w-6" />
                  </div>
                  <div>
                    <p class="text-sm font-semibold uppercase tracking-[0.24em] text-emerald-700">Payment successful</p>
                    <h3 class="mt-1 text-2xl font-semibold">Thank you, {{ form.cardholderName || 'customer' }}.</h3>
                    <p class="mt-2 text-sm leading-6 text-emerald-900/80">
                      Your payment of {{ formattedTotal }} has been confirmed and a receipt was sent to {{ form.email }}.
                    </p>
                  </div>
                </div>
              </div>

              <div class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                <p class="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">Receipt</p>
                <div class="mt-4 space-y-3 text-sm text-slate-600">
                  <div class="flex items-center justify-between gap-4">
                    <span>Reference</span>
                    <span class="font-mono text-slate-900">{{ receipt.reference }}</span>
                  </div>
                  <div class="flex items-center justify-between gap-4">
                    <span>Merchant</span>
                    <span class="font-medium text-slate-900">{{ merchantName }}</span>
                  </div>
                  <div class="flex items-center justify-between gap-4">
                    <span>Card</span>
                    <span class="font-medium text-slate-900">•••• {{ receipt.last4 }}</span>
                  </div>
                  <div class="flex items-center justify-between gap-4">
                    <span>Paid at</span>
                    <span class="font-medium text-slate-900">{{ receipt.paidAt }}</span>
                  </div>
                </div>
              </div>

              <button class="btn-primary w-full" @click="resetCheckout">
                Start another payment
              </button>
            </div>

            <form v-else class="space-y-5" @submit.prevent="submitPayment">
              <div class="space-y-4">
                <div>
                  <label class="checkout-label" for="cardholderName">Cardholder name</label>
                  <input
                    id="cardholderName"
                    v-model="form.cardholderName"
                    class="field-input"
                    autocomplete="cc-name"
                    placeholder="Alex Johnson"
                    type="text"
                  />
                </div>

                <div>
                  <label class="checkout-label" for="cardNumber">Card number</label>
                  <input
                    id="cardNumber"
                    v-model="form.cardNumber"
                    class="field-input font-mono tracking-widest"
                    autocomplete="cc-number"
                    inputmode="numeric"
                    maxlength="19"
                    placeholder="4242 4242 4242 4242"
                    type="text"
                    @input="handleCardNumberInput"
                  />
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="checkout-label" for="expiry">Expiry</label>
                    <input
                      id="expiry"
                      v-model="form.expiry"
                      class="field-input font-mono"
                      autocomplete="cc-exp"
                      inputmode="numeric"
                      maxlength="5"
                      placeholder="MM/YY"
                      type="text"
                      @input="handleExpiryInput"
                    />
                  </div>
                  <div>
                    <label class="checkout-label" for="cvc">CVC</label>
                    <input
                      id="cvc"
                      v-model="form.cvc"
                      class="field-input font-mono"
                      autocomplete="cc-csc"
                      inputmode="numeric"
                      maxlength="4"
                      placeholder="123"
                      type="text"
                    />
                  </div>
                </div>

                <div>
                  <label class="checkout-label" for="email">Receipt email</label>
                  <input
                    id="email"
                    v-model="form.email"
                    class="field-input"
                    autocomplete="email"
                    placeholder="you@example.com"
                    type="email"
                  />
                </div>
              </div>

              <div class="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                <div class="flex items-center justify-between text-sm text-slate-600">
                  <span>Subtotal</span>
                  <span>{{ formattedSubtotal }}</span>
                </div>
                <div class="mt-2 flex items-center justify-between text-sm text-slate-600">
                  <span>Processing fee</span>
                  <span>{{ formattedFee }}</span>
                </div>
                <div class="mt-4 border-t border-slate-200 pt-4 flex items-center justify-between">
                  <span class="text-sm font-medium text-slate-700">Total</span>
                  <span class="text-2xl font-semibold tracking-tight text-slate-950">{{ formattedTotal }}</span>
                </div>
              </div>

              <p v-if="errorMessage" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
                {{ errorMessage }}
              </p>

              <button
                class="btn-primary w-full py-3 text-base"
                :disabled="isProcessing"
                type="submit"
              >
                <span v-if="isProcessing" class="flex items-center gap-2">
                  <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" class="opacity-20"></circle>
                    <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="4" stroke-linecap="round"></path>
                  </svg>
                  Processing payment...
                </span>
                <span v-else>Pay {{ formattedTotal }}</span>
              </button>

              <div class="flex items-center justify-center gap-3 text-xs text-slate-500">
                <span class="rounded-full bg-slate-100 px-3 py-1 font-medium text-slate-600">Card secured</span>
                <span class="rounded-full bg-slate-100 px-3 py-1 font-medium text-slate-600">Private checkout</span>
                <span class="rounded-full bg-slate-100 px-3 py-1 font-medium text-slate-600">Instant receipt</span>
              </div>
            </form>
          </div>
        </aside>
      </main>

      <footer class="mt-6 flex flex-col items-center justify-between gap-3 border-t border-white/10 px-2 py-4 text-xs text-white/55 sm:flex-row">
        <p>© {{ currentYear }} Embedded Payments. Built for a modern checkout experience.</p>
        <p>Secure by design · Responsive · Fast confirmation</p>
      </footer>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  CircleCheckBig,
  CreditCard,
  LockKeyhole,
  ShieldCheck,
  Sparkles,
} from 'lucide-vue-next'
import { getPaymentIntent, submitCheckoutPayment, type PaymentIntent, type CheckoutSubmitResponse } from '@/services/checkout'
import { useNotificationStore } from '@/stores/notifications'

const route = useRoute()
const notificationStore = useNotificationStore()

const currentYear = new Date().getFullYear()

// Loading states
const isLoading = ref(true)
const isProcessing = ref(false)
const paymentCompleted = ref(false)
const errorMessage = ref('')

// Payment intent data from backend
const paymentIntent = ref<PaymentIntent | null>(null)
const transactionData = ref<CheckoutSubmitResponse | null>(null)

// Computed properties for display
const merchantName = computed(() => paymentIntent.value?.merchantName || 'Merchant')
const description = computed(() => paymentIntent.value?.description || 'One-time secure payment')
const sessionLabel = computed(() => route.params.checkoutId ? `Order ${String(route.params.checkoutId).substring(0, 8)}...` : 'Checkout')

const subtotal = computed(() => paymentIntent.value?.amount || 0)
const currency = computed(() => paymentIntent.value?.currency?.toUpperCase() || 'USD')
const processingFee = computed(() => subtotal.value * 0.029 + 0.3)
const total = computed(() => subtotal.value + processingFee.value)

const formattedSubtotal = computed(() => formatMoney(subtotal.value, currency.value))
const formattedFee = computed(() => formatMoney(processingFee.value, currency.value))
const formattedTotal = computed(() => formatMoney(total.value, currency.value))

const form = reactive({
  cardholderName: '',
  cardNumber: '',
  expiry: '',
  cvc: '',
  email: '',
})

const receipt = reactive({
  reference: '',
  last4: '4242',
  paidAt: '',
})

// Utility functions
function formatMoney(amount: number, code: string) {
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: code,
      maximumFractionDigits: 2,
    }).format(amount)
  } catch {
    return `${code} ${amount.toFixed(2)}`
  }
}

function handleCardNumberInput(event: Event) {
  const target = event.target as HTMLInputElement
  const digits = target.value.replace(/\D/g, '').slice(0, 16)
  const groups = digits.match(/.{1,4}/g) ?? []
  form.cardNumber = groups.join(' ')
}

function handleExpiryInput(event: Event) {
  const target = event.target as HTMLInputElement
  const digits = target.value.replace(/\D/g, '').slice(0, 4)
  if (digits.length <= 2) {
    form.expiry = digits
    return
  }
  form.expiry = `${digits.slice(0, 2)}/${digits.slice(2)}`
}

function stepClass(step: number) {
  const activeStep = paymentCompleted.value ? 3 : isProcessing.value ? 2 : 1
  return [
    'rounded-full px-3 py-2 text-center transition',
    step === activeStep ? 'bg-slate-950 text-white shadow-sm' : 'bg-slate-100 text-slate-500',
  ]
}

function validateForm() {
  if (!form.cardholderName.trim()) return 'Cardholder name is required'
  if (!/^\d{4} \d{4} \d{4} \d{4}$/.test(form.cardNumber)) return 'Card number must have 16 digits'
  if (!/^\d{2}\/\d{2}$/.test(form.expiry)) return 'Expiry must be in MM/YY format'
  if (!/^\d{3,4}$/.test(form.cvc)) return 'CVC must contain 3 or 4 digits'
  if (!form.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Enter a valid email address'
  return ''
}

async function loadPaymentIntent() {
  if (!route.params.checkoutId) {
    errorMessage.value = 'No payment intent specified. Please use a valid checkout link.'
    isLoading.value = false
    return
  }

  try {
    const intent = await getPaymentIntent(route.params.checkoutId as string)
    paymentIntent.value = intent
    notificationStore.success(`Payment intent loaded: ${formatMoney(intent.amount, intent.currency)}`)
  } catch (error: any) {
    const status = error.response?.status
    if (status === 404) {
      errorMessage.value = 'Payment intent not found. Please check the checkout link.'
    } else if (status === 410) {
      errorMessage.value = 'This payment intent is no longer available.'
    } else {
      errorMessage.value = 'Failed to load payment intent. Please try again.'
    }
    notificationStore.error(errorMessage.value)
  } finally {
    isLoading.value = false
  }
}

async function submitPayment() {
  errorMessage.value = validateForm()
  if (errorMessage.value) return

  if (!paymentIntent.value) {
    errorMessage.value = 'Payment intent not loaded'
    return
  }

  isProcessing.value = true
  errorMessage.value = ''

  try {
    const response = await submitCheckoutPayment({
      checkoutId: paymentIntent.value.id,
      customerEmail: form.email,
      customerName: form.cardholderName,
    })

    transactionData.value = response

    if (response.status === 'SUCCEEDED') {
      paymentCompleted.value = true
      // Use backend-generated references instead of local mock references.
      receipt.reference = response.processorReference || response.transactionId
      receipt.last4 = form.cardNumber.replace(/\s/g, '').slice(-4)
      receipt.paidAt = new Intl.DateTimeFormat('en-US', {
        dateStyle: 'medium',
        timeStyle: 'short',
      }).format(new Date())
      notificationStore.success(`Payment successful! Transaction ID: ${response.transactionId}`)
    } else {
      errorMessage.value = 'Payment processing failed. Please try again.'
      notificationStore.error(errorMessage.value)
    }
  } catch (error: any) {
    const status = error.response?.status
    const errorCode = error.response?.data?.error

    if (status === 409) {
      errorMessage.value = 'A transaction is already in progress for this intent.'
    } else if (status === 422) {
      errorMessage.value = 'This payment intent cannot be processed.'
    } else if (status === 500) {
      errorMessage.value = 'Payment processing error. Please try again.'
    } else {
      errorMessage.value = 'We could not complete the payment. Please try again.'
    }
    notificationStore.error(errorMessage.value)
  } finally {
    isProcessing.value = false
  }
}

function resetCheckout() {
  paymentCompleted.value = false
  errorMessage.value = ''
  form.cardholderName = ''
  form.cardNumber = ''
  form.expiry = ''
  form.cvc = ''
  form.email = ''
}

// Load payment intent on component mount
onMounted(() => {
  loadPaymentIntent()
})
</script>



