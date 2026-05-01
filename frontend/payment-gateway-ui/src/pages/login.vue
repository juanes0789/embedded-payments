<template>
  <div class="min-h-screen bg-gradient-to-br from-indigo-600 to-indigo-800 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-lg shadow-xl p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Payment Gateway</h1>
        <p class="text-gray-600 mb-8">Manage your payments and merchant settings</p>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              required
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="your@email.com"
            />
            <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">
              Password
            </label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              required
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="••••••••"
            />
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
          </div>

          <p v-if="authStore.error" class="text-sm text-red-600">{{ authStore.error }}</p>

          <button
            type="submit"
            :disabled="authStore.isLoading"
            class="w-full bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            {{ authStore.isLoading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <p class="mt-4 text-center text-sm text-gray-600">
          Demo credentials: Use test@example.com / password
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const form = reactive({
  email: '',
  password: '',
})

const errors = ref({
  email: '',
  password: '',
})

async function handleLogin() {
  errors.value = { email: '', password: '' }

  if (!form.email) {
    errors.value.email = 'Email is required'
    return
  }
  if (!form.password) {
    errors.value.password = 'Password is required'
    return
  }

  try {
    await authStore.login(form.email, form.password)
    notificationStore.success('Logged in successfully')
    router.push('/dashboard')
  } catch (error) {
    notificationStore.error('Login failed. Please check your credentials.')
  }
}
</script>

