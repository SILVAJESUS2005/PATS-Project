<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import EvaluateModal from '../components/EvaluateModal.vue'
import UploadEvidenceModal from '../components/UploadEvidenceModal.vue'

const router = useRouter()
const route = useRoute()
const isLoaded = ref(false)

const activeTab = ref('proximamente')
const searchQuery = ref('')

const showEvaluateModal = ref(false)
const showUploadModal = ref(false)
const selectedPortafolio = ref('')
const selectedPortafolioId = ref(null)

const claseNombre = ref(route.query.claseNombre || 'Clase')
const claseId = route.query.claseId

const tareas = ref([])
const errorMessage = ref('')
const isLoadingPortafolios = ref(false)

const fetchPortafolios = async () => {
  if (!claseId) {
    errorMessage.value = "No se ha seleccionado ninguna clase."
    setTimeout(() => isLoaded.value = true, 100)
    return
  }

  isLoadingPortafolios.value = true
  try {
    const response = await fetch(`http://localhost:8080/api/portafolios/clase/${claseId}`, {
      credentials: 'include'
    })
    
    if (!response.ok) {
      throw new Error('Error al obtener los portafolios de la clase.')
    }
    
    const data = await response.json()
    formatPortafolios(data)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoadingPortafolios.value = false
    setTimeout(() => isLoaded.value = true, 100)
  }
}

onMounted(() => {
  fetchPortafolios()
})

const formatPortafolios = (portafoliosReales) => {
  const grupos = {}
  const ahora = new Date()
  
  portafoliosReales.forEach(p => {
    const fechaLim = new Date(p.fechaLimite)
    const vencido = fechaLim < ahora
    
    let estado = 'proximamente'
    let iconColor = 'text-blue-500'
    let bgColor = 'bg-blue-50'
    let subtitulo = 'Por entregar'

    if (p.entregado) {
      estado = 'completado'
      iconColor = 'text-green-500'
      bgColor = 'bg-green-50'
      subtitulo = 'Completado'
    } else if (vencido) {
      estado = 'vencido'
      iconColor = 'text-red-500'
      bgColor = 'bg-red-50'
      subtitulo = 'Vencido'
    }

    const dateStr = p.fechaLimite ? fechaLim.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }) : 'Sin fecha'
    
    // Si hay varias tareas en el mismo día con distinto estado, el subtitulo principal puede variar, pero las tareas individuales se pintan bien
    if (!grupos[dateStr]) {
      grupos[dateStr] = {
        fechaGrupo: dateStr,
        subtitulo: subtitulo,
        vencido: vencido,
        items: []
      }
    }
    
    grupos[dateStr].items.push({
      id: p.id,
      title: p.titulo,
      classCode: claseNombre.value,
      estado: estado,
      iconColor: iconColor,
      bgColor: bgColor
    })
  })
  
  tareas.value = Object.values(grupos)
}

const goBack = () => {
  router.push('/dashboard')
}

const openModal = (tarea) => {
  const userRole = localStorage.getItem('user_role')
  selectedPortafolio.value = tarea.title
  selectedPortafolioId.value = tarea.id
  
  if (userRole === 'USER') {
    if (tarea.estado === 'completado') {
      // Optional: Podríamos evitar que abra si ya entregó, pero por ahora lo dejamos
      showUploadModal.value = true
    } else {
      showUploadModal.value = true
    }
  } else {
    showEvaluateModal.value = true
  }
}

const handleEvaluated = (data) => {
  console.log('Evaluación guardada:', data)
}

const handleUploaded = () => {
  console.log('Evidencia subida correctamente al portafolio', selectedPortafolioId.value)
  // Refrescar la lista silenciosamente para que la tarjeta se mueva a "Completado"
  fetchPortafolios()
}
</script>

<template>
  <div class="relative min-h-[calc(100vh-4rem)] bg-slate-50 font-sans">
    
    <!-- Elementos Decorativos del Fondo -->
    <div class="absolute top-0 left-0 w-full h-96 bg-gradient-to-b from-blue-100/50 to-transparent pointer-events-none"></div>

    <div class="relative max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 z-10">
      
      <!-- HEADER -->
      <div 
        class="flex items-center justify-between mb-8 transition-all duration-700 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : '-translate-y-5 opacity-0'"
      >
        <div class="flex items-center gap-4">
          <button 
            @click="goBack"
            class="p-2 rounded-full hover:bg-slate-200 text-slate-500 hover:text-slate-800 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-slate-900 rounded-lg text-white">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
              </svg>
            </div>
            <div>
              <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">Portafolios</h1>
              <p class="text-sm font-semibold text-slate-500 mt-1" v-if="claseId">{{ claseNombre }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- BARRA DE NAVEGACIÓN TABS & BUSCADOR -->
      <div 
        class="flex flex-col md:flex-row justify-between items-center mb-10 border-b border-slate-300 pb-2 transition-all duration-700 delay-100 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-5 opacity-0'"
      >
        <div class="flex space-x-8 mb-4 md:mb-0">
          <button 
            @click="activeTab = 'proximamente'"
            class="pb-2 text-sm font-bold transition-colors duration-200"
            :class="activeTab === 'proximamente' ? 'text-slate-900 border-b-4 border-slate-900' : 'text-slate-400 hover:text-slate-600'"
          >
            Próximamente
          </button>
          <button 
            @click="activeTab = 'vencido'"
            class="pb-2 text-sm font-bold transition-colors duration-200"
            :class="activeTab === 'vencido' ? 'text-slate-900 border-b-4 border-slate-900' : 'text-slate-400 hover:text-slate-600'"
          >
            Vencido
          </button>
          <button 
            @click="activeTab = 'completado'"
            class="pb-2 text-sm font-bold transition-colors duration-200"
            :class="activeTab === 'completado' ? 'text-slate-900 border-b-4 border-slate-900' : 'text-slate-400 hover:text-slate-600'"
          >
            Completado
          </button>
        </div>

        <div class="relative w-full md:w-64">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg class="h-4 w-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <input 
            type="text" 
            v-model="searchQuery"
            class="block w-full pl-9 pr-3 py-1.5 border border-slate-300 rounded-full text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-slate-900 bg-white shadow-sm" 
            placeholder="Buscar por nombre..."
          >
        </div>
      </div>

      <!-- ESTADO VACÍO / CARGANDO -->
      <div v-if="isLoadingPortafolios" class="flex flex-col items-center justify-center py-20">
        <svg class="animate-spin h-10 w-10 text-blue-600 mb-4" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p class="text-slate-500 font-medium text-sm">Cargando portafolios...</p>
      </div>

      <div v-else-if="errorMessage || tareas.length === 0" class="flex flex-col items-center justify-center py-20 text-center transition-all duration-500 transform" :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'">
        <div class="w-24 h-24 bg-slate-100 rounded-full flex items-center justify-center mb-6 shadow-inner">
          <svg class="w-12 h-12 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        </div>
        <h3 class="text-xl font-bold text-slate-800 mb-2">{{ errorMessage || 'No hay portafolios' }}</h3>
        <p class="text-slate-500 max-w-md">{{ !errorMessage ? 'Esta clase aún no tiene tareas asignadas o no se encontraron coincidencias.' : 'Regresa al panel y selecciona una materia válida para continuar.' }}</p>
        
        <button v-if="errorMessage" @click="goBack" class="mt-8 px-6 py-2.5 bg-slate-900 text-white rounded-xl font-bold hover:bg-slate-800 transition-colors shadow-sm focus:ring-4 focus:ring-slate-900/30">
          Volver al Dashboard
        </button>
      </div>

      <!-- LISTA DE TAREAS (GROUPS) -->
      <div v-else class="space-y-10">
        <template v-for="(grupo, gIndex) in tareas" :key="gIndex">
          <div 
            v-if="grupo.items.some(item => item.estado === activeTab)"
            class="transition-all duration-500 transform"
            :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'"
            :style="{ transitionDelay: `${gIndex * 100 + 100}ms` }"
          >
            <!-- Título del Grupo (Fecha) -->
            <div class="flex items-baseline gap-2 mb-4">
              <h2 class="text-xl font-bold text-slate-900">{{ grupo.fechaGrupo }}</h2>
              <span class="text-sm font-semibold text-slate-500">{{ grupo.subtitulo }}</span>
            </div>

            <!-- Filas de Tareas -->
            <div class="space-y-3">
              <div 
                v-for="tarea in grupo.items.filter(i => i.estado === activeTab)" 
                :key="tarea.id"
                class="group flex items-center justify-between p-4 bg-white/70 backdrop-blur-md border border-slate-200/60 rounded-xl shadow-[0_2px_10px_rgba(0,0,0,0.02)] hover:shadow-[0_8px_30px_rgba(0,0,0,0.08)] hover:-translate-y-0.5 transition-all duration-300 cursor-pointer"
                @click="openModal(tarea)"
              >
                <!-- Lado Izquierdo -->
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 flex items-center justify-center rounded-xl border border-slate-100 shadow-inner" :class="tarea.bgColor">
                    <svg class="w-6 h-6" :class="tarea.iconColor" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                  <div>
                    <h3 class="text-base font-bold transition-colors" :class="tarea.estado === 'vencido' ? 'text-red-600 group-hover:text-red-700' : 'text-blue-600 group-hover:text-blue-700'">
                      {{ tarea.title }}
                    </h3>
                    <p class="text-xs font-bold text-slate-400 mt-0.5 tracking-wide">{{ tarea.classCode }}</p>
                  </div>
                </div>

                <!-- Lado Derecho: Acciones -->
                <div class="flex items-center gap-2 opacity-50 group-hover:opacity-100 transition-opacity duration-300">
                  <button class="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                    </svg>
                  </button>
                  <button class="p-2 text-slate-400 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
            
          </div>
        </template>
      </div>

      <!-- Modal de Evaluación para Docentes -->
      <EvaluateModal 
        :show="showEvaluateModal"
        :portafolioName="selectedPortafolio"
        @close="showEvaluateModal = false"
        @evaluated="handleEvaluated"
      />

      <!-- Modal de Subida de Evidencia para Alumnos -->
      <UploadEvidenceModal
        :show="showUploadModal"
        :portafolioName="selectedPortafolio"
        :portafolioId="selectedPortafolioId"
        @close="showUploadModal = false"
        @uploaded="handleUploaded"
      />

    </div>
  </div>
</template>
