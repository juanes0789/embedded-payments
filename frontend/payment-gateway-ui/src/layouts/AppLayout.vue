<template>
  <div class="flex h-screen bg-slate-50">
    <!-- Sidebar -->
    <div class="w-64 bg-white border-r border-slate-200 flex flex-col">
      <!-- Logo -->
      <div class="p-6 border-b border-slate-200">
        <h1 class="text-xl font-bold text-slate-900">Payment Gateway</h1>
        <p class="text-xs text-slate-500 mt-1">Merchant Dashboard</p>
      </div>

      <!-- Navigation (sidebar accordion groups) -->
      <nav class="flex-1 px-4 py-6 space-y-1">
        <!-- Main group -->
        <div>
          <button
            @click="toggleAccordion('main')"
            :aria-expanded="isOpen('main')"
            class="w-full flex items-center justify-between px-4 py-2.5 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors"
          >
            <span class="flex items-center">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-3m0 0l7-4 7 4M5 9v10a1 1 0 001 1h12a1 1 0 001-1V9m-9 4h4" />
              </svg>
              Main
            </span>
            <svg :class="['w-4 h-4 transition-transform', isOpen('main') ? 'rotate-90' : 'rotate-0']" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>

          <div v-if="isOpen('main')" class="mt-2 space-y-1 pl-8 pr-2">
            <router-link
              to="/dashboard"
              class="block px-3 py-2 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors"
              active-class="bg-slate-100 text-slate-900 font-semibold"
            >
              Dashboard
            </router-link>

            <router-link
              to="/transactions"
              class="block px-3 py-2 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors"
              active-class="bg-slate-100 text-slate-900 font-semibold"
            >
              Transactions
            </router-link>

            <router-link
              to="/settings"
              class="block px-3 py-2 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors"
              active-class="bg-slate-100 text-slate-900 font-semibold"
            >
              Settings
            </router-link>
          </div>
        </div>

        <!-- Payments / Orders group -->
        <div>
          <button
            @click="toggleAccordion('payments')"
            :aria-expanded="isOpen('payments')"
            class="w-full flex items-center justify-between px-4 py-2.5 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors"
          >
            <span class="flex items-center">
              <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7h18M3 12h18M3 17h18" />
              </svg>
              Orders
            </span>
            <svg :class="['w-4 h-4 transition-transform', isOpen('payments') ? 'rotate-90' : 'rotate-0']" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>

          <div v-if="isOpen('payments')" class="mt-2 space-y-1 pl-8 pr-2">
            <router-link
              to="/orders"
              class="block px-3 py-2 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors"
              active-class="bg-slate-100 text-slate-900 font-semibold"
            >
              All orders
            </router-link>
            <router-link
              to="/create-order"
              class="block px-3 py-2 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors"
              active-class="bg-slate-100 text-slate-900 font-semibold"
            >
              Create order
            </router-link>
          </div>
        </div>
      </nav>

      <!-- User Profile -->
      <div class="p-4 border-t border-slate-200 space-y-4">
        <div class="px-2 py-3 rounded-lg bg-slate-50">
          <p class="text-xs text-slate-500 uppercase tracking-wider">Account</p>
          <p class="text-sm font-medium text-slate-900 mt-1 truncate">{{ authStore.user?.email }}</p>
        </div>
        <button
          @click="logout"
          class="w-full px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-slate-100 text-slate-900 hover:bg-slate-200 active:bg-slate-300 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
        >
          Sign out
        </button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <div class="flex-1 overflow-auto">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// simple accordion state (keep track of open sections). Open 'main' by default.
const openSections = ref<Record<string, boolean>>({ main: true })

function toggleAccordion(key: string) {
  openSections.value[key] = !openSections.value[key]
}

function isOpen(key: string) {
  return !!openSections.value[key]
}

// Open a sensible accordion section based on the current route
onMounted(() => {
  const path = route.path || ''
  if (path.startsWith('/orders') || path.startsWith('/create-order')) {
    openSections.value.payments = true
    openSections.value.main = false
  } else if (path.startsWith('/transactions') || path.startsWith('/dashboard') || path.startsWith('/settings')) {
    openSections.value.main = true
    openSections.value.payments = false
  }
})

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.active-nav {
  position: relative;
}
</style>

