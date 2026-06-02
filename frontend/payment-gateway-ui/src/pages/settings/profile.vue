<template>
  <div class="bg-slate-50 rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <div class="border-b border-slate-200 bg-white px-6 py-5 sm:px-8">
      <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-[0.2em] text-emerald-600">Perfil del comercio</p>
          <h2 class="mt-1 text-2xl font-bold text-slate-900">{{ merchant?.name || 'Merchant Profile' }}</h2>
          <p class="mt-1 text-sm text-slate-600">Resumen de tu cuenta, contacto y estado operativo.</p>
        </div>

        <span
          :class="[
            'inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold',
            merchant?.status === 'ACTIVE'
              ? 'bg-emerald-100 text-emerald-800'
              : 'bg-rose-100 text-rose-800',
          ]"
        >
          {{ merchant?.status || '—' }}
        </span>
      </div>
    </div>

    <div v-if="merchant" class="space-y-6 p-6 sm:p-8">
      <div class="grid gap-4 md:grid-cols-3">
        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Email de acceso</p>
          <p class="mt-2 break-all text-sm font-medium text-slate-900">{{ merchant.email }}</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Contacto</p>
          <p class="mt-2 text-sm font-medium text-slate-900">{{ merchant.contact_name || 'Sin contacto configurado' }}</p>
          <p class="text-sm text-slate-600">{{ merchant.contact_email || '—' }}</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Operación</p>
          <p class="mt-2 text-sm font-medium text-slate-900">{{ merchantStore.hasBankAccount ? 'Cuenta bancaria registrada' : 'Sin cuenta bancaria' }}</p>
          <p class="text-sm text-slate-600">{{ merchantStore.hasBankAccount ? 'Listo para liquidación' : 'Completa tus datos bancarios cuando quieras' }}</p>
        </div>
      </div>

      <!-- Financial overview cards -->
      <div class="mt-4 grid gap-4 md:grid-cols-4">
        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Total recaudado</p>
          <p class="mt-2 text-lg font-semibold text-slate-900">{{ formatMoney(totalCollected, displayCurrency) }}</p>
          <p class="text-sm text-slate-600">Suma de transacciones completadas</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Por pagar</p>
          <p class="mt-2 text-lg font-semibold text-slate-900">{{ formatMoney(pendingAmount, displayCurrency) }}</p>
          <p class="text-sm text-slate-600">Transacciones pendientes</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Comisión plataforma</p>
          <p class="mt-2 text-lg font-semibold text-slate-900">{{ formatMoney(platformCommission, displayCurrency) }}</p>
          <p class="text-sm text-slate-600">Comisiones acumuladas sobre ventas</p>
        </div>

        <router-link to="/settings/balances" class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm hover:shadow-md flex flex-col justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Ver detalles</p>
            <p class="mt-2 text-lg font-semibold text-indigo-700">Ir a balances</p>
          </div>
          <p class="text-sm text-slate-600">Ver transacciones y desglose por estado</p>
        </router-link>
      </div>

      <div class="grid gap-6 lg:grid-cols-2">
        <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center justify-between gap-4">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">Información de contacto</h3>
              <p class="mt-1 text-sm text-slate-600">Datos visibles del responsable del comercio.</p>
            </div>
          </div>

          <div class="mt-5 space-y-4">
            <div class="rounded-xl bg-slate-50 px-4 py-3">
              <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Nombre</p>
              <p class="mt-1 text-sm font-medium text-slate-900">{{ merchant.contact_name || 'No contact information configured' }}</p>
            </div>

            <div class="rounded-xl bg-slate-50 px-4 py-3">
              <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Email</p>
              <p class="mt-1 text-sm font-medium text-slate-900">{{ merchant.contact_email || 'No contact information configured' }}</p>
            </div>
          </div>
        </section>

        <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center justify-between gap-4">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">Estado y fechas</h3>
              <p class="mt-1 text-sm text-slate-600">Información de creación y actualización.</p>
            </div>
          </div>

          <div class="mt-5 space-y-4">
            <div class="rounded-xl bg-slate-50 px-4 py-3">
              <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Estado</p>
              <p class="mt-1 text-sm font-medium text-slate-900">{{ merchant.status === 'ACTIVE' ? 'Activo y listo para operar' : 'Inactivo' }}</p>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <div class="rounded-xl bg-slate-50 px-4 py-3">
                <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Creado</p>
                <p class="mt-1 text-sm font-medium text-slate-900">{{ createdAtLabel }}</p>
              </div>
              <div class="rounded-xl bg-slate-50 px-4 py-3">
                <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">Última actualización</p>
                <p class="mt-1 text-sm font-medium text-slate-900">{{ updatedAtLabel }}</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <div v-else class="p-6 sm:p-8 text-sm text-slate-600">
      No merchant information available.
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useMerchantStore } from '@/stores/merchant'
import { transactionService } from '@/services/transactions'

const merchantStore = useMerchantStore()
const merchant = computed(() => merchantStore.current)

// Balances state
const balancesLoading = ref(false)
const transactions = ref<any[]>([])

function formatMoney(amount: number | null | undefined, currency = 'USD') {
  if (amount == null || Number.isNaN(Number(amount))) return '—'
  try {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Number(amount))
  } catch {
    return `${currency} ${Number(amount).toFixed(2)}`
  }
}

// Commission calculation used in checkout (2.9% + 0.30)
function calcPlatformFee(amount: number) {
  return amount * 0.029 + 0.3
}

const totalCollected = computed(() => {
  return transactions.value
    .filter((t) => ['SUCCEEDED', 'COMPLETED'].includes(String(t.status).toUpperCase()))
    .reduce((s, t) => s + Number(t.amount || 0), 0)
})

const pendingAmount = computed(() => {
  return transactions.value
    .filter((t) => String(t.status).toUpperCase() === 'PENDING')
    .reduce((s, t) => s + Number(t.amount || 0), 0)
})

const platformCommission = computed(() => {
  // sum commissions only for completed/succeeded transactions
  return transactions.value
    .filter((t) => ['SUCCEEDED', 'COMPLETED'].includes(String(t.status).toUpperCase()))
    .reduce((s, t) => s + calcPlatformFee(Number(t.amount || 0)), 0)
})

const displayCurrency = computed(() => transactions.value.length ? transactions.value[0].currency || 'USD' : 'USD')

async function fetchMerchantTransactions() {
  if (!merchant.value) return
  balancesLoading.value = true
  try {
    // Attempt to fetch many transactions in one call (pageSize large). If backend paginates, adjust later.
    const resp = await transactionService.list(1, 1000)
    transactions.value = resp.items.filter((t: any) => t.merchantId === merchant.value!.id)
  } catch (err) {
    // ignore silently for now; transactions will be empty
    transactions.value = []
  } finally {
    balancesLoading.value = false
  }
}

onMounted(() => {
  // Try initial fetch if merchant already available
  if (merchant.value) fetchMerchantTransactions()
})

// Re-fetch whenever merchant becomes available/changes
watch(merchant, (val) => {
  if (val) fetchMerchantTransactions()
})

function formatDate(value: string | Date | undefined | null) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date)
}

const createdAtLabel = computed(() =>
  formatDate(merchant.value?.created_at || merchant.value?.createdAt || null)
)

const updatedAtLabel = computed(() =>
  formatDate(merchant.value?.updated_at || merchant.value?.updatedAt || null)
)
</script>

