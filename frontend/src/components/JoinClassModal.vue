<script setup>
import { ref } from 'vue'
import BasePremiumModal from './BasePremiumModal.vue'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['close', 'joined'])
const isLoading = ref(false)
const codigoAcceso = ref('')
const errorMessage = ref('')

const closeModal = () => {
  codigoAcceso.value = ''
  errorMessage.value = ''
  emit('close')
}

const unirseClase = async () => {
  if (!codigoAcceso.value.trim()) {
    errorMessage.value = "Por favor, ingresa un código de clase."
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  
  try {
    const response = await fetch('http://localhost:8080/api/clases/unirse', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ codigoAcceso: codigoAcceso.value }),
      credentials: 'include' // IMPORTANTE: Para que Spring Boot sepa quién somos
    })

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.error || 'Error al unirse a la clase')
    }

    emit('joined', data)
    closeModal()

  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <BasePremiumModal :show="show" @close="closeModal" maxWidthClass="max-w-md">
    
    <template #header>
      <h3 class="text-2xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-slate-800 to-slate-600 tracking-tight">Unirse a una clase</h3>
    </template>

    <template #content>
      <p class="text-slate-500 mb-8 text-sm font-medium leading-relaxed">
        Pídele a tu profesor el código de acceso de la clase e introdúcelo a continuación para vincular tu portafolio.
      </p>

      <!-- Input del Código -->
      <form @submit.prevent="unirseClase" class="space-y-6">
        
        <div class="group/input">
          <label for="codigo" class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 group-focus-within/input:text-blue-500 transition-colors">Código de acceso</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <svg class="h-5 w-5 text-slate-400 group-focus-within/input:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
              </svg>
            </div>
            <input 
              type="text" 
              id="codigo" 
              v-model="codigoAcceso"
              class="block w-full pl-11 pr-4 py-3.5 border-2 border-slate-100 rounded-xl leading-5 bg-slate-50/50 text-slate-800 placeholder-slate-300 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all duration-300 sm:text-base uppercase font-mono font-bold tracking-wider shadow-inner" 
              placeholder="PATS-XYZ1"
              required
            >
          </div>
        </div>

        <!-- Alerta de Error (Estilo suave) -->
        <Transition name="slide-fade">
          <div v-if="errorMessage" class="rounded-xl bg-red-50/80 backdrop-blur-md p-4 border border-red-200/50 shadow-sm">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 rounded-full bg-red-100 flex items-center justify-center">
                  <svg class="h-4 w-4 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                  </svg>
                </div>
              </div>
              <div class="ml-3">
                <p class="text-sm font-semibold text-red-700">
                  {{ errorMessage }}
                </p>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Botones -->
        <div class="mt-8 pt-2 flex justify-end space-x-3">
          <button 
            type="button" 
            @click="closeModal"
            class="px-6 py-2.5 rounded-xl text-slate-500 font-bold hover:bg-slate-100 hover:text-slate-800 focus:outline-none transition-all duration-200"
            :disabled="isLoading"
          >
            Cancelar
          </button>
          <button 
            type="submit" 
            class="relative px-6 py-2.5 bg-slate-900 rounded-xl text-white font-bold hover:bg-black hover:shadow-lg hover:-translate-y-0.5 focus:outline-none focus:ring-4 focus:ring-slate-900/30 transition-all duration-300 disabled:opacity-50 disabled:hover:translate-y-0"
            :disabled="isLoading"
          >
            <span class="flex items-center justify-center">
              <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ isLoading ? 'Conectando...' : 'Unirse a la clase' }}
            </span>
          </button>
        </div>
      </form>
    </template>

  </BasePremiumModal>
</template>
