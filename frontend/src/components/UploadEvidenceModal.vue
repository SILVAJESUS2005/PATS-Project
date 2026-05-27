<script setup>
import { ref } from 'vue'
import BasePremiumModal from './BasePremiumModal.vue'

const props = defineProps({
  show: Boolean,
  portafolioId: {
    type: Number,
    required: true
  },
  portafolioName: {
    type: String,
    default: 'Portafolio'
  }
})

const emit = defineEmits(['close', 'uploaded'])

const selectedFile = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    errorMessage.value = ''
    successMessage.value = ''
  }
}

const closeModal = () => {
  selectedFile.value = null
  errorMessage.value = ''
  successMessage.value = ''
  emit('close')
}

const submitEvidencia = async () => {
  if (!selectedFile.value) {
    errorMessage.value = "Por favor, selecciona un archivo antes de enviar."
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('portafolioId', props.portafolioId)

  try {
    const response = await fetch('http://localhost:8080/api/entregas/subir', {
      method: 'POST',
      body: formData,
      credentials: 'include' // Obligatorio para validar sesión
    })

    const data = await response.text() // El backend a veces devuelve JSON o un String en error

    if (!response.ok) {
      throw new Error(data || 'Ocurrió un error al subir el archivo')
    }

    // Intentar parsear el JSON si fue exitoso
    let jsonResponse
    try {
      jsonResponse = JSON.parse(data)
    } catch(e) {
      jsonResponse = { mensaje: 'Archivo subido correctamente' }
    }

    successMessage.value = jsonResponse.mensaje || '¡Evidencia enviada con éxito!'
    
    // Esperar un momento para que el usuario vea el éxito antes de cerrar
    setTimeout(() => {
      emit('uploaded')
      closeModal()
    }, 1500)
    
  } catch (error) {
    errorMessage.value = error.message || 'Fallo de conexión con el servidor.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <BasePremiumModal :show="show" @close="closeModal" maxWidthClass="max-w-lg">
    
    <template #header>
      <h3 class="text-xl font-bold text-slate-800 tracking-tight border-b border-slate-100 pb-2 w-full pr-4">
        Subir Evidencia - <span class="text-blue-600 font-extrabold">{{ portafolioName }}</span>
      </h3>
    </template>

    <template #content>
      <form @submit.prevent="submitEvidencia" class="space-y-6 mt-4">
        
        <!-- Área de Selección de Archivo -->
        <div>
          <label class="block text-sm font-bold text-slate-700 mb-2">Seleccionar Archivo</label>
          <div class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-slate-200 border-dashed rounded-xl bg-slate-50 hover:bg-slate-100 transition-colors cursor-pointer relative">
            <div class="space-y-1 text-center">
              <svg class="mx-auto h-12 w-12 text-slate-400" stroke="currentColor" fill="none" viewBox="0 0 48 48" aria-hidden="true">
                <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <div class="flex text-sm text-slate-600 justify-center">
                <label for="file-upload" class="relative cursor-pointer rounded-md bg-transparent font-medium text-blue-600 focus-within:outline-none focus-within:ring-2 focus-within:ring-blue-500 focus-within:ring-offset-2 hover:text-blue-500">
                  <span>{{ selectedFile ? 'Cambiar archivo' : 'Sube un archivo' }}</span>
                  <input id="file-upload" name="file-upload" type="file" class="sr-only" @change="handleFileSelect">
                </label>
                <p class="pl-1" v-if="!selectedFile">o arrástralo aquí</p>
              </div>
              <p class="text-xs text-slate-500">PDF, PNG, JPG, ZIP hasta 10MB</p>
            </div>
            <!-- Capa invisible sobre toda el área para permitir hacer clic en cualquier lado -->
            <input type="file" class="absolute inset-0 w-full h-full opacity-0 cursor-pointer" @change="handleFileSelect" title="">
          </div>

          <!-- Nombre del archivo seleccionado -->
          <div v-if="selectedFile" class="mt-3 flex items-center bg-blue-50/50 p-3 rounded-lg border border-blue-100">
            <svg class="w-5 h-5 text-blue-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"></path></svg>
            <span class="text-sm font-semibold text-slate-700 truncate flex-1">{{ selectedFile.name }}</span>
            <span class="text-xs font-medium text-slate-500 ml-2">({{ (selectedFile.size / 1024 / 1024).toFixed(2) }} MB)</span>
          </div>
        </div>

        <!-- Alerta de Error -->
        <Transition name="slide-fade">
          <div v-if="errorMessage" class="rounded-xl bg-red-50/80 backdrop-blur-md p-3 border border-red-200/50 shadow-sm">
            <p class="text-sm font-semibold text-red-700 text-center">{{ errorMessage }}</p>
          </div>
        </Transition>

        <!-- Alerta de Éxito -->
        <Transition name="slide-fade">
          <div v-if="successMessage" class="rounded-xl bg-green-50/80 backdrop-blur-md p-3 border border-green-200/50 shadow-sm">
            <p class="text-sm font-semibold text-green-700 text-center">{{ successMessage }}</p>
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
            class="relative px-6 py-2.5 bg-blue-600 rounded-xl text-white font-bold hover:bg-blue-700 hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-blue-600/30 transition-all duration-300 disabled:opacity-50"
            :disabled="isLoading || !selectedFile"
          >
            <span class="flex items-center justify-center">
              <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ isLoading ? 'Subiendo...' : 'Entregar Evidencia' }}
            </span>
          </button>
        </div>
      </form>
    </template>

  </BasePremiumModal>
</template>
