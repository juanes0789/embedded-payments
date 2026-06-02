<template>
  <div class="bg-white rounded-lg shadow p-6">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Merchant Profile</h2>

    <div v-if="merchantStore.current" class="space-y-6">
      <!-- Basic Info -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Name</label>
          <p class="text-lg text-gray-900">{{ merchantStore.current.name }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
          <p class="text-lg text-gray-900">{{ merchantStore.current.email }}</p>
        </div>
      </div>

      <!-- Status -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">Merchant Status</label>
        <div class="flex items-center gap-4">
          <span
            :class="[
              'px-4 py-2 rounded-full font-semibold',
              merchantStore.current.status === 'ACTIVE'
                ? 'bg-green-100 text-green-800'
                : 'bg-red-100 text-red-800',
            ]"
          >
            {{ merchantStore.current.status }}
          </span>
          <p class="text-sm text-gray-600">
            {{ merchantStore.current.status === 'ACTIVE' ? 'Your merchant account is active' : 'Your merchant account is inactive' }}
          </p>
        </div>
      </div>

      <!-- Contact Info -->
      <div class="border-t pt-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Contact Information</h3>
        <div class="space-y-2">
          <p v-if="merchantStore.current.contact_name">
            <span class="text-sm font-medium text-gray-700">Name:</span>
            <span class="text-gray-900">{{ merchantStore.current.contact_name }}</span>
          </p>
          <p v-if="merchantStore.current.contact_email">
            <span class="text-sm font-medium text-gray-700">Email:</span>
            <span class="text-gray-900">{{ merchantStore.current.contact_email }}</span>
          </p>
          <p v-if="!merchantStore.current.contact_name && !merchantStore.current.contact_email" class="text-gray-600">
            No contact information configured
          </p>
        </div>
      </div>

      <!-- Bank Account -->
      <div class="border-t pt-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Bank Account</h3>
        <div v-if="merchantStore.hasBankAccount" class="bg-green-50 border border-green-200 rounded p-4">
          <p class="text-green-800">✓ Bank account information is registered and encrypted</p>
        </div>
        <div v-else class="text-gray-600">
          No bank account information registered
        </div>
      </div>

              <!-- API Key (development/testing) -->
              <div class="border-t pt-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-4">API Key (desarrollo)</h3>
                <div class="space-y-3">
                  <p v-if="apiKeySaved" class="text-sm text-gray-700">API Key configurada: <span class="font-mono">{{ maskedApiKey }}</span></p>
                  <div class="flex items-center gap-2">
                    <input v-model="apiKey" type="text" placeholder="Pegar API Key (epk_...)" class="border rounded px-3 py-2 w-full" />
                    <button @click="saveApiKey" class="px-3 py-2 bg-blue-600 text-white rounded">Guardar</button>
                    <button @click="removeApiKey" class="px-3 py-2 bg-red-600 text-white rounded">Borrar</button>
                  </div>
                  <p class="text-xs text-gray-500">Esta clave se guarda en localStorage y sirve para llamadas a /payments y /transactions (solo desarrollo).</p>
                </div>
              </div>

      <!-- Dates -->
      <div class="border-t pt-6 text-sm text-gray-600">
        <p>Created: {{ new Date(merchantStore.current.created_at || merchantStore.current.createdAt!).toLocaleDateString() }}</p>
        <p>Last Updated: {{ new Date(merchantStore.current.updated_at || merchantStore.current.updatedAt!).toLocaleDateString() }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMerchantStore } from '@/stores/merchant'
import { ref, computed, onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notifications'

const merchantStore = useMerchantStore()
const notificationStore = useNotificationStore()

const apiKey = ref<string>('')
const apiKeySaved = computed(() => !!localStorage.getItem('apiKey'))

const maskedApiKey = computed(() => {
  const k = localStorage.getItem('apiKey') || ''
  if (!k) return ''
  return k.length > 8 ? `${k.substring(0, 6)}...${k.substring(k.length - 4)}` : k
})

function saveApiKey() {
  try {
    if (!apiKey.value) {
      notificationStore.error('Ingresa una API Key válida')
      return
    }
    localStorage.setItem('apiKey', apiKey.value.trim())
    apiKey.value = ''
    notificationStore.success('API Key guardada en localStorage')
  } catch (e) {
    notificationStore.error('Error al guardar API Key')
  }
}

function removeApiKey() {
  try {
    localStorage.removeItem('apiKey')
    notificationStore.success('API Key eliminada')
  } catch (e) {
    notificationStore.error('Error al borrar API Key')
  }
}

onMounted(() => {
  apiKey.value = ''
})
</script>

