<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CreatePortafolioModal from '../components/CreatePortafolioModal.vue'

const route = useRoute()
const router = useRouter()

const isLoaded = ref(false)
const rol = ref(localStorage.getItem('user_role') || 'ALUMNO')
const claseId = route.params.id

const nombreClase = ref('Cargando clase...')
const portafolios = ref([])
const showCreatePortafolioModal = ref(false)

const goBack = () => {
  router.push('/dashboard')
}

// Opcional: Obtener detalles de la clase (para el título)
const fetchDetallesClase = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/clases', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    })
    if (response.ok) {
      const clases = await response.json()
      const claseEncontrada = clases.find(c => c.id.toString() === claseId)
      if (claseEncontrada) {
        nombreClase.value = claseEncontrada.nombre
      } else {
        nombreClase.value = 'Detalle de la Clase'
      }
    }
  } catch (error) {
    console.error("Error al cargar detalles de clase:", error)
  }
}

// Obtener portafolios (tareas) de esta clase
const fetchPortafolios = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/portafolios/clase/${claseId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    })
    
    if (response.ok) {
      portafolios.value = await response.json()
    } else {
      console.error("Error obteniendo los portafolios")
    }
  } catch (error) {
    console.error("Error de conexión al backend:", error)
  }
}

onMounted(() => {
  fetchDetallesClase()
  fetchPortafolios()
  
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})

const formatearFecha = (fechaArray) => {
  if (!fechaArray || !Array.isArray(fechaArray)) return 'Sin fecha'
  // Spring Boot a veces devuelve LocalDateTime como arreglo [yyyy, MM, dd, HH, mm]
  const [year, month, day, hour, minute] = fechaArray
  const date = new Date(year, month - 1, day, hour, minute)
  return new Intl.DateTimeFormat('es-MX', { 
    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' 
  }).format(date)
}

const handlePortafolioCreated = (data) => {
  // Refrescar la lista de tareas
  fetchPortafolios()
}

// Navegar a los detalles del portafolio (Tarea 3)
const verEntregas = (portafolioId) => {
  if (rol.value === 'DOCENTE') {
    router.push(`/portafolio/${portafolioId}/entregas`)
  } else {
    // Para el alumno, podrías abrir el modal de subir archivo, etc.
    console.log("Abrir modal de entrega para el alumno")
  }
}

</script>

<template>
  <div class="relative min-h-[calc(100vh-4rem)] bg-slate-50 font-sans">
    
    <!-- Elementos Decorativos -->
    <div class="absolute top-0 left-0 w-full h-96 bg-gradient-to-b from-blue-100/50 to-transparent pointer-events-none"></div>

    <div class="relative max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 z-10">
      
      <!-- HEADER -->
      <div 
        class="flex items-center justify-between mb-10 transition-all duration-700 transform"
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
            <div class="p-3 bg-gradient-to-br from-blue-600 to-indigo-600 rounded-xl text-white shadow-lg">
              <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
              </svg>
            </div>
            <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">{{ nombreClase }}</h1>
          </div>
        </div>

        <!-- Botón Crear Tarea (Exclusivo Docente) -->
        <button 
          v-if="rol === 'DOCENTE'"
          @click="showCreatePortafolioModal = true"
          class="flex-shrink-0 inline-flex items-center justify-center px-6 py-2.5 font-bold text-white transition-all duration-300 bg-slate-900 rounded-full hover:bg-slate-800 hover:shadow-lg focus:outline-none hover:-translate-y-0.5"
        >
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nueva Tarea
        </button>
      </div>

      <!-- LISTA DE PORTAFOLIOS (TAREAS) -->
      <div 
        class="bg-white/60 backdrop-blur-xl border border-white/80 shadow-xl rounded-3xl p-8 transition-all duration-700 delay-100 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'"
      >
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          Portafolios Asignados
        </h2>

        <div v-if="portafolios.length === 0" class="text-center py-12">
          <div class="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-10 h-10 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
          </div>
          <p class="text-slate-500 font-medium">Aún no hay tareas asignadas en esta clase.</p>
        </div>

        <div v-else class="space-y-4">
          <!-- Tarjeta de cada portafolio -->
          <div 
            v-for="portafolio in portafolios" 
            :key="portafolio.id"
            @click="verEntregas(portafolio.id)"
            class="group flex flex-col sm:flex-row sm:items-center justify-between p-5 bg-white border border-slate-200 rounded-2xl hover:border-blue-300 hover:shadow-lg transition-all duration-300 cursor-pointer"
          >
            <div class="flex items-start gap-4 mb-4 sm:mb-0">
              <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0 group-hover:scale-110 transition-transform">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div>
                <h3 class="text-lg font-bold text-slate-800 group-hover:text-blue-600 transition-colors">{{ portafolio.titulo }}</h3>
                <p class="text-sm text-slate-500 mt-1 line-clamp-2 max-w-xl">{{ portafolio.descripcion || 'Sin descripción' }}</p>
              </div>
            </div>

            <div class="flex flex-col sm:items-end gap-2 pl-16 sm:pl-0">
              <div class="inline-flex items-center gap-1.5 px-3 py-1 bg-red-50 text-red-700 text-xs font-bold rounded-lg border border-red-100">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Límite: {{ typeof portafolio.fechaLimite === 'string' ? new Date(portafolio.fechaLimite).toLocaleString() : formatearFecha(portafolio.fechaLimite) }}
              </div>
              
              <span v-if="rol === 'DOCENTE'" class="text-xs font-bold text-blue-500 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                Ver entregas de alumnos
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </span>
              <span v-else class="text-xs font-bold text-blue-500 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                Subir evidencia
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                </svg>
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Reutilizado -->
      <CreatePortafolioModal 
        :show="showCreatePortafolioModal"
        :preselectedClaseId="claseId"
        @close="showCreatePortafolioModal = false"
        @created="handlePortafolioCreated"
      />

    </div>
  </div>
</template>
