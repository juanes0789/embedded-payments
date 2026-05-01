<template>
  <div class="flex h-screen bg-gray-100">
    <!-- Sidebar -->
    <div class="w-64 bg-indigo-600 text-white flex flex-col">
      <div class="p-6 border-b border-indigo-500">
        <h1 class="text-2xl font-bold">Payment Gateway</h1>
      </div>

      <nav class="flex-1 px-4 py-6 space-y-2">
        <router-link
          to="/dashboard"
          class="block px-4 py-2 rounded hover:bg-indigo-500 transition"
          active-class="bg-indigo-700"
        >
          Dashboard
        </router-link>
        <router-link
          to="/settings"
          class="block px-4 py-2 rounded hover:bg-indigo-500 transition"
          active-class="bg-indigo-700"
        >
          Settings
        </router-link>
        <router-link
          to="/transactions"
          class="block px-4 py-2 rounded hover:bg-indigo-500 transition"
          active-class="bg-indigo-700"
        >
          Transactions
        </router-link>
      </nav>

      <div class="p-4 border-t border-indigo-500">
        <p class="text-sm">{{ authStore.user?.email }}</p>
        <button
          @click="logout"
          class="w-full mt-4 px-4 py-2 bg-indigo-700 rounded hover:bg-indigo-800 transition text-sm"
        >
          Logout
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

