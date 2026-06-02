<template>
  <AppLayout>
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Payment Orders</h1>
          <p class="text-sm text-gray-600 mt-1">Lista de órdenes de pago creadas. Controla links y estado hasta que sean exitosas.</p>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Order ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Checkout Link</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-if="isLoading">
              <td colspan="5" class="px-6 py-4">Loading...</td>
            </tr>
            <tr v-else-if="orders.length === 0">
              <td colspan="5" class="px-6 py-4">No payment orders found.</td>
            </tr>
            <tr v-for="order in orders" :key="order.id">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-mono">{{ order.id }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ order.currency }} {{ order.amount.toFixed(2) }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span :class="statusClass(order.status)">{{ order.status }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <input class="flex-1 px-2 py-1 text-xs font-mono border rounded bg-gray-50" :value="checkoutLink(order.id)" readonly />
                  <button @click="copyLink(order.id)" class="px-2 py-1 bg-blue-600 text-white rounded text-xs">{{ copied === order.id ? '✓' : 'Copy' }}</button>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                <button v-if="order.transactionId" @click="goToTransaction(order.transactionId)" class="px-3 py-1 bg-green-600 text-white rounded text-sm">View Transaction</button>
                <button v-else-if="order.status === 'SUCCEEDED'" @click="goToTransactionsList" class="px-3 py-1 bg-indigo-600 text-white rounded text-sm">Transactions</button>
                <button v-else @click="openInNewTab(order.id)" class="px-3 py-1 border rounded text-sm">Open Checkout</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { paymentsService, type PaymentOrder } from '@/services/payments'
import { generateCheckoutLink } from '@/services/checkout'
import { useRouter } from 'vue-router'

const orders = ref<PaymentOrder[]>([])
const isLoading = ref(false)
const copied = ref<string | null>(null)
const router = useRouter()

function checkoutLink(id: string) {
  return generateCheckoutLink(id)
}

function statusClass(status: string) {
  const s = (status || '').toUpperCase()
  if (s === 'SUCCEEDED' || s === 'COMPLETED') return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-green-100 text-green-800'
  if (s === 'PROCESSING' || s === 'PENDING') return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-yellow-100 text-yellow-800'
  if (s === 'FAILED' || s === 'CANCELED') return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-red-100 text-red-800'
  return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-gray-100 text-gray-800'
}

async function load() {
  isLoading.value = true
  try {
    orders.value = await paymentsService.listIntents()
  } catch (e: any) {
    console.error('failed to load orders', e)
  } finally {
    isLoading.value = false
  }
}

function copyLink(id: string) {
  const l = checkoutLink(id)
  navigator.clipboard.writeText(l).then(() => {
    copied.value = id
    setTimeout(() => (copied.value = null), 2000)
  })
}

function openInNewTab(id: string) {
  window.open(checkoutLink(id), '_blank')
}

function goToTransaction(transactionId: string) {
  router.push(`/transactions/${transactionId}`)
}

function goToTransactionsList() {
  router.push('/transactions')
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.font-mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, "Roboto Mono", "Courier New", monospace; }
</style>

