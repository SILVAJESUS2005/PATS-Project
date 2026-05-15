<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthLayout from '../components/AuthLayout.vue'
import AuthInput from '../components/AuthInput.vue'

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
    // TODO: CUANDO EL BACKEND ESTÉ LISTO, REEMPLAZA ESTE BLOQUE:
    // ---------------------------------------------------------
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (loginForm.email === 'admin@pats.com' && loginForm.password === '1234') {
      localStorage.setItem('auth_token', 'mock-token-xyz-789')
      localStorage.setItem('user_name', 'Administrador')
      router.push('/dashboard')
    } else {
      throw new Error('Credenciales incorrectas. (Prueba con admin@pats.com y 1234)')
    }
    // Fin del bloque a borrar
    // ---------------------------------------------------------
    // Y PON ESTO EN SU LUGAR:
    // const response = await fetch('http://localhost:8080/api/login', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ email: loginForm.email, password: loginForm.password })
    // })
    // if (!response.ok) throw new Error('Credenciales incorrectas')
    // const data = await response.json()
    // localStorage.setItem('auth_token', data.token)
    // localStorage.setItem('user_name', data.nombre)
    // router.push('/dashboard')
    // ---------------------------------------------------------

  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}
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
    </form>
  </AuthLayout>
</template>