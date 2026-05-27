<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['role-updated'])
const router = useRouter()
const isLoading = ref(false)
const errorMessage = ref('')
const selectedRole = ref('')

const saveRole = async () => {
  if (!selectedRole.value) {
    errorMessage.value = 'Debes seleccionar un rol para continuar.'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const res = await fetch('http://localhost:8080/api/usuarios/rol', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rol: selectedRole.value }),
      credentials: 'include'
    })

    if (!res.ok) throw new Error('No se pudo actualizar el rol.')

    const data = await res.json()
    // Actualizamos localStorage
    localStorage.setItem('user_role', data.rol)
    emit('role-updated', data.rol)
    
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6 animate-fade-in-up">
      <h2 class="text-2xl font-bold text-gray-900 text-center mb-4">Completa tu Registro</h2>
      <p class="text-sm text-gray-500 text-center mb-6">
        Por favor, indícanos cómo vas a utilizar la plataforma PATS para configurar tu cuenta correctamente.
      </p>

      <div v-if="errorMessage" class="mb-4 bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
        <p class="text-sm text-red-700">{{ errorMessage }}</p>
      </div>

      <div class="space-y-4">
        <label 
          class="flex items-center p-4 border rounded-lg cursor-pointer transition-all duration-200"
          :class="selectedRole === 'ALUMNO' ? 'border-blue-500 bg-blue-50 ring-1 ring-blue-500' : 'border-gray-200 hover:bg-gray-50'"
        >
          <input type="radio" value="ALUMNO" v-model="selectedRole" class="hidden" />
          <div class="ml-3">
            <span class="block text-sm font-medium text-gray-900">Soy Alumno</span>
            <span class="block text-sm text-gray-500">Quiero unirme a clases y subir mis portafolios.</span>
          </div>
        </label>

        <label 
          class="flex items-center p-4 border rounded-lg cursor-pointer transition-all duration-200"
          :class="selectedRole === 'DOCENTE' ? 'border-blue-500 bg-blue-50 ring-1 ring-blue-500' : 'border-gray-200 hover:bg-gray-50'"
        >
          <input type="radio" value="DOCENTE" v-model="selectedRole" class="hidden" />
          <div class="ml-3">
            <span class="block text-sm font-medium text-gray-900">Soy Docente</span>
            <span class="block text-sm text-gray-500">Quiero crear clases y evaluar a mis alumnos.</span>
          </div>
        </label>
      </div>

      <div class="mt-8">
        <button 
          @click="saveRole"
          :disabled="isLoading || !selectedRole"
          class="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
        >
          <span v-if="isLoading">Guardando...</span>
          <span v-else>Confirmar Rol e Ingresar</span>
        </button>
      </div>
    </div>
  </div>
</template>
