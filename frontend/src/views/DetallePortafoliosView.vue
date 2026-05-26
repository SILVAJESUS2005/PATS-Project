<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import EvaluateModal from '../components/EvaluateModal.vue'
import CreatePortafolioModal from '../components/CreatePortafolioModal.vue'

const router = useRouter()
const isLoaded = ref(false)
const rol = ref(localStorage.getItem('user_role') || 'ALUMNO')

const activeTab = ref('proximamente')
const searchQuery = ref('')
const showEvaluateModal = ref(false)
const showCreatePortafolioModal = ref(false)
const selectedPortafolio = ref('')

// Simulación de datos (Mock Data) estructurada por fechas
const tareas = ref([
  {
    fechaGrupo: '14 may',
    subtitulo: 'Mañana',
    vencido: false,
    items: [
      { id: 101, title: 'Portafolio_PlanMov_U2', classCode: '2026 T1-601 PLANIFICACIÓN DE MOVIMIENTOS', estado: 'proximamente', iconColor: 'text-red-500', bgColor: 'bg-red-50' },
      { id: 102, title: 'Portafolio_U1', classCode: '2026 T1-601 DESARROLLO SUSTENTABLE', estado: 'proximamente', iconColor: 'text-red-500', bgColor: 'bg-red-50' }
    ]
  },
  {
    fechaGrupo: '14 may',
    subtitulo: 'Jueves',
    vencido: false,
    items: [
      { id: 103, title: 'Portafolio_U2', classCode: '2026 T1-601 PROGRAMACIÓN WEB', estado: 'proximamente', iconColor: 'text-green-500', bgColor: 'bg-green-50' }
    ]
  },
  {
    fechaGrupo: '12 may',
    subtitulo: 'Vencido hace 1 día',
    vencido: true,
    items: [
      { id: 201, title: 'Portafolio_PlanMov_U2', classCode: '2026 T1-601 PLANIFICACIÓN DE MOVIMIENTOS', estado: 'vencido', iconColor: 'text-red-500', bgColor: 'bg-red-50' },
      { id: 202, title: 'Portafolio_U1', classCode: '2026 T1-601 TECNOLOGÍAS INALÁMBRICAS', estado: 'vencido', iconColor: 'text-red-500', bgColor: 'bg-red-50' }
    ]
  },
  {
    fechaGrupo: '11 may',
    subtitulo: 'Lunes',
    vencido: false,
    items: [
      { id: 301, title: 'Portafolio_U2', classCode: '2026 T1-601 PLANIFICACIÓN DE MOVIMIENTOS', estado: 'completado', iconColor: 'text-green-500', bgColor: 'bg-green-50' },
      { id: 302, title: 'Portafolio_U1', classCode: '2026 T1-601 PROGRAMACIÓN WEB', estado: 'completado', iconColor: 'text-green-500', bgColor: 'bg-green-50' }
    ]
  },
  {
    fechaGrupo: '10 may',
    subtitulo: 'Domingo',
    vencido: false,
    items: [
      { id: 303, title: 'Portafolio_U1', classCode: '2026 T1-601 TECNOLOGÍAS INALÁMBRICAS', estado: 'completado', iconColor: 'text-green-500', bgColor: 'bg-green-50' }
    ]
  }
])

onMounted(() => {
  setTimeout(() => isLoaded.value = true, 100)
})

const goBack = () => {
  router.push('/dashboard')
}

const openEvaluateModal = (tareaTitle) => {
  selectedPortafolio.value = tareaTitle
  showEvaluateModal.value = true
}

const handleEvaluated = (data) => {
  console.log('Evaluación guardada:', data)
  // Aquí se enviaría el fetch PUT a la API real
}

const handlePortafolioCreated = (data) => {
  console.log('Portafolio creado:', data)
  // Opcional: Podrías hacer un fetch de los portafolios aquí cuando el backend esté integrado
  // Para la tarea 2 solo se pedía la interfaz y conectarlo, lo cual el modal ya hace.
}
</script>

<template>
  <div class="relative min-h-[calc(100vh-4rem)] bg-slate-50 font-sans">
    
    <!-- Elementos Decorativos del Fondo (Mismo estilo del Dashboard) -->
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
            <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">Portafolios</h1>
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

        <div class="flex items-center gap-4 w-full md:w-auto">
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
          
          <button 
            v-if="rol === 'DOCENTE'"
            @click="showCreatePortafolioModal = true"
            class="flex-shrink-0 inline-flex items-center justify-center px-4 py-2 font-bold text-white transition-all duration-300 bg-slate-900 rounded-full hover:bg-slate-800 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2"
          >
            <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Crear Portafolio
          </button>
        </div>
      </div>

      <!-- LISTA DE TAREAS (GROUPS) -->
      <!-- ========================================================================= -->
      <!-- INTEGRANTE 3: Tarea 3 - Mostrar visualmente el estado del portafolio (Próximamente, Completado, Vencido) -->
      <!-- Aquí puedes modificar los colores según el estado (vencido en rojo, etc.) -->
      <!-- ========================================================================= -->
      <div class="space-y-10">
        <!-- Renderizar grupos dinámicamente filtrando por el tab activo -->
        <template v-for="(grupo, gIndex) in tareas" :key="gIndex">
          <div 
            v-if="grupo.items.some(item => item.estado === activeTab)"
            class="transition-all duration-500 transform"
            :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'"
            :style="{ transitionDelay: `${gIndex * 100 + 200}ms` }"
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
                @click="openEvaluateModal(tarea.title)"
              >
                <!-- Lado Izquierdo: Icono + Textos -->
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 flex items-center justify-center rounded-xl border border-slate-100 shadow-inner" :class="tarea.bgColor">
                    <svg class="w-6 h-6" :class="tarea.iconColor" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                  <div>
                    <h3 class="text-base font-bold" :class="tarea.estado === 'vencido' ? 'text-red-600' : 'text-blue-600'">
                      {{ tarea.title }}
                    </h3>
                    <p class="text-xs font-bold text-slate-400 mt-0.5 tracking-wide">{{ tarea.classCode }}</p>
                  </div>
                </div>

                <!-- Lado Derecho: Acciones -->
                <div class="flex items-center gap-2 opacity-50 group-hover:opacity-100 transition-opacity duration-300">
                  
                  <!-- ========================================================================= -->
                  <!-- INTEGRANTE 3: Tarea 2 - Agregar botón de "Subir Archivo" aquí -->
                  <!-- Al dar clic debe abrir el seleccionador de archivos o mandar a /api/entregas/subir -->
                  <!-- ========================================================================= -->

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

      <!-- Modal de Evaluación (Se abre al hacer clic en una fila) -->
      <EvaluateModal 
        :show="showEvaluateModal"
        :portafolioName="selectedPortafolio"
        @close="showEvaluateModal = false"
        @evaluated="handleEvaluated"
      />

      <!-- Modal de Creación de Portafolio para Docentes -->
      <CreatePortafolioModal 
        :show="showCreatePortafolioModal"
        @close="showCreatePortafolioModal = false"
        @created="handlePortafolioCreated"
      />

    </div>
  </div>
</template>
