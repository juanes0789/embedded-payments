<template>
  <div class="min-h-screen bg-gray-50">
    <AppLayout>
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-8">Dashboard</h1>

        <!-- Loading State -->
        <div v-if="merchantStore.isLoading" class="flex justify-center">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>

        <!-- Error State -->
        <div
          v-else-if="merchantStore.error"
          class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded"
        >
          {{ merchantStore.error }}
        </div>

        <!-- Dashboard Content -->
        <div v-else-if="merchantStore.current" class="space-y-8">
          <!-- Merchant Status Card -->
          <div class="bg-white rounded-lg shadow p-6">
            <h2 class="text-xl font-semibold text-gray-900 mb-4">Merchant Status</h2>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <p class="text-sm text-gray-600">Name</p>
                <p class="text-lg font-semibold text-gray-900">{{ merchantStore.current.name }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-600">Email</p>
                <p class="text-lg font-semibold text-gray-900">{{ merchantStore.current.email }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-600">Status</p>
                <div class="mt-2">
                  <span
                    :class="[
                      'px-3 py-1 rounded-full text-sm font-semibold',
                      merchantStore.current.status === 'ACTIVE'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-red-100 text-red-800',
                    ]"
                  >
                    {{ merchantStore.current.status }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <router-link
              to="/settings/contact"
              class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition cursor-pointer"
            >
              <h3 class="text-lg font-semibold text-gray-900 mb-2">Update Contact</h3>
              <p class="text-sm text-gray-600">Manage your contact information</p>
            </router-link>

            <router-link
              to="/settings/bank"
              class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition cursor-pointer"
            >
              <h3 class="text-lg font-semibold text-gray-900 mb-2">Bank Account</h3>
              <p class="text-sm text-gray-600">Register your bank details</p>
            </router-link>

            <router-link
              to="/settings/profile"
              class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition cursor-pointer"
            >
              <h3 class="text-lg font-semibold text-gray-900 mb-2">Profile</h3>
              <p class="text-sm text-gray-600">View your merchant profile</p>
            </router-link>

            <router-link
              to="/transactions"
              class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition cursor-pointer"
            >
              <h3 class="text-lg font-semibold text-gray-900 mb-2">Transactions</h3>
              <p class="text-sm text-gray-600">View recent transactions</p>
            </router-link>
          </div>

          <!-- Info Cards -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 class="font-semibold text-blue-900 mb-2">Contact Information</h3>
              <p v-if="merchantStore.current.contactName" class="text-sm text-blue-800">
                Name: {{ merchantStore.current.contactName }}
              </p>
              <p v-if="merchantStore.current.contactEmail" class="text-sm text-blue-800">
                Email: {{ merchantStore.current.contactEmail }}
              </p>
              <p v-else class="text-sm text-blue-800">Not configured yet</p>
            </div>

            <div class="bg-green-50 border border-green-200 rounded-lg p-6">
              <h3 class="font-semibold text-green-900 mb-2">Bank Account</h3>
              <p v-if="merchantStore.hasBankAccount" class="text-sm text-green-800">
                ✓ Bank account registered
              </p>
              <p v-else class="text-sm text-green-800">Not configured yet</p>
            </div>
          </div>

          <!-- Status Management -->
          <div class="bg-white rounded-lg shadow p-6">
            <h2 class="text-xl font-semibold text-gray-900 mb-4">Account Management</h2>
            <div class="space-y-4">
              <div v-if="merchantStore.current.status === 'ACTIVE'" class="bg-green-50 border border-green-200 rounded p-4">
                <p class="text-green-900 font-semibold mb-3">Your account is ACTIVE</p>
                <button
                  @click="openDeactivateDialog"
                  :disabled="merchantStore.isLoading"
                  class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50 transition"
                >
                  {{ merchantStore.isLoading ? 'Processing...' : 'Deactivate Account' }}
                </button>
              </div>
              <div v-else class="bg-red-50 border border-red-200 rounded p-4">
                <p class="text-red-900 font-semibold mb-3">Your account is INACTIVE</p>
                <button
                  @click="openActivateDialog"
                  :disabled="merchantStore.isLoading"
                  class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50 transition"
                >
                  {{ merchantStore.isLoading ? 'Processing...' : 'Activate Account' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Activate Dialog -->
          <div v-if="showActivateDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg p-8 max-w-md w-full">
              <h3 class="text-xl font-semibold text-gray-900 mb-4">Activate Account</h3>
              <p class="text-gray-600 mb-4">Are you sure you want to activate your merchant account?</p>
              <div class="space-y-4">
                <textarea
                  v-model="statusChangeForm.reason"
                  rows="3"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="Reason for activation..."
                />
                <div v-if="statusChangeError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
                  {{ statusChangeError }}
                </div>
                <div class="flex gap-4">
                  <button
                    @click="closeStatusDialog"
                    :disabled="merchantStore.isLoading"
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 transition"
                  >
                    Cancel
                  </button>
                  <button
                    @click="handleActivate"
                    :disabled="merchantStore.isLoading || !statusChangeForm.reason.trim()"
                    class="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 transition"
                  >
                    {{ merchantStore.isLoading ? 'Processing...' : 'Activate' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Deactivate Dialog -->
          <div v-if="showDeactivateDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg p-8 max-w-md w-full">
              <h3 class="text-xl font-semibold text-gray-900 mb-4">Deactivate Account</h3>
              <p class="text-gray-600 mb-4">Are you sure you want to deactivate your merchant account? This action may prevent you from accepting payments.</p>
              <div class="space-y-4">
                <textarea
                  v-model="statusChangeForm.reason"
                  rows="3"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="Reason for deactivation..."
                />
                <div v-if="statusChangeError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
                  {{ statusChangeError }}
                </div>
                <div class="flex gap-4">
                  <button
                    @click="closeStatusDialog"
                    :disabled="merchantStore.isLoading"
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 transition"
                  >
                    Cancel
                  </button>
                  <button
                    @click="handleDeactivate"
                    :disabled="merchantStore.isLoading || !statusChangeForm.reason.trim()"
                    class="flex-1 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50 transition"
                  >
                    {{ merchantStore.isLoading ? 'Processing...' : 'Deactivate' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useMerchantStore } from '@/stores/merchant'
import { useNotificationStore } from '@/stores/notifications'
import AppLayout from '@/layouts/AppLayout.vue'

const authStore = useAuthStore()
const merchantStore = useMerchantStore()
const notificationStore = useNotificationStore()

const showActivateDialog = ref(false)
const showDeactivateDialog = ref(false)
const statusChangeError = ref('')
const statusChangeForm = ref({
  reason: '',
})

function openActivateDialog() {
  statusChangeForm.value.reason = ''
  statusChangeError.value = ''
  showActivateDialog.value = true
}

function openDeactivateDialog() {
  statusChangeForm.value.reason = ''
  statusChangeError.value = ''
  showDeactivateDialog.value = true
}

function closeStatusDialog() {
  showActivateDialog.value = false
  showDeactivateDialog.value = false
  statusChangeForm.value.reason = ''
  statusChangeError.value = ''
}

async function handleActivate() {
  if (!statusChangeForm.value.reason.trim()) {
    statusChangeError.value = 'Reason is required'
    return
  }

  try {
    await merchantStore.activate(statusChangeForm.value.reason)
    notificationStore.success('Account activated successfully')
    closeStatusDialog()
  } catch (error) {
    statusChangeError.value = 'Failed to activate account'
    notificationStore.error('Failed to activate account')
  }
}

async function handleDeactivate() {
  if (!statusChangeForm.value.reason.trim()) {
    statusChangeError.value = 'Reason is required'
    return
  }

  try {
    await merchantStore.deactivate(statusChangeForm.value.reason)
    notificationStore.success('Account deactivated successfully')
    closeStatusDialog()
  } catch (error) {
    statusChangeError.value = 'Failed to deactivate account'
    notificationStore.error('Failed to deactivate account')
  }
}

onMounted(async () => {
  if (authStore.user) {
    await merchantStore.fetchById(authStore.user.id)
  }
})
</script>

