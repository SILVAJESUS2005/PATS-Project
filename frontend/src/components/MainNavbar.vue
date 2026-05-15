<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isAuth = ref(false)
const userName = ref('')

// Escuchamos los cambios de ruta para actualizar la barra de navegación dinámicamente
watch(() => route.path, () => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    isAuth.value = true
    userName.value = localStorage.getItem('user_name') || 'Usuario'
  } else {
    isAuth.value = false
    userName.value = ''
  }
}, { immediate: true })

const cerrarSesion = () => {
  localStorage.removeItem('auth_token')
  localStorage.removeItem('user_name')
  isAuth.value = false
  router.push('/login')
}
</script>

<template>
  <nav class="bg-white shadow-sm border-b border-gray-200">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        
        <!-- Logo / Marca -->
        <div class="flex items-center">
          <router-link to="/" class="flex-shrink-0 flex items-center gap-2">
            <!-- Icono provisional -->
            <div class="w-8 h-8 bg-blue-600 text-white rounded-md flex items-center justify-center font-bold">
              P
            </div>
            <span class="text-xl font-bold text-gray-900 tracking-tight">PATS</span>
          </router-link>
        </div>

        <!-- Menú de enlaces -->
        <div class="flex items-center space-x-4">
          
          <!-- Vista para usuarios NO autenticados -->
          <template v-if="!isAuth">
            <router-link 
              to="/login" 
              class="text-gray-600 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              Iniciar Sesión
            </router-link>
            <router-link 
              to="/registro" 
              class="bg-blue-600 text-white hover:bg-blue-700 px-4 py-2 rounded-md text-sm font-medium transition-colors shadow-sm"
            >
              Registrarse
            </router-link>
          </template>

          <!-- Vista para usuarios SÍ autenticados -->
          <template v-else>
            <span class="text-gray-700 text-sm hidden sm:block">
              Hola, <span class="font-semibold">{{ userName }}</span>
            </span>
            <button 
              @click="cerrarSesion"
              class="inline-flex items-center px-4 py-2 border border-red-200 text-sm font-medium rounded-md text-red-700 bg-red-50 hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors"
            >
              Cerrar Sesión
            </button>
          </template>

        </div>
      </div>
    </div>
  </nav>
</template>
