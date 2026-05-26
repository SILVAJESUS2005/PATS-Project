<script setup>
import { ref } from 'vue'
import BasePremiumModal from './BasePremiumModal.vue'

const props = defineProps({
  show: Boolean,
  entregaId: [Number, String],
  portafolioName: {
    type: String,
    default: 'Portafolio'
  }
})

const emit = defineEmits(['close', 'evaluated'])

const calificacion = ref('')
const feedback = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

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
    // -----------------------------------------------------------------------------------------
    // INTEGRANTE 4: Tarea 4 - Conectar el botón "Entregar" con el endpoint CalificarEntregaDTO
    // Aquí debes hacer el fetch PUT a /api/entregas/${props.entregaId}/calificar
    // -----------------------------------------------------------------------------------------
    
    // Simulación temporal para que no marque error mientras el Integrante 4 lo programa
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    emit('evaluated')
    closeModal()
  } catch (error) {
    errorMessage.value = error.message || "Ocurrió un error al guardar la calificación."
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <BasePremiumModal :show="show" @close="closeModal" maxWidthClass="max-w-lg">
    
    <template #header>
      <h3 class="text-xl font-bold text-slate-800 tracking-tight border-b border-slate-100 pb-2 w-full pr-4">
        Calificación - <span class="text-blue-600 font-extrabold">{{ portafolioName }}</span>
      </h3>
    </template>

    <template #content>
      <!-- ========================================================================= -->
      <!-- INTEGRANTE 4: Tarea 2 - Mostrar aquí el visualizador del archivo entregado -->
      <!-- ========================================================================= -->
      
      <form @submit.prevent="submitEvaluacion" class="space-y-6 mt-4">
        
        <!-- ========================================================================= -->
        <!-- INTEGRANTE 4: Tarea 3 - Formulario de calificación (Comentarios y Nota) -->
        <!-- ========================================================================= -->
        
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
              class="block w-full pl-4 pr-4 py-3 border-2 border-slate-100 rounded-xl leading-5 bg-slate-50 text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all duration-300 font-bold text-lg shadow-inner" 
              placeholder="Ej. 95"
              required
            >
          </div>
        </div>

        <!-- Campo de Retroalimentación -->
        <div>
          <label for="feedback" class="block text-sm font-bold text-slate-700 mb-2">Retroalimentación (Opcional)</label>
          <div class="relative">
            <textarea 
              id="feedback" 
              v-model="feedback"
              rows="3"
              class="block w-full pl-4 pr-4 py-3 border-2 border-slate-100 rounded-xl leading-5 bg-slate-50 text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all duration-300 shadow-inner resize-none" 
              placeholder="Buen trabajo en la estructura del código..."
            ></textarea>
          </div>
        </div>

        <!-- Alerta de Error -->
        <Transition name="slide-fade">
          <div v-if="errorMessage" class="rounded-xl bg-red-50/80 backdrop-blur-md p-3 border border-red-200/50 shadow-sm">
            <p class="text-sm font-semibold text-red-700 text-center">{{ errorMessage }}</p>
          </div>
        </Transition>

        <!-- Botones -->
        <div class="mt-8 pt-2 flex justify-end space-x-3">
          <button 
            type="button" 
            @click="closeModal"
            class="px-6 py-2.5 rounded-xl text-slate-500 font-bold hover:bg-slate-100 hover:text-slate-800 focus:outline-none transition-all duration-200"
          >
            Cancelar
          </button>
          <button 
            type="submit" 
            class="relative px-6 py-2.5 bg-blue-600 rounded-xl text-white font-bold hover:bg-blue-700 hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-blue-600/30 transition-all duration-300 disabled:opacity-50"
            :disabled="isLoading"
          >
            <span class="flex items-center justify-center">
              <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ isLoading ? 'Guardando...' : 'Guardar calificación' }}
            </span>
          </button>
        </div>
      </form>
    </template>

  </BasePremiumModal>
</template>
