<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import BasePremiumModal from './BasePremiumModal.vue'
import AuthInput from './AuthInput.vue'

const router = useRouter()
const route = useRoute()

const isAuth = ref(false)
const userName = ref('')
const userRole = ref('')

// Escuchamos los cambios de ruta para actualizar la barra de navegación dinámicamente
watch(() => route.path, () => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    isAuth.value = true
    userName.value = localStorage.getItem('user_name') || 'Usuario'
    userRole.value = localStorage.getItem('user_role') || ''
  } else {
    isAuth.value = false
    userName.value = ''
    userRole.value = ''
  }
}, { immediate: true })

const cerrarSesion = async () => {
  try {
    // Le avisamos al backend que destruya la sesión (HttpSession)
    await fetch('http://localhost:8080/api/auth/logout', {
      method: 'GET',
      credentials: 'include' // Enviamos la cookie para que sepa quién está cerrando sesión
    })
  } catch (error) {
    console.error("Error al cerrar sesión en el servidor", error)
  }

  // Limpiamos la sesión del frontend
  localStorage.removeItem('auth_token')
  localStorage.removeItem('user_name')
  localStorage.removeItem('user_role')
  localStorage.removeItem('user_matricula')
  isAuth.value = false
  router.push('/login')
}

// Variables reactivas para el modal de perfil
const showProfileModal = ref(false)
const perfilNombre = ref('')
const perfilMatricula = ref('')
const isSaving = ref(false)
const profileErrorMessage = ref('')
const profileSuccessMessage = ref('')

const abrirModalPerfil = () => {
  perfilNombre.value = localStorage.getItem('user_name') || ''
  perfilMatricula.value = localStorage.getItem('user_matricula') || ''
  profileErrorMessage.value = ''
  profileSuccessMessage.value = ''
  showProfileModal.value = true
}

const guardarPerfil = async () => {
  if (!perfilNombre.value.trim() || (userRole.value === 'ALUMNO' && !perfilMatricula.value.trim())) {
    profileErrorMessage.value = 'Todos los campos son requeridos.'
    return
  }

  isSaving.value = true
  profileErrorMessage.value = ''
  profileSuccessMessage.value = ''

  try {
    const response = await fetch('http://localhost:8080/api/usuarios/perfil', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        nombre: perfilNombre.value,
        matricula: perfilMatricula.value
      }),
      credentials: 'include'
    })

    const data = await response.json()

    if (!response.ok) {
      if (typeof data === 'object') {
        const errorMsg = Object.values(data).join(', ')
        throw new Error(errorMsg || 'Error al actualizar el perfil')
      }
      throw new Error(data.message || 'Error al actualizar el perfil')
    }

    // Guardar en localStorage
    localStorage.setItem('user_name', data.nombre)
    localStorage.setItem('user_matricula', data.matricula)

    // Actualizar nombre reactivo del Navbar
    userName.value = data.nombre

    // Disparar evento personalizado para actualizar otras pantallas (por ejemplo, Dashboard)
    window.dispatchEvent(new Event('profile-updated'))

    profileSuccessMessage.value = 'Perfil actualizado con éxito.'

    setTimeout(() => {
      showProfileModal.value = false
    }, 1500)

  } catch (error) {
    profileErrorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
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
            
            <!-- ========================================================================= -->
            <!-- INTEGRANTE 3: Tarea 1 - Edición de perfil -->
            <!-- Agrega aquí el modal o vista de la esquina superior derecha para editar nombre y matrícula -->
            <!-- ========================================================================= -->
            
            <span class="text-gray-700 text-sm hidden sm:block">
              Hola, <span class="font-semibold text-slate-800">{{ userName }}</span>
            </span>
            
            <button 
              @click="abrirModalPerfil"
              class="inline-flex items-center px-3.5 py-1.5 border border-slate-200 text-sm font-semibold rounded-lg text-slate-700 bg-white hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 cursor-pointer shadow-sm"
            >
              Mi Perfil
            </button>

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

    <!-- Modal de Edición de Perfil Premium -->
    <BasePremiumModal :show="showProfileModal" @close="showProfileModal = false" maxWidthClass="max-w-md">
      <template #header>
        <h3 class="text-2xl font-extrabold text-slate-900 tracking-tight">Editar Perfil</h3>
      </template>

      <template #content>
        <p class="text-slate-500 mb-6 text-sm font-medium leading-relaxed">
          Mantén tus datos actualizados para que tus profesores puedan identificarte correctamente.
        </p>

        <form @submit.prevent="guardarPerfil" class="space-y-5">
          <!-- Campo Nombre -->
          <AuthInput
            id="perfil-nombre"
            label="Nombre Completo"
            type="text"
            placeholder="Juan Pérez"
            v-model="perfilNombre"
            required
          />

          <!-- Campo Matrícula -->
          <AuthInput
            v-if="userRole === 'ALUMNO'"
            id="perfil-matricula"
            label="Matrícula"
            type="text"
            placeholder="Ej. 20261001"
            v-model="perfilMatricula"
            required
          />

          <!-- Mensaje de Error -->
          <Transition name="slide-fade">
            <div v-if="profileErrorMessage" class="rounded-xl bg-red-50/80 backdrop-blur-md p-4 border border-red-200/50 shadow-sm">
              <p class="text-sm font-semibold text-red-700">{{ profileErrorMessage }}</p>
            </div>
          </Transition>

          <!-- Mensaje de Éxito -->
          <Transition name="slide-fade">
            <div v-if="profileSuccessMessage" class="rounded-xl bg-green-50/80 backdrop-blur-md p-4 border border-green-200/50 shadow-sm">
              <p class="text-sm font-semibold text-green-700">{{ profileSuccessMessage }}</p>
            </div>
          </Transition>

          <!-- Botones del Modal -->
          <div class="mt-8 pt-2 flex justify-end space-x-3">
            <button 
              type="button" 
              @click="showProfileModal = false"
              class="px-5 py-2.5 rounded-xl text-slate-500 font-bold hover:bg-slate-100 hover:text-slate-800 focus:outline-none transition-all duration-200"
              :disabled="isSaving"
            >
              Cancelar
            </button>
            <button 
              type="submit" 
              class="relative px-6 py-2.5 bg-blue-600 rounded-xl text-white font-bold hover:bg-blue-700 hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-blue-600/30 transition-all duration-300 disabled:opacity-50"
              :disabled="isSaving"
            >
              <span class="flex items-center justify-center">
                <svg v-if="isSaving" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ isSaving ? 'Guardando...' : 'Guardar Cambios' }}
              </span>
            </button>
          </div>
        </form>
      </template>
    </BasePremiumModal>
  </nav>
</template>

