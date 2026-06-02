<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-800 text-white flex items-center justify-center p-4">
	<div class="w-full max-w-6xl grid lg:grid-cols-2 gap-8 items-stretch">
	  <!-- Left hero -->
	  <div class="hidden lg:flex flex-col justify-between rounded-3xl border border-white/10 bg-white/5 backdrop-blur-xl p-10 shadow-2xl">
		<div>
		  <div class="inline-flex items-center gap-2 rounded-full bg-emerald-500/15 px-4 py-2 text-emerald-200 text-sm font-medium border border-emerald-400/20">
			Merchant onboarding
		  </div>
		  <h1 class="mt-6 text-4xl font-bold tracking-tight text-white">
			Registra tu comercio y comienza a operar
		  </h1>
		  <p class="mt-4 text-slate-300 text-lg max-w-xl">
			Crea tu cuenta de acceso, completa los datos del negocio y habilita la plataforma para generar órdenes, links de pago y transacciones.
		  </p>
		</div>

		<div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-10">
		  <div class="rounded-2xl border border-white/10 bg-slate-950/40 p-5">
			<p class="text-sm text-slate-300">Inicio inmediato</p>
			<p class="mt-2 text-lg font-semibold">Cuenta activa para operar</p>
		  </div>
		  <div class="rounded-2xl border border-white/10 bg-slate-950/40 p-5">
			<p class="text-sm text-slate-300">Configuración guiada</p>
			<p class="mt-2 text-lg font-semibold">Datos del comercio y contacto</p>
		  </div>
		  <div class="rounded-2xl border border-white/10 bg-slate-950/40 p-5">
			<p class="text-sm text-slate-300">Cobros</p>
			<p class="mt-2 text-lg font-semibold">Links de checkout y órdenes</p>
		  </div>
		  <div class="rounded-2xl border border-white/10 bg-slate-950/40 p-5">
			<p class="text-sm text-slate-300">Control</p>
			<p class="mt-2 text-lg font-semibold">Transacciones y seguimiento</p>
		  </div>
		</div>
	  </div>

	  <!-- Form card -->
	  <div class="rounded-3xl bg-white/95 text-slate-900 shadow-2xl border border-white/20 backdrop-blur-xl p-8 sm:p-10">
		<div class="mb-8">
		  <p class="text-sm font-semibold uppercase tracking-[0.2em] text-emerald-600">Crear comercio</p>
		  <h2 class="text-3xl font-bold text-slate-900 mt-2">Alta de comercio</h2>
		  <p class="mt-2 text-slate-600">
			Completa la información de tu negocio para crear la cuenta y empezar a operar.
		  </p>
		</div>

		<form class="space-y-5" @submit.prevent="handleSubmit">
		  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
			<div>
			  <label for="merchantName" class="block text-sm font-medium text-slate-700 mb-1.5">Nombre del comercio</label>
			  <input
				id="merchantName"
				v-model="form.merchantName"
				type="text"
				required
				placeholder="Nova Commerce S.A.S."
				class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
			  />
			  <p v-if="errors.merchantName" class="mt-2 text-sm text-red-600">{{ errors.merchantName }}</p>
			</div>

			<div>
			  <label for="contactName" class="block text-sm font-medium text-slate-700 mb-1.5">Nombre de contacto</label>
			  <input
				id="contactName"
				v-model="form.contactName"
				type="text"
				required
				placeholder="Juan Pérez"
				class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
			  />
			  <p v-if="errors.contactName" class="mt-2 text-sm text-red-600">{{ errors.contactName }}</p>
			</div>
		  </div>

		  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
			<div>
			  <label for="contactEmail" class="block text-sm font-medium text-slate-700 mb-1.5">Email de contacto</label>
			  <input
				id="contactEmail"
				v-model="form.contactEmail"
				type="email"
				required
				placeholder="contacto@tucomercio.com"
				class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
			  />
			  <p v-if="errors.contactEmail" class="mt-2 text-sm text-red-600">{{ errors.contactEmail }}</p>
			</div>

			<div>
			  <label for="email" class="block text-sm font-medium text-slate-700 mb-1.5">Email de acceso</label>
			  <input
				id="email"
				v-model="form.email"
				type="email"
				required
				placeholder="admin@tucomercio.com"
				class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
			  />
			  <p v-if="errors.email" class="mt-2 text-sm text-red-600">{{ errors.email }}</p>
			</div>
		  </div>

		  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
			<div>
			  <label for="password" class="block text-sm font-medium text-slate-700 mb-1.5">Contraseña</label>
			  <input
				id="password"
				v-model="form.password"
				type="password"
				required
				placeholder="Mínimo 8 caracteres"
				class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
			  />
			  <p v-if="errors.password" class="mt-2 text-sm text-red-600">{{ errors.password }}</p>
			</div>

			<div>
			  <label for="confirmPassword" class="block text-sm font-medium text-slate-700 mb-1.5">Confirmar contraseña</label>
			  <input
				id="confirmPassword"
				v-model="form.confirmPassword"
				type="password"
				required
				placeholder="Repite tu contraseña"
				class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
			  />
			  <p v-if="errors.confirmPassword" class="mt-2 text-sm text-red-600">{{ errors.confirmPassword }}</p>
			</div>
		  </div>

		  <div class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
			Al crear la cuenta, tu comercio quedará listo para iniciar operaciones en la plataforma.
		  </div>

		  <transition
			enter-active-class="transition ease-in duration-100"
			leave-active-class="transition ease-out duration-75"
			enter-from-class="opacity-0"
			leave-to-class="opacity-0"
		  >
			<div v-if="authStore.error" class="flex items-start p-3 bg-red-50 border border-red-200 rounded-lg">
			  <svg class="w-5 h-5 text-red-600 mt-0.5 mr-3 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
				<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
			  </svg>
			  <span class="text-sm text-red-800">{{ authStore.error }}</span>
			</div>
		  </transition>

		  <button
			type="submit"
			:disabled="authStore.isLoading"
			class="w-full px-4 py-2.5 rounded-lg font-semibold transition-all duration-200 bg-slate-900 text-white hover:bg-slate-800 active:bg-slate-950 disabled:opacity-50 disabled:cursor-not-allowed"
		  >
			<span v-if="authStore.isLoading" class="flex items-center justify-center">
			  <svg class="animate-spin -ml-1 mr-2 h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
				<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
				<path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
			  </svg>
			  Creando cuenta...
			</span>
			<span v-else>Crear comercio</span>
		  </button>
		</form>

		<div class="mt-6 pt-6 border-t border-slate-200 flex items-center justify-between gap-4">
		  <p class="text-sm text-slate-600">
			¿Ya tienes cuenta?
			<router-link to="/login" class="font-semibold text-slate-900 hover:underline">Inicia sesión</router-link>
		  </p>
		  <p class="text-xs text-slate-400">Merchant onboarding</p>
		</div>
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
  merchantName: '',
  contactName: '',
  contactEmail: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const errors = ref({
  merchantName: '',
  contactName: '',
  contactEmail: '',
  email: '',
  password: '',
  confirmPassword: '',
})

function validateEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

async function handleSubmit() {
  errors.value = {
	merchantName: '',
	contactName: '',
	contactEmail: '',
	email: '',
	password: '',
	confirmPassword: '',
  }

  if (!form.merchantName.trim()) errors.value.merchantName = 'El nombre del comercio es obligatorio'
  if (!form.contactName.trim()) errors.value.contactName = 'El nombre de contacto es obligatorio'
  if (!form.contactEmail.trim()) errors.value.contactEmail = 'El email de contacto es obligatorio'
  if (!form.email.trim()) errors.value.email = 'El email de acceso es obligatorio'
  if (!form.password) errors.value.password = 'La contraseña es obligatoria'
  if (!form.confirmPassword) errors.value.confirmPassword = 'Debes confirmar la contraseña'

  if (form.contactEmail && !validateEmail(form.contactEmail)) {
	errors.value.contactEmail = 'Ingresa un email de contacto válido'
  }
  if (form.email && !validateEmail(form.email)) {
	errors.value.email = 'Ingresa un email de acceso válido'
  }
  if (form.password && form.password.length < 8) {
	errors.value.password = 'La contraseña debe tener al menos 8 caracteres'
  }
  if (form.password && form.confirmPassword && form.password !== form.confirmPassword) {
	errors.value.confirmPassword = 'Las contraseñas no coinciden'
  }

  const hasErrors = Object.values(errors.value).some(Boolean)
  if (hasErrors) return

  try {
	const response = await authStore.register({
	  email: form.email.trim(),
	  password: form.password,
	  role: 'MERCHANT',
	  merchantName: form.merchantName.trim(),
	  contactName: form.contactName.trim(),
	  contactEmail: form.contactEmail.trim(),
	})

	notificationStore.success('Tu comercio fue creado correctamente')

	await authStore.login(form.email.trim(), form.password)
	notificationStore.success(
	  response.merchantStatus === 'ACTIVE'
		? 'Comercio listo para operar'
		: 'Cuenta creada, revisa el estado de tu comercio'
	)
	router.push('/dashboard')
  } catch (_error) {
	notificationStore.error('No fue posible completar el registro')
  }
}
</script>

