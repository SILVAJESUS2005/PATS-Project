<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ProfileModal from '../components/ProfileModal.vue'

const router = useRouter()
const userName = ref('')
const userRole = ref('')
const showProfileModal = ref(false)

// Al cargar la página, leemos la "sesión" (localStorage)
onMounted(() => {
  const storedName = localStorage.getItem('user_name')
  const storedRole = localStorage.getItem('user_role')
  
  if (storedName) {
    userName.value = storedName
  } else {
    userName.value = 'Usuario'
  }

  if (storedRole) {
    userRole.value = storedRole
  }

  // Validación ineludible de perfil para alumnos
  const matricula = localStorage.getItem('user_matricula')
  if (storedRole === 'ALUMNO' && !matricula) {
    showProfileModal.value = true
  }
})

const onProfileSaved = (data) => {
  localStorage.setItem('user_matricula', data.matricula)
  showProfileModal.value = false
}

// La función de cerrar sesión ahora vive en el MainNavbar global,
// pero mantenemos userName por si el Dashboard quiere mostrar un mensaje de bienvenida gigante en el centro.
</script>

<template>
  <div class="min-h-[calc(100vh-4rem)] bg-gray-50 pt-10">

    <!-- Contenido Principal -->
    <main class="max-w-7xl mx-auto py-10 px-4 sm:px-6 lg:px-8">
      <div class="bg-white overflow-hidden shadow rounded-lg border border-gray-100">
        <div class="px-4 py-5 sm:p-6 text-center">
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
            <svg class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h3 class="text-lg leading-6 font-medium text-gray-900">¡Acceso concedido!</h3>
          <div class="mt-2 max-w-xl text-sm text-gray-500 mx-auto">
            <p>Has superado exitosamente el Guardia de Navegación de Vue Router. Tu sesión (Token) está activa en el almacenamiento local.</p>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Ineludible de Perfil -->
    <ProfileModal :is-open="showProfileModal" @profile-updated="onProfileSaved" />
  </div>
</template>
