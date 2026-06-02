<template>
  <AppLayout>
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <router-link to="/dashboard" class="text-indigo-600 hover:text-indigo-900 flex items-center gap-2 mb-4">
          ← Back to Dashboard
        </router-link>
        <h1 class="text-3xl font-bold text-gray-900">Create Payment Order</h1>
        <p class="text-gray-600 mt-2">Generate a new payment order and share a link with your customers</p>
      </div>

      <!-- Main Content -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Form Section -->
        <div class="lg:col-span-2">
          <div class="bg-white rounded-lg shadow p-6">
            <form @submit.prevent="submitForm" class="space-y-6">
              <!-- Amount -->
              <div>
                <label for="amount" class="block text-sm font-medium text-gray-700 mb-2">
                  Amount
                </label>
                <div class="relative">
                  <input
                    id="amount"
                    v-model.number="form.amount"
                    type="number"
                    step="0.01"
                    min="0.01"
                    placeholder="99.99"
                    class="block w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 text-gray-900"
                    required
                  />
                </div>
                <p class="mt-1 text-sm text-gray-600">Minimum: $0.01</p>
              </div>

              <!-- Currency -->
              <div>
                <label for="currency" class="block text-sm font-medium text-gray-700 mb-2">
                  Currency
                </label>
                <select
                  id="currency"
                  v-model="form.currency"
                  class="block w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 text-gray-900"
                  required
                >
                  <option value="USD">USD (US Dollar)</option>
                  <option value="EUR">EUR (Euro)</option>
                  <option value="GBP">GBP (British Pound)</option>
                  <option value="MXN">MXN (Mexican Peso)</option>
                  <option value="COP">COP (Colombian Peso)</option>
                </select>
              </div>

              <!-- Description -->
              <div>
                <label for="description" class="block text-sm font-medium text-gray-700 mb-2">
                  Order Description (Optional)
                </label>
                <textarea
                  id="description"
                  v-model="form.description"
                  rows="3"
                  placeholder="e.g., Order #12345, Premium subscription, Service delivery..."
                  class="block w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 text-gray-900"
                />
                <p class="mt-1 text-sm text-gray-600">This will be shown to customers on the checkout page</p>
              </div>

              <!-- Error Message -->
              <div v-if="errorMessage" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                {{ errorMessage }}
              </div>

              <!-- Submit Button -->
              <button
                type="submit"
                :disabled="isSubmitting"
                class="w-full px-4 py-3 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                <span v-if="isSubmitting" class="flex items-center justify-center gap-2">
                  <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" class="opacity-20"></circle>
                    <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="4" stroke-linecap="round"></path>
                  </svg>
                  Creating order...
                </span>
                <span v-else>Create Payment Order</span>
              </button>
            </form>
          </div>
        </div>

        <!-- Preview Section -->
        <div class="space-y-6">
          <!-- Order Summary -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">Order Summary</h3>
            <div class="space-y-4">
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">Amount</span>
                <span class="font-semibold text-gray-900">{{ form.currency }} {{ form.amount?.toFixed(2) || '0.00' }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">Currency</span>
                <span class="font-semibold text-gray-900">{{ form.currency }}</span>
              </div>
              <div v-if="form.description" class="pt-4 border-t">
                <p class="text-xs text-gray-600 mb-1">Description</p>
                <p class="text-sm text-gray-900">{{ form.description }}</p>
              </div>
              <div class="pt-4 border-t">
                <p class="text-xs text-gray-600 mb-1">Status</p>
                <span class="inline-block px-3 py-1 bg-yellow-100 text-yellow-800 text-sm rounded-full">
                  Will be: CREATED
                </span>
              </div>
            </div>
          </div>

          <!-- Features List -->
          <div class="bg-indigo-50 rounded-lg p-6 border border-indigo-200">
            <h3 class="font-semibold text-indigo-900 mb-3">What happens next</h3>
            <ul class="space-y-2 text-sm text-indigo-800">
              <li class="flex items-start gap-2">
                <span class="text-indigo-600 font-bold">1.</span>
                <span>Order is created and gets a unique link</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-indigo-600 font-bold">2.</span>
                <span>Share the link with your customer</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-indigo-600 font-bold">3.</span>
                <span>Customer opens link and enters info</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-indigo-600 font-bold">4.</span>
                <span>Payment is processed automatically</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-indigo-600 font-bold">5.</span>
                <span>Transaction appears in your dashboard</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Success Modal -->
      <div v-if="showSuccessModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-lg shadow-lg max-w-md w-full p-6 animate-in fade-in zoom-in-95">
          <!-- Success Icon -->
          <div class="flex justify-center mb-4">
            <div class="rounded-full bg-green-100 p-3">
              <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>

          <h3 class="text-xl font-semibold text-gray-900 text-center mb-2">
            Payment Order Created!
          </h3>
          <p class="text-gray-600 text-center text-sm mb-6">
            Your payment order has been created successfully. Share the link below with your customer.
          </p>

          <!-- Order ID -->
          <div class="bg-gray-50 rounded-lg p-4 mb-4">
            <p class="text-xs text-gray-600 mb-1">Order ID</p>
            <p class="font-mono text-sm text-gray-900 break-all">{{ createdIntentId }}</p>
          </div>

          <!-- Checkout Link -->
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <p class="text-xs text-blue-600 mb-2">Payment Link</p>
            <div class="flex items-center gap-2">
              <input
                type="text"
                :value="checkoutLink"
                readonly
                class="flex-1 px-3 py-2 bg-white border border-blue-200 rounded text-sm text-gray-900 font-mono"
              />
              <button
                @click="copyCheckoutLink"
                class="px-3 py-2 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 transition"
              >
                {{ copySuccess ? '✓' : 'Copy' }}
              </button>
            </div>
          </div>

          <!-- Amount Confirmation -->
          <div class="bg-gray-50 rounded-lg p-4 mb-6 text-center">
            <p class="text-gray-600 text-sm">Order Amount</p>
            <p class="text-2xl font-bold text-gray-900">{{ form.currency }} {{ form.amount?.toFixed(2) }}</p>
          </div>

          <!-- Action Buttons -->
          <div class="flex gap-3">
            <button
              @click="goToDashboard"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-900 hover:bg-gray-50 transition"
            >
              Go to Dashboard
            </button>
            <button
              @click="createAnother"
              class="flex-1 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
            >
              Create Another
            </button>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import { createPaymentIntent, generateCheckoutLink, type CreatePaymentIntentRequest } from '@/services/checkout'
import { useNotificationStore } from '@/stores/notifications'

const router = useRouter()
const notificationStore = useNotificationStore()

const form = ref<CreatePaymentIntentRequest>({
  amount: 99.99,
  currency: 'USD',
  description: '',
})

const isSubmitting = ref(false)
const errorMessage = ref('')
const showSuccessModal = ref(false)
const createdIntentId = ref('')
const copySuccess = ref(false)

const checkoutLink = computed(() => {
  return generateCheckoutLink(createdIntentId.value)
})

async function submitForm() {
  errorMessage.value = ''
  isSubmitting.value = true

  try {
    if (!form.value.amount || form.value.amount < 0.01) {
      throw new Error('Amount must be at least $0.01')
    }

    const intent = await createPaymentIntent({
      amount: form.value.amount,
      currency: form.value.currency,
      description: form.value.description,
    })

    createdIntentId.value = intent.id
    showSuccessModal.value = true
    notificationStore.success(`Payment order created: ${form.value.currency} ${form.value.amount.toFixed(2)}`)
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || error.message || 'Failed to create payment order'
    notificationStore.error(errorMessage.value)
  } finally {
    isSubmitting.value = false
  }
}

async function copyCheckoutLink() {
  try {
    await navigator.clipboard.writeText(checkoutLink.value)
    copySuccess.value = true
    notificationStore.success('Link copied to clipboard')
    setTimeout(() => {
      copySuccess.value = false
    }, 2000)
  } catch {
    notificationStore.error('Failed to copy link')
  }
}

function createAnother() {
  form.value = {
    amount: 99.99,
    currency: 'USD',
    description: '',
  }
  showSuccessModal.value = false
  createdIntentId.value = ''
  errorMessage.value = ''
}

function goToDashboard() {
  router.push('/dashboard')
}
</script>

