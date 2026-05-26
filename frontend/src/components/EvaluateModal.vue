<script setup>
import { ref, watch } from 'vue'
import BasePremiumModal from './BasePremiumModal.vue'

const props = defineProps({
  show: Boolean,
  portafolioName: {
    type: String,
    default: 'Portafolio'
  },
  entregaId: {
    type: [Number, String],
    required: false
  },
  archivoUrl: {
    type: String,
    required: false,
    default: ''
  }
})

const emit = defineEmits(['close', 'evaluated'])

const calificacion = ref('')
const feedback = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

// Si se abre el modal, reiniciamos estados
watch(() => props.show, (newVal) => {
  if (newVal) {
    calificacion.value = ''
    feedback.value = ''
    errorMessage.value = ''
  }
})

const closeModal = () => {
  calificacion.value = ''
  feedback.value = ''
  errorMessage.value = ''
  emit('close')
}

const submitEvaluacion = async () => {
  if (calificacion.value === '' || calificacion.value < 0 || calificacion.value > 100) {
    errorMessage.value = "Por favor, ingresa una calificación válida (0-100)."
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  
  try {
    // ---------------------------------------------------------
    // CONEXIÓN REAL AL BACKEND DE AZURE:
    // ---------------------------------------------------------
    const res = await fetch(`http://localhost:8080/api/entregas/${props.entregaId}/calificar`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        calificacion: calificacion.value,
        comentarios: feedback.value
      }),
      credentials: 'include' // Obligatorio para compartir la sesión/cookie
    })

    if (!res.ok) {
      const errorTxt = await res.text()
      throw new Error(errorTxt || 'No se pudo guardar la calificación.')
    }

    const data = await res.json()
    emit('evaluated', { 
      entregaId: props.entregaId, 
      calificacion: calificacion.value, 
      feedback: feedback.value 
    })
    closeModal()
    // ---------------------------------------------------------
  } catch (error) {
    errorMessage.value = error.message || "Ocurrió un error al guardar la calificación."
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <BasePremiumModal :show="show" @close="closeModal" maxWidthClass="max-w-5xl">
    
    <template #header>
      <div class="border-b border-slate-100 pb-3 w-full pr-8">
        <h3 class="text-xl font-bold text-slate-800 tracking-tight">
          Calificar Entregable - <span class="text-blue-600 font-extrabold">{{ portafolioName }}</span>
        </h3>
        <p class="text-xs font-semibold text-slate-400 mt-1 uppercase tracking-wider">Módulo de Evaluación de Evidencias</p>
      </div>
    </template>

    <template #content>
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 mt-4">
        
        <!-- COLUMNA IZQUIERDA: Visor de Archivos (60% del ancho) -->
        <div class="lg:col-span-7 flex flex-col h-[500px] border border-slate-200 rounded-2xl overflow-hidden bg-slate-100 shadow-inner">
          <div class="bg-slate-50 border-b border-slate-200 px-4 py-3 flex items-center justify-between">
            <span class="text-xs font-bold text-slate-600 flex items-center gap-2">
              <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              Vista Previa del Entregable
            </span>
            <a 
              v-if="archivoUrl" 
              :href="`http://localhost:8080${archivoUrl}`" 
              target="_blank" 
              class="text-xs font-bold text-blue-600 hover:text-blue-700 hover:underline flex items-center gap-1"
            >
              Abrir en pestaña nueva ↗
            </a>
          </div>
          
          <div class="flex-1 relative flex items-center justify-center bg-slate-200/50">
            <!-- Iframe para visualizador de PDF/Imagen/Archivo -->
            <iframe 
              v-if="archivoUrl" 
              :src="`http://localhost:8080${archivoUrl}`" 
              class="w-full h-full border-none bg-white"
            ></iframe>
            
            <!-- Si no hay archivo -->
            <div v-else class="text-center p-8">
              <span class="text-4xl animate-pulse">📂</span>
              <p class="text-sm font-bold text-slate-500 mt-2">Cargando archivo del alumno...</p>
            </div>
          </div>
        </div>

        <!-- COLUMNA DERECHA: Formulario de Evaluación (40% del ancho) -->
        <div class="lg:col-span-5 flex flex-col h-[500px]">
          <form @submit.prevent="submitEvaluacion" class="flex flex-col h-full justify-between">
            
            <div class="space-y-5">
              <!-- Información del alumno -->
              <div class="bg-blue-50/50 border border-blue-100 rounded-xl p-4">
                <span class="text-xs font-bold uppercase tracking-wider text-blue-500">Estado del Envío</span>
                <p class="text-sm font-bold text-slate-800 mt-1">Listo para calificar</p>
                <p class="text-xs font-medium text-slate-500 mt-0.5">El alumno ha entregado la evidencia correctamente.</p>
              </div>

              <!-- Campo de Calificación -->
              <div>
                <label for="calificacion" class="block text-sm font-bold text-slate-700 mb-2">Puntuación (0-100)</label>
                <div class="relative">
                  <input 
                    type="number" 
                    id="calificacion" 
                    v-model="calificacion"
                    min="0"
                    max="100"
                    class="block w-full pl-4 pr-16 py-3 border-2 border-slate-100 rounded-xl leading-5 bg-slate-50 text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all duration-300 font-extrabold text-xl shadow-inner" 
                    placeholder="Ej. 95"
                    required
                  >
                  <span class="absolute right-4 top-1/2 -translate-y-1/2 font-extrabold text-slate-400 text-lg">/ 100</span>
                </div>
              </div>

              <!-- Campo de Retroalimentación Justificado -->
              <div>
                <label for="feedback" class="block text-sm font-bold text-slate-700 mb-2">Retroalimentación / Comentarios</label>
                <textarea 
                  id="feedback" 
                  v-model="feedback"
                  rows="4"
                  class="block w-full px-4 py-3 border-2 border-slate-100 rounded-xl leading-relaxed bg-slate-50 text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all duration-300 shadow-inner resize-none text-sm text-justify font-sans" 
                  placeholder="Redacta comentarios detallados y constructivos sobre el desempeño del alumno en esta evidencia escolar..."
                  required
                ></textarea>
              </div>
            </div>

            <!-- Alerta de Error y Botones -->
            <div class="space-y-3">
              <Transition name="slide-fade">
                <div v-if="errorMessage" class="rounded-xl bg-red-50/80 backdrop-blur-md p-3 border border-red-200/50 shadow-sm">
                  <p class="text-sm font-semibold text-red-700 text-center">{{ errorMessage }}</p>
                </div>
              </Transition>

              <div class="flex items-center space-x-3 pt-3 border-t border-slate-100">
                <button 
                  type="button" 
                  @click="closeModal"
                  class="flex-1 px-6 py-3 rounded-xl text-slate-500 font-bold hover:bg-slate-100 hover:text-slate-800 focus:outline-none transition-all duration-200 text-center"
                >
                  Cancelar
                </button>
                <button 
                  type="submit" 
                  class="flex-1 relative px-6 py-3 bg-blue-600 rounded-xl text-white font-bold hover:bg-blue-700 hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-blue-600/30 transition-all duration-300 disabled:opacity-50 text-center"
                  :disabled="isLoading"
                >
                  <span class="flex items-center justify-center">
                    <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    {{ isLoading ? 'Guardando...' : 'Calificar' }}
                  </span>
                </button>
              </div>
            </div>

          </form>
        </div>

      </div>
    </template>

  </BasePremiumModal>
</template>
