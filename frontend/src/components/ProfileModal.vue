<script setup>
import { ref, reactive } from 'vue'

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['profile-updated'])

const form = reactive({
  nombre: localStorage.getItem('user_name') || '',
  matricula: ''
})

const isLoading = ref(false)
const errorMessage = ref('')

const saveProfile = async () => {
  if (!form.nombre || !form.matricula) {
    errorMessage.value = 'Todos los campos son obligatorios.'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const res = await fetch('http://localhost:8080/api/usuarios/perfil', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre: form.nombre, matricula: form.matricula }),
      credentials: 'include'
    })

    if (!res.ok) throw new Error('Error al guardar el perfil.')

    const data = await res.json()
    localStorage.setItem('user_name', data.nombre)
    emit('profile-updated', data)
    
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
      <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100 mb-4">
        <svg class="h-6 w-6 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h2 class="text-xl font-bold text-gray-900 text-center mb-2">Información Incompleta</h2>
      <p class="text-sm text-gray-500 text-center mb-6">
        Como alumno, es obligatorio que registres tu Matrícula y Nombre Completo para la generación automática de las portadas de tus evidencias.
      </p>

      <div v-if="errorMessage" class="mb-4 bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
        <p class="text-sm text-red-700">{{ errorMessage }}</p>
      </div>

      <form @submit.prevent="saveProfile" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Nombre Completo</label>
          <input 
            type="text" 
            v-model="form.nombre"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Ej. Juan Pérez"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Matrícula</label>
          <input 
            type="text" 
            v-model="form.matricula"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Ej. 19123456"
          />
        </div>

        <div class="pt-4">
          <button 
            type="submit" 
            :disabled="isLoading"
            class="w-full py-2.5 px-4 bg-blue-600 text-white rounded-md font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
          >
            <span v-if="isLoading">Guardando...</span>
            <span v-else>Guardar y Continuar</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
