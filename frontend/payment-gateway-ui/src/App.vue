<template>
  <div class="min-h-screen bg-gray-50">
    <div v-if="isLoading" class="flex items-center justify-center h-screen">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
    </div>
    <router-view v-else />
    <Notifications />
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Notifications from '@/components/shared/Notifications.vue'

const authStore = useAuthStore()
const isLoading = computed(() => authStore.isLoading)

onMounted(() => {
  // Check if user is authenticated and initialize
})
</script>

