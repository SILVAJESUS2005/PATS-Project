<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EvaluateModal from '../components/EvaluateModal.vue'

const route = useRoute()
const router = useRouter()

const portafolioId = route.params.id
const isLoaded = ref(false)
const alumnos = ref([])
const errorMessage = ref('')

const showEvaluateModal = ref(false)
const selectedEntregaId = ref(null)
const selectedAlumnoName = ref('')

const fetchEntregas = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/entregas/portafolio/${portafolioId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    })
    
    if (response.ok) {
      alumnos.value = await response.json()
    } else {
      errorMessage.value = "Error al cargar la lista de alumnos. Asegúrate de tener permisos."
    }
  } catch (error) {
    errorMessage.value = "Error de conexión al servidor."
    console.error(error)
  }
}

onMounted(() => {
  fetchEntregas()
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})

const goBack = () => {
  router.back()
}

const openEvaluateModal = (alumnoName, entregaId) => {
  selectedAlumnoName.value = alumnoName
  selectedEntregaId.value = entregaId
  showEvaluateModal.value = true
}

const handleEvaluated = () => {
  // Cuando se califica exitosamente, recargamos la tabla
  fetchEntregas()
}

// Para descargar el archivo
const descargarEvidencia = (archivoUrl) => {
  window.open(`http://localhost:8080${archivoUrl}`, '_blank')
}
</script>

<template>
  <div class="relative min-h-[calc(100vh-4rem)] bg-slate-50 font-sans">
    
    <!-- Elementos Decorativos -->
    <div class="absolute top-0 left-0 w-full h-96 bg-gradient-to-b from-blue-100/50 to-transparent pointer-events-none"></div>

    <div class="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 z-10">
      
      <!-- HEADER -->
      <div 
        class="flex flex-col md:flex-row items-start md:items-center justify-between mb-10 transition-all duration-700 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : '-translate-y-5 opacity-0'"
      >
        <div class="flex items-center gap-4 mb-4 md:mb-0">
          <button 
            @click="goBack"
            class="p-2 rounded-full hover:bg-slate-200 text-slate-500 hover:text-slate-800 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <div>
            <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">Entregas de Alumnos</h1>
            <p class="text-slate-500 font-medium mt-1">Revisa y califica las evidencias enviadas</p>
          </div>
        </div>
      </div>

      <!-- ERROR MSG -->
      <div v-if="errorMessage" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 font-medium">
        {{ errorMessage }}
      </div>

      <!-- TABLA (Pantalla 16) -->
      <div 
        class="bg-white border border-slate-200 shadow-xl shadow-slate-200/50 rounded-2xl overflow-hidden transition-all duration-700 delay-100 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'"
      >
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-xs uppercase tracking-wider text-slate-500 font-bold">
                <th class="py-4 px-6">Alumno</th>
                <th class="py-4 px-6 text-center">Estado</th>
                <th class="py-4 px-6 text-center">Calificación</th>
                <th class="py-4 px-6 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr 
                v-for="alumno in alumnos" 
                :key="alumno.alumnoId"
                class="hover:bg-slate-50/50 transition-colors"
              >
                <!-- Alumno (Nombre y Correo) -->
                <td class="py-4 px-6">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-100 to-indigo-100 text-blue-700 font-bold flex items-center justify-center border border-blue-200">
                      {{ alumno.nombreAlumno.charAt(0).toUpperCase() }}
                    </div>
                    <div>
                      <p class="font-bold text-slate-800">{{ alumno.nombreAlumno }}</p>
                      <p class="text-xs text-slate-500">{{ alumno.correo }}</p>
                    </div>
                  </div>
                </td>

                <!-- Estado de Entrega -->
                <td class="py-4 px-6 text-center">
                  <span 
                    v-if="alumno.entregado"
                    class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-green-50 text-green-700 border border-green-200"
                  >
                    <div class="w-2 h-2 rounded-full bg-green-500"></div>
                    Entregado
                  </span>
                  <span 
                    v-else
                    class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-red-50 text-red-700 border border-red-200"
                  >
                    <div class="w-2 h-2 rounded-full bg-red-500"></div>
                    Sin entregar
                  </span>
                </td>

                <!-- Calificación -->
                <td class="py-4 px-6 text-center font-bold">
                  <span v-if="alumno.calificacion !== null && alumno.calificacion !== undefined" class="text-blue-600 bg-blue-50 px-3 py-1 rounded-lg border border-blue-100">
                    {{ alumno.calificacion }} / 100
                  </span>
                  <span v-else class="text-slate-400 text-sm">--</span>
                </td>

                <!-- Acciones (Ver / Calificar) -->
                <td class="py-4 px-6 text-right">
                  <div class="flex items-center justify-end gap-2">
                    <button 
                      v-if="alumno.entregado"
                      @click="descargarEvidencia(alumno.archivoUrl)"
                      class="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors tooltip-trigger"
                      title="Descargar Evidencia"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                    </button>
                    
                    <button 
                      v-if="alumno.entregado"
                      @click="openEvaluateModal(alumno.nombreAlumno, alumno.entregaId)"
                      class="px-4 py-2 bg-slate-900 text-white text-xs font-bold rounded-lg hover:bg-slate-800 transition-colors shadow-sm"
                    >
                      {{ alumno.calificacion !== null ? 'Modificar Nota' : 'Calificar' }}
                    </button>
                    
                    <span v-if="!alumno.entregado" class="text-xs text-slate-400 italic">
                      Esperando...
                    </span>
                  </div>
                </td>
              </tr>
              
              <tr v-if="alumnos.length === 0 && !errorMessage">
                <td colspan="4" class="py-10 text-center text-slate-500">
                  No hay alumnos en esta clase aún.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Modal de Evaluación -->
      <EvaluateModal 
        :show="showEvaluateModal"
        :entregaId="selectedEntregaId"
        :portafolioName="selectedAlumnoName"
        @close="showEvaluateModal = false"
        @evaluated="handleEvaluated"
      />

    </div>
  </div>
</template>
