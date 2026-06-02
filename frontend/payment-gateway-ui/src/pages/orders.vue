<template>
  <AppLayout>
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Payment Orders</h1>
          <p class="text-sm text-gray-600 mt-1">Lista de órdenes de pago creadas. Controla links y estado hasta que sean exitosas.</p>
        </div>
      </div>

      <!-- Filters -->
      <div class="mb-4 flex items-center gap-3">
        <div class="flex items-center space-x-2">
          <label class="text-sm text-gray-600">Status</label>
          <select v-model="filterStatus" class="px-2 py-1 border rounded bg-white text-sm">
            <option value="">All</option>
            <option value="CREATED">CREATED</option>
            <option value="SUCCEEDED">SUCCEEDED</option>
            <option value="FAILED">FAILED</option>
          </select>
        </div>

        <div class="flex items-center space-x-2">
          <label class="text-sm text-gray-600">From</label>
          <DatePicker v-model="filterFrom" />
        </div>

        <div class="flex items-center space-x-2">
          <label class="text-sm text-gray-600">To</label>
          <DatePicker v-model="filterTo" />
        </div>

        <div class="ml-auto flex items-center gap-2">
          <button @click="applyFilters" class="px-3 py-1 bg-blue-600 text-white rounded text-sm">Apply</button>
          <button @click="clearFilters" class="px-3 py-1 border rounded text-sm">Clear</button>
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
            <tr v-else-if="filteredOrders.length === 0">
              <td colspan="5" class="px-6 py-4">No payment orders found for selected filters.</td>
            </tr>
            <tr v-for="order in filteredOrders" :key="order.id">
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
import DatePicker from '@/components/ui/DatePicker.vue'
import { computed } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { paymentsService, type PaymentOrder } from '@/services/payments'
import { generateCheckoutLink } from '@/services/checkout'
import { useRouter } from 'vue-router'

const orders = ref<PaymentOrder[]>([])
const isLoading = ref(false)
const copied = ref<string | null>(null)
const router = useRouter()

// filters
const filterStatus = ref<string>('')
const filterFrom = ref<string | null>(null)
const filterTo = ref<string | null>(null)

const filteredOrders = computed(() => {
  const from = filterFrom.value ? new Date(filterFrom.value) : null
  const to = filterTo.value ? new Date(filterTo.value) : null

  return orders.value.filter((o) => {
    // status filter
    if (filterStatus.value) {
      if ((o.status || '').toUpperCase() !== filterStatus.value.toUpperCase()) return false
    }

    // date filter (use createdAt if available)
    if (from || to) {
      const created = o.createdAt ? new Date(o.createdAt) : null
      if (!created) return false
      // normalize to date-only for comparison
      const createdDate = new Date(created.getFullYear(), created.getMonth(), created.getDate())
      if (from) {
        const f = new Date(from.getFullYear(), from.getMonth(), from.getDate())
        if (createdDate < f) return false
      }
      if (to) {
        const t = new Date(to.getFullYear(), to.getMonth(), to.getDate())
        if (createdDate > t) return false
      }
    }

    return true
  })
})

function applyFilters() {
  // computed property updates automatically, but keep this for UX hooks or analytics
}

function clearFilters() {
  filterStatus.value = ''
  filterFrom.value = null
  filterTo.value = null
}

function checkoutLink(id: string) {
  return generateCheckoutLink(id)
}

function statusClass(status: string) {
  const s = (status || '').toUpperCase()
  if (s === 'SUCCEEDED') return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-green-100 text-green-800'
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

