<template>
  <input ref="el" :value="props.modelValue" class="px-2 py-1 border rounded bg-white text-sm" />
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import flatpickr from 'flatpickr'
import 'flatpickr/dist/flatpickr.min.css'

const props = defineProps<{ modelValue?: string | null; placeholder?: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: string | null): void }>()

const el = ref<HTMLInputElement | null>(null)
let fp: any = null

onMounted(() => {
  if (!el.value) return
  fp = flatpickr(el.value as any, {
    dateFormat: 'Y-m-d',
    allowInput: true,
    defaultDate: props.modelValue ?? undefined,
    onChange: (selectedDates: Date[]) => {
      const d = selectedDates && selectedDates[0] ? formatDate(selectedDates[0]) : null
      emit('update:modelValue', d)
    },
  })
})

onBeforeUnmount(() => {
  if (fp) fp.destroy()
})

watch(() => props.modelValue, (v) => {
  if (fp) fp.setDate(v || undefined)
})

function formatDate(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
</script>

<style scoped>
/* make input match existing styles from orders page */
input { width: 160px }
</style>


