<script setup>
import { ref, watch, onMounted } from 'vue'
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
  }
})

const emit = defineEmits(['close', 'evaluated'])

const entrega = ref(null)
const calificacion = ref('')
const feedback = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const isFetching = ref(false)

watch(() => props.show, (newVal) => {
  if (newVal && props.entregaId) {
    calificacion.value = ''
    feedback.value = ''
    errorMessage.value = ''
    fetchEntregaData()
  }
})

const fetchEntregaData = async () => {
  isFetching.value = true
  errorMessage.value = ''
  try {
    const res = await fetch(`http://localhost:8080/api/entregas/${props.entregaId}`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error("No se pudo cargar la entrega")
    const data = await res.json()
    entrega.value = data
    if (data.calificacion !== null) calificacion.value = data.calificacion
    if (data.comentarios) feedback.value = data.comentarios
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isFetching.value = false
  }
}

const closeModal = () => {
  entrega.value = null
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
    const res = await fetch(`http://localhost:8080/api/entregas/${props.entregaId}/calificar`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        calificacion: calificacion.value,
        comentarios: feedback.value
      }),
      credentials: 'include'
    })

    if (!res.ok) {
      const errorTxt = await res.text()
      throw new Error(errorTxt || 'No se pudo guardar la calificación.')
    }

    emit('evaluated', { 
      entregaId: props.entregaId, 
      calificacion: calificacion.value, 
      feedback: feedback.value 
    })
    closeModal()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

const getArchivosPorCategoria = (categoria) => {
  if (!entrega.value || !entrega.value.archivos) return []
  return entrega.value.archivos.filter(a => a.categoriaEvidencia === categoria)
}

const enlargedImageUrl = ref(null)

const enlargeImage = (url) => {
  enlargedImageUrl.value = url
}

const closeEnlargedImage = () => {
  enlargedImageUrl.value = null
}
</script>

<template>
  <BasePremiumModal :show="show" @close="closeModal" maxWidthClass="max-w-6xl">
    <template #header>
      <div class="border-b border-slate-100 pb-3 w-full pr-8">
        <h3 class="text-xl font-bold text-slate-800 tracking-tight">
          Calificar Entregable Estructurado - <span class="text-blue-600 font-extrabold">{{ portafolioName }}</span>
        </h3>
        <p class="text-xs font-semibold text-slate-400 mt-1 uppercase tracking-wider">Módulo de Evaluación</p>
      </div>
    </template>

    <template #content>
      <div v-if="isFetching" class="flex justify-center py-20">
        <span class="animate-spin text-blue-600">Cargando portafolio...</span>
      </div>

      <div v-else-if="entrega" class="grid grid-cols-1 lg:grid-cols-12 gap-8 mt-4 h-[75vh]">
        
        <!-- VISOR ESTRUCTURADO DEL PORTAFOLIO (75%) -->
        <div class="lg:col-span-8 flex flex-col border border-slate-200 rounded-2xl bg-white shadow-inner overflow-y-auto p-8 font-sans space-y-12">
          
          <!-- A) Portada Institucional Dinámica -->
          <div class="text-center border-b-2 border-slate-900 pb-10">
            <h1 class="text-3xl font-black text-slate-900 tracking-tight uppercase">Instituto Tecnológico - PATS</h1>
            <h2 class="text-xl font-bold text-slate-600 mt-2">{{ portafolioName }}</h2>
            
            <div class="mt-8 inline-block text-left bg-slate-50 p-6 rounded-xl border border-slate-200">
              <p class="text-lg text-slate-800"><span class="font-bold text-slate-500">Alumno:</span> {{ entrega.alumno?.nombre }}</p>
              <p class="text-lg text-slate-800 mt-2"><span class="font-bold text-slate-500">Matrícula:</span> {{ entrega.alumno?.matricula || 'N/A' }}</p>
              <p class="text-sm text-slate-500 mt-4">Entregado el: {{ new Date(entrega.fechaEntrega).toLocaleString() }}</p>
            </div>
          </div>

          <!-- B) Introducción -->
          <div>
            <h3 class="text-2xl font-bold text-slate-800 border-b pb-2 mb-4">Introducción</h3>
            <p class="text-slate-700 leading-relaxed whitespace-pre-wrap">{{ entrega.introduccion || 'Sin introducción.' }}</p>
          </div>

          <!-- C) Rejillas de Evidencias Físicas -->
          <div class="space-y-8">
            <h3 class="text-2xl font-bold text-slate-800 border-b pb-2">Evidencias Fotográficas</h3>
            
            <!-- Actividades de Clase -->
            <div v-if="getArchivosPorCategoria('ACTIVIDADES_CLASE').length">
              <h4 class="text-lg font-bold text-blue-800 bg-blue-50 py-2 px-4 rounded-lg mb-4">Actividades de Clase</h4>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <img v-for="(arch, i) in getArchivosPorCategoria('ACTIVIDADES_CLASE')" :key="i" :src="`http://localhost:8080/api/entregas/descargar/${arch.archivoUrl}`" class="w-full h-48 object-cover rounded-xl shadow border border-slate-200 hover:scale-105 transition cursor-pointer" alt="Evidencia" @click="enlargeImage(`http://localhost:8080/api/entregas/descargar/${arch.archivoUrl}`)">
              </div>
            </div>

            <!-- Tareas de Casa -->
            <div v-if="getArchivosPorCategoria('TAREAS_CASA').length">
              <h4 class="text-lg font-bold text-green-800 bg-green-50 py-2 px-4 rounded-lg mb-4 mt-6">Tareas en Casa</h4>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <img v-for="(arch, i) in getArchivosPorCategoria('TAREAS_CASA')" :key="i" :src="`http://localhost:8080/api/entregas/descargar/${arch.archivoUrl}`" class="w-full h-48 object-cover rounded-xl shadow border border-slate-200 hover:scale-105 transition cursor-pointer" alt="Evidencia" @click="enlargeImage(`http://localhost:8080/api/entregas/descargar/${arch.archivoUrl}`)">
              </div>
            </div>

            <!-- Examen / Proyecto -->
            <div v-if="getArchivosPorCategoria('EXAMEN_PROYECTO').length">
              <h4 class="text-lg font-bold text-purple-800 bg-purple-50 py-2 px-4 rounded-lg mb-4 mt-6">Examen / Proyecto</h4>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <img v-for="(arch, i) in getArchivosPorCategoria('EXAMEN_PROYECTO')" :key="i" :src="`http://localhost:8080/api/entregas/descargar/${arch.archivoUrl}`" class="w-full h-48 object-cover rounded-xl shadow border border-slate-200 hover:scale-105 transition cursor-pointer" alt="Evidencia" @click="enlargeImage(`http://localhost:8080/api/entregas/descargar/${arch.archivoUrl}`)">
              </div>
            </div>
          </div>

          <!-- D) Conclusión -->
          <div class="pb-12">
            <h3 class="text-2xl font-bold text-slate-800 border-b pb-2 mb-4">Conclusiones</h3>
            <p class="text-slate-700 leading-relaxed whitespace-pre-wrap">{{ entrega.conclusion || 'Sin conclusiones.' }}</p>
          </div>
        </div>

        <!-- FORMULARIO DE EVALUACIÓN (25%) -->
        <div class="lg:col-span-4 flex flex-col h-full bg-slate-50 p-6 rounded-2xl border border-slate-200">
          <form @submit.prevent="submitEvaluacion" class="flex flex-col h-full justify-between">
            <div class="space-y-6">
              
              <div>
                <label class="block text-sm font-bold text-slate-700 mb-2">Puntuación (0-100)</label>
                <div class="relative">
                  <input type="number" v-model="calificacion" min="0" max="100" class="block w-full pl-4 pr-16 py-4 border-2 border-slate-200 rounded-xl bg-white text-slate-800 focus:border-blue-500 focus:ring-4 font-black text-3xl shadow-sm" placeholder="Ej. 95" required>
                  <span class="absolute right-4 top-1/2 -translate-y-1/2 font-extrabold text-slate-400 text-xl">/ 100</span>
                </div>
              </div>

              <div>
                <label class="block text-sm font-bold text-slate-700 mb-2">Retroalimentación del Docente</label>
                <textarea v-model="feedback" rows="8" class="block w-full px-4 py-3 border-2 border-slate-200 rounded-xl bg-white text-slate-800 focus:border-blue-500 focus:ring-4 shadow-sm resize-none text-sm" placeholder="Escribe un comentario detallado sobre este portafolio..." required></textarea>
              </div>

            </div>

            <div class="space-y-3 mt-6">
              <Transition name="slide-fade">
                <div v-if="errorMessage" class="rounded-xl bg-red-50 p-3 border border-red-200">
                  <p class="text-sm font-semibold text-red-700 text-center">{{ errorMessage }}</p>
                </div>
              </Transition>

              <button type="submit" class="w-full py-4 bg-slate-900 rounded-xl text-white font-black hover:bg-slate-800 transition-all shadow-lg disabled:opacity-50 text-lg tracking-wide" :disabled="isLoading">
                {{ isLoading ? 'Guardando...' : 'Asentar Calificación' }}
              </button>
              <button type="button" @click="closeModal" class="w-full py-3 text-slate-500 font-bold hover:bg-slate-200 rounded-xl transition-all">Cancelar</button>
            </div>
          </form>
        </div>

      </div>
    </template>
  </BasePremiumModal>

  <!-- Image Enlargement Overlay -->
  <div v-if="enlargedImageUrl" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 backdrop-blur-md" @click="closeEnlargedImage">
    <button class="absolute top-6 right-6 text-white hover:text-gray-300 bg-black/50 p-2 rounded-full transition-colors" @click="closeEnlargedImage">
      <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
    </button>
    <img :src="enlargedImageUrl" class="max-w-[95vw] max-h-[95vh] object-contain rounded-lg shadow-2xl" @click.stop />
  </div>
</template>
