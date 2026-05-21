<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AuthLayout from '../components/AuthLayout.vue'
import AuthInput from '../components/AuthInput.vue'

// TODO: Reemplaza con tu Client ID
const GOOGLE_CLIENT_ID = "294864417020-usltovtittatrqk6rl8d107tqgc6ne3h.apps.googleusercontent.com"

const router = useRouter()

// Estado del formulario
const loginForm = reactive({
  email: '',
  password: ''
})

const errorMessage = ref('')
const isLoading = ref(false)

const realizarLogin = async () => {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    // ---------------------------------------------------------
    // CONEXIÓN REAL AL BACKEND:
    // ---------------------------------------------------------
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo: loginForm.email, password: loginForm.password }),
      credentials: 'include' // VITAL: Permite que Spring Boot guarde su Cookie de Sesión
    })

    if (!response.ok) {
      // Si recibimos un 401 Unauthorized u otro error
      throw new Error('Credenciales incorrectas o el usuario no existe.')
    }
    
    const data = await response.json()
    
    // Guardamos un identificador local (en el futuro esto podría ser el JWT)
    localStorage.setItem('auth_token', 'sesion-activa-spring-boot')
    localStorage.setItem('user_name', data.nombre)
    
    router.push('/dashboard')
    // ---------------------------------------------------------

  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

const handleGoogleResponse = async (response) => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await fetch('http://localhost:8080/api/auth/google', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token: response.credential }),
      credentials: 'include'
    })

    if (!res.ok) throw new Error('Error al autenticar con Google')
    
    const data = await res.json()
    localStorage.setItem('auth_token', 'sesion-activa-spring-boot')
    localStorage.setItem('user_name', data.nombre)
    router.push('/dashboard')
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  if (window.google) {
    window.google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: handleGoogleResponse
    });
    window.google.accounts.id.renderButton(
      document.getElementById("google-buttonDiv"),
      { theme: "outline", size: "large", width: "100%" }
    );
  }
})
</script>

<template>
  <AuthLayout 
    title="Inicia Sesión en PATS" 
    altText="O" 
    altLinkText="crea una nueva cuenta" 
    altLinkTo="/registro"
  >
    <!-- Mensaje de error general -->
    <div v-if="errorMessage" class="mb-4 bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
      <div class="flex">
        <div class="ml-3">
          <p class="text-sm text-red-700">
            {{ errorMessage }}
          </p>
        </div>
      </div>
    </div>

    <form class="space-y-6" @submit.prevent="realizarLogin">
      
      <AuthInput 
        id="email" 
        label="Correo Electrónico" 
        type="email" 
        placeholder="ejemplo@correo.com" 
        v-model="loginForm.email" 
      />

      <AuthInput 
        id="password" 
        label="Contraseña" 
        type="password" 
        placeholder="••••••••" 
        v-model="loginForm.password" 
      />

      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <input id="remember-me" type="checkbox" class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded" />
          <label for="remember-me" class="ml-2 block text-sm text-gray-900">
            Recordarme
          </label>
        </div>

        <div class="text-sm">
          <a href="#" class="font-medium text-blue-600 hover:text-blue-500">
            ¿Olvidaste tu contraseña?
          </a>
        </div>
      </div>

      <div>
        <button 
          type="submit" 
          :disabled="isLoading"
          class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="isLoading">Cargando...</span>
          <span v-else>Entrar</span>
        </button>
      </div>

      <div class="mt-6">
        <div class="relative">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-300"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-2 bg-white text-gray-500">O continúa con</span>
          </div>
        </div>

        <div class="mt-6">
          <div id="google-buttonDiv" class="w-full flex justify-center"></div>
        </div>
      </div>
    </form>
  </AuthLayout>
</template>