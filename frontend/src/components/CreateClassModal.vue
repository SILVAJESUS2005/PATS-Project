<script setup>
import { ref } from 'vue'
import BasePremiumModal from './BasePremiumModal.vue'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['close', 'created'])
const isLoading = ref(false)

const form = ref({
  nombre: '',
  descripcion: ''
})

const errorMessage = ref('')
const successMessage = ref('')
const newClassCode = ref('')

const closeModal = () => {
  form.value.nombre = ''
  form.value.descripcion = ''
  errorMessage.value = ''
  successMessage.value = ''
  newClassCode.value = ''
  emit('close')
}

const crearClase = async () => {
  if (!form.value.nombre.trim()) {
    errorMessage.value = "Por favor, ingresa el nombre de la clase."
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  newClassCode.value = ''
  
  try {
    const response = await fetch('http://localhost:8080/api/clases', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value),
      credentials: 'include'
    })

    const data = await response.json()

    if (!response.ok) {
        // Manejar errores de validación (Map de Spring Boot) o errores generales
        if (typeof data === 'object' && !data.error) {
            const firstError = Object.values(data)[0];
            throw new Error(firstError || 'Error al crear la clase');
        }
        throw new Error(data.error || 'Error al crear la clase')
    }

    successMessage.value = `Clase creada con éxito. Código: ${data.codigoAcceso}`
    newClassCode.value = data.codigoAcceso

    // Emitir el evento al componente padre pasándole la nueva clase
    emit('created', data)

    // Opcional: Cerrar automáticamente después de un momento
    setTimeout(() => {
        closeModal()
    }, 3000)

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
      <h3 class="text-2xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-slate-800 to-slate-600 tracking-tight">Crear nueva clase</h3>
    </template>

    <template #content>
      <p class="text-slate-500 mb-8 text-sm font-medium leading-relaxed">
        Configura los detalles de tu nueva materia para generar un código de acceso único para tus alumnos.
      </p>

      <form @submit.prevent="crearClase" class="space-y-6">
        
        <div class="group/input">
          <label for="nombre" class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 group-focus-within/input:text-blue-500 transition-colors">Nombre de la Materia</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <svg class="h-5 w-5 text-slate-400 group-focus-within/input:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
              </svg>
            </div>
            <input 
              type="text" 
              id="nombre" 
              v-model="form.nombre"
              class="block w-full pl-11 pr-4 py-3.5 border-2 border-slate-100 rounded-xl leading-5 bg-slate-50/50 text-slate-800 placeholder-slate-300 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all duration-300 sm:text-base font-bold shadow-inner" 
              placeholder="Ej. Matemáticas Discretas"
              required
              :disabled="isLoading || successMessage !== ''"
            >
          </div>
        </div>

        <div class="group/input">
          <label for="descripcion" class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 group-focus-within/input:text-blue-500 transition-colors">Descripción (Opcional)</label>
          <div class="relative">
            <textarea 
              id="descripcion" 
              v-model="form.descripcion"
              rows="3"
              class="block w-full px-4 py-3.5 border-2 border-slate-100 rounded-xl leading-5 bg-slate-50/50 text-slate-800 placeholder-slate-300 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all duration-300 sm:text-base shadow-inner resize-none" 
              placeholder="Descripción breve de la materia..."
              :disabled="isLoading || successMessage !== ''"
            ></textarea>
          </div>
        </div>

        <!-- Alerta de Error -->
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

        <!-- Alerta de Éxito -->
        <Transition name="slide-fade">
          <div v-if="successMessage" class="rounded-xl bg-green-50/80 backdrop-blur-md p-4 border border-green-200/50 shadow-sm">
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                  <div class="flex-shrink-0">
                    <div class="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center">
                      <svg class="h-4 w-4 text-green-500" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                      </svg>
                    </div>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm font-semibold text-green-700">
                      {{ successMessage }}
                    </p>
                  </div>
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
          >
            {{ successMessage ? 'Cerrar' : 'Cancelar' }}
          </button>
          <button 
            v-if="!successMessage"
            type="submit" 
            class="relative px-6 py-2.5 bg-slate-900 rounded-xl text-white font-bold hover:bg-black hover:shadow-lg hover:-translate-y-0.5 focus:outline-none focus:ring-4 focus:ring-slate-900/30 transition-all duration-300 disabled:opacity-50 disabled:hover:translate-y-0"
            :disabled="isLoading"
          >
            <span class="flex items-center justify-center">
              <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ isLoading ? 'Creando...' : 'Crear clase' }}
            </span>
          </button>
        </div>
      </form>
    </template>

  </BasePremiumModal>
</template>
