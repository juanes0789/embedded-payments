<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Transactions</h1>

      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <p class="text-sm text-gray-600">View your recent transactions and payment history</p>
          </div>

          <!-- Filters Panel (HU 2.2) -->
          <div class="flex flex-wrap items-center gap-3">
            <!-- Search ID -->
            <div class="relative">
              <input
                v-model="searchId"
                type="text"
                placeholder="Search by ID..."
                class="pl-9 pr-4 py-2 border border-gray-300 rounded-md text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent w-48 md:w-60 bg-white text-gray-900 placeholder-gray-400"
              />
              <span class="absolute left-3 top-2.5 text-gray-400">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </span>
            </div>

            <!-- Status Filter -->
            <select
              v-model="selectedStatus"
              @change="fetchTransactions"
              class="px-3 py-2 border border-gray-300 rounded-md text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white text-gray-900"
            >
              <option value="ALL">All Statuses</option>
              <option value="COMPLETED">Completed</option>
              <option value="PENDING">Pending</option>
              <option value="FAILED">Failed</option>
            </select>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">ID</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Customer</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Amount</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Status</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Date</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Action</th>
              </tr>
            </thead>
            <tbody class="divide-y">
              <tr v-if="!transactions.length" class="hover:bg-gray-50">
                <td colspan="6" class="px-6 py-4 text-center text-gray-600">
                  No transactions found
                </td>
              </tr>
              <tr v-for="transaction in transactions" :key="transaction.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 text-sm text-gray-900 font-mono">
                  {{ transaction.id.substring(0, 8) }}...
                </td>
                <td class="px-6 py-4 text-sm text-gray-900">
                  {{ transaction.customerEmail }}
                </td>
                <td class="px-6 py-4 text-sm font-semibold text-gray-900">
                  {{ transaction.currency }} {{ transaction.amount.toFixed(2) }}
                </td>
                <td class="px-6 py-4 text-sm">
                  <span
                    :class="[
                      'px-3 py-1 rounded-full text-xs font-semibold',
                      transaction.status === 'COMPLETED'
                        ? 'bg-green-100 text-green-800'
                        : transaction.status === 'PENDING'
                        ? 'bg-yellow-100 text-yellow-800'
                        : 'bg-red-100 text-red-800',
                    ]"
                  >
                    {{ transaction.status }}
                  </span>
                </td>
                <td class="px-6 py-4 text-sm text-gray-600">
                  {{ new Date(transaction.createdAt).toLocaleDateString() }}
                </td>
                <td class="px-6 py-4 text-sm">
                  <router-link
                    :to="`/transactions/${transaction.id}`"
                    class="text-indigo-600 hover:text-indigo-900 font-medium"
                  >
                    View
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { transactionService } from '@/services/transactions'
import { Transaction } from '@/types'

const allTransactions = ref<Transaction[]>([])
const searchId = ref('')
const selectedStatus = ref('ALL')

const transactions = computed(() => {
  return allTransactions.value.filter(t => {
    const matchesId = !searchId.value.trim() || t.id.toLowerCase().includes(searchId.value.toLowerCase().trim())
    return matchesId
  })
})

async function fetchTransactions() {
  try {
    const response = await transactionService.list(1, 50, selectedStatus.value)
    allTransactions.value = response.items
  } catch (error) {
    console.error('Failed to fetch transactions:', error)
  }
}

onMounted(async () => {
  await fetchTransactions()
})
</script>

