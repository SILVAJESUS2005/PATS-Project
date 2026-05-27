<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import JoinClassModal from '../components/JoinClassModal.vue'
import CreateClassModal from '../components/CreateClassModal.vue'

const router = useRouter()

const userName = ref(localStorage.getItem('user_name') || 'Usuario')
const rol = ref(localStorage.getItem('user_role') || 'ALUMNO')

// Paleta de colores para las tarjetas
const colorPalette = [
  'from-pink-500 to-rose-500',
  'from-blue-500 to-cyan-500',
  'from-violet-500 to-purple-500',
  'from-emerald-500 to-teal-500',
  'from-amber-500 to-orange-500',
  'from-indigo-500 to-blue-500'
]
const defaultIcon = 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253'

const clases = ref([])

const isLoaded = ref(false)
const showAddClassModal = ref(false)
const showCreateClassModal = ref(false)

const fetchClases = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/clases', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    })
    if (response.ok) {
      const data = await response.json()
      clases.value = data.map((c, index) => ({
        id: c.id,
        name: c.nombre,
        code: c.codigoAcceso,
        color: colorPalette[index % colorPalette.length],
        icon: defaultIcon
      }))
    } else {
      console.error("Error obteniendo las clases")
    }
  } catch (error) {
    console.error("Error de conexión al backend:", error)
  }
}

onMounted(() => {
  fetchClases()
  // Pequeño retraso para la animación de entrada
  setTimeout(() => {
    isLoaded.value = true
  }, 100)

  // Escuchar actualizaciones de perfil de usuario
  window.addEventListener('profile-updated', () => {
    userName.value = localStorage.getItem('user_name') || 'Alumno'
  })
})

const navigateToPortafolios = (clase) => {
  if (clase && clase.id) {
    if (rol.value === 'DOCENTE') {
      router.push(`/clase/${clase.id}`)
    } else {
      router.push(`/portafolios?claseId=${clase.id}&claseNombre=${encodeURIComponent(clase.name)}`)
    }
  } else {
    router.push('/portafolios')
  }
}

const openAddClassModal = () => {
  if (rol.value === 'DOCENTE') {
    showCreateClassModal.value = true
  } else {
    showAddClassModal.value = true
  }
}

const handleClassJoinedOrCreated = (data) => {
  console.log("¡Operación exitosa!", data)
  fetchClases()
}
</script>

<template>
  <!-- Contenedor Principal con fondo vibrante y moderno -->
  <div class="relative min-h-[calc(100vh-4rem)] bg-slate-50 overflow-hidden font-sans">
    
    <!-- Elementos decorativos del fondo (Blobs) -->
    <div class="absolute top-[-10%] left-[-10%] w-96 h-96 bg-purple-300 rounded-full mix-blend-multiply filter blur-3xl opacity-40 animate-blob"></div>
    <div class="absolute top-[20%] right-[-10%] w-96 h-96 bg-blue-300 rounded-full mix-blend-multiply filter blur-3xl opacity-40 animate-blob animation-delay-2000"></div>
    <div class="absolute bottom-[-20%] left-[20%] w-96 h-96 bg-pink-300 rounded-full mix-blend-multiply filter blur-3xl opacity-40 animate-blob animation-delay-4000"></div>

    <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 z-10">
      
      <!-- Encabezado Glassmórfico -->
      <div 
        class="bg-white/60 backdrop-blur-xl border border-white/80 shadow-lg rounded-3xl p-8 mb-10 flex flex-col md:flex-row items-center justify-between transition-all duration-700 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : '-translate-y-10 opacity-0'"
      >
        <div class="flex items-center gap-6 mb-6 md:mb-0">
          <!-- Avatar del usuario -->
          <div class="relative">
            <div class="w-16 h-16 rounded-full bg-gradient-to-tr from-blue-600 to-purple-600 p-1 shadow-lg">
              <div class="w-full h-full bg-white rounded-full flex items-center justify-center">
                <span class="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-tr from-blue-600 to-purple-600">
                  {{ userName.charAt(0).toUpperCase() }}
                </span>
              </div>
            </div>
            <div class="absolute bottom-0 right-0 w-4 h-4 bg-green-500 border-2 border-white rounded-full"></div>
          </div>
          <div>
            <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">
              Hola, <span class="bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600">{{ userName }}</span>
            </h1>
            <p class="mt-1 text-slate-600 font-medium">Bienvenido a tu panel de control PATS.</p>
          </div>
        </div>
        
        <button 
          @click="navigateToPortafolios"
          class="group relative inline-flex items-center justify-center px-8 py-3 font-bold text-white transition-all duration-300 bg-slate-900 rounded-xl hover:bg-slate-800 hover:shadow-xl hover:shadow-slate-900/20 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2 overflow-hidden"
        >
          <span class="absolute inset-0 w-full h-full -mt-1 rounded-lg opacity-30 bg-gradient-to-b from-transparent via-transparent to-black"></span>
          <svg class="w-5 h-5 mr-2 group-hover:rotate-12 transition-transform duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          <span class="relative">Ver Portafolios</span>
        </button>
      </div>

      <!-- Título de Sección -->
      <div 
        class="flex items-center justify-between mb-8 transition-all duration-700 delay-100 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'"
      >
        <h2 class="text-2xl font-bold text-slate-800 flex items-center gap-2">
          <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
          Mis Materias Activas
        </h2>
      </div>

      <!-- Grid de Clases (Tarjetas Modernas) -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div 
          v-for="(clase, index) in clases" 
          :key="clase.id"
          @click="navigateToPortafolios(clase)"
          class="group relative bg-white/70 backdrop-blur-lg border border-white/60 rounded-3xl p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] hover:shadow-[0_8px_30px_rgb(0,0,0,0.12)] hover:-translate-y-2 transition-all duration-500 cursor-pointer overflow-hidden"
          :style="{ transitionDelay: `${index * 100 + 200}ms` }"
          :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-12 opacity-0'"
        >
          <!-- Efecto de gradiente sutil al fondo de la tarjeta en hover -->
          <div 
            class="absolute -right-20 -top-20 w-40 h-40 rounded-full blur-3xl opacity-0 group-hover:opacity-40 transition-opacity duration-500 bg-gradient-to-br"
            :class="clase.color"
          ></div>

          <div class="relative z-10 flex flex-col h-full justify-between">
            <div>
              <!-- Icono de la clase -->
              <div 
                class="w-14 h-14 mx-auto rounded-2xl flex items-center justify-center mb-6 shadow-md bg-gradient-to-br text-white transform group-hover:scale-110 transition-transform duration-500"
                :class="clase.color"
              >
                <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="clase.icon" />
                </svg>
              </div>
              
              <h3 class="text-xl font-bold text-center text-slate-800 leading-tight group-hover:text-blue-600 transition-colors duration-300">
                {{ clase.name }}
              </h3>
            </div>

            <!-- Botón del código interactivo -->
            <div class="mt-8 pt-6 border-t border-slate-100 flex items-center justify-between">
              <div class="flex items-center space-x-2">
                <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">CÓDIGO</span>
                <span class="inline-flex items-center px-3 py-1 rounded-lg text-sm font-bold bg-slate-100 text-slate-700 border border-slate-200 group-hover:border-blue-200 group-hover:bg-blue-50 transition-colors duration-300">
                  {{ clase.code }}
                </span>
              </div>
              <div class="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center group-hover:bg-blue-600 group-hover:text-white text-slate-400 transition-all duration-300 shadow-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer / Botón Agregar -->
      <div 
        class="mt-16 flex justify-center pb-12 transition-all duration-700 transform"
        :class="isLoaded ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'"
        style="transition-delay: 800ms;"
      >
        <button 
          @click="openAddClassModal"
          class="relative group inline-flex items-center justify-center"
        >
          <!-- Efecto de resplandor trasero (Glow) -->
          <div class="absolute -inset-1 bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl blur opacity-40 group-hover:opacity-75 transition duration-500 group-hover:duration-200"></div>
          
          <div class="relative px-8 py-4 bg-white ring-1 ring-slate-900/5 rounded-2xl leading-none flex items-center shadow-xl group-hover:shadow-2xl transition-all duration-300 group-hover:-translate-y-1">
            <div class="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center mr-4 group-hover:bg-blue-600 group-hover:text-white text-blue-600 transition-colors duration-300">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
            </div>
            <div class="flex flex-col text-left">
              <span class="text-slate-800 font-bold text-lg">
                {{ rol === 'DOCENTE' ? 'Crear nueva clase' : 'Agregar nueva clase' }}
              </span>
              <span class="text-slate-500 text-sm font-medium">
                {{ rol === 'DOCENTE' ? 'Genera un código para tus alumnos' : 'Ingresa el código proporcionado por tu profesor' }}
              </span>
            </div>
          </div>
        </button>
      </div>

      <!-- Integración de los Modales -->
      <JoinClassModal 
        :show="showAddClassModal"
        @close="showAddClassModal = false"
        @joined="handleClassJoinedOrCreated"
      />

      <CreateClassModal 
        :show="showCreateClassModal"
        @close="showCreateClassModal = false"
        @created="handleClassJoinedOrCreated"
      />

    </div>
  </div>
</template>

<style scoped>
/* Animación personalizada para los blobs del fondo */
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}
</style>
