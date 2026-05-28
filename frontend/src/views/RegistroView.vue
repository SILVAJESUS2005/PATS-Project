<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthLayout from '../components/AuthLayout.vue'
import AuthInput from '../components/AuthInput.vue'

const router = useRouter()

// Estado del formulario
const formulario = ref({
  nombre: '',
  apellidos: '',
  correo: '',
  password: '',
  confirmarPassword: '',
  rol: 'ALUMNO'
})

// Estado de errores y mensajes
const errores = ref({})
const mensajeExito = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

// Función de Validación en el Cliente
const validarCliente = () => {
  errores.value = {}
  let esValido = true

  if (!formulario.value.nombre) { errores.value.nombre = 'El nombre es requerido'; esValido = false }
  if (!formulario.value.apellidos) { errores.value.apellidos = 'Los apellidos son requeridos'; esValido = false }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formulario.value.correo) { 
    errores.value.correo = 'El correo es requerido'
    esValido = false
  } else if (!emailRegex.test(formulario.value.correo)) {
    errores.value.correo = 'Ingresa un correo electrónico válido'
    esValido = false
  }

  if (!formulario.value.password) { errores.value.password = 'La contraseña es requerida'; esValido = false }
  
  if (formulario.value.password !== formulario.value.confirmarPassword) {
    errores.value.confirmarPassword = 'Las contraseñas no coinciden'
    esValido = false
  }

  return esValido
}

// Procesamiento y envío (Mock)
const procesarRegistro = async () => {
  if (!validarCliente()) return
  
  isLoading.value = true
  errorMessage.value = ''
  mensajeExito.value = ''

  try {
    // ---------------------------------------------------------
    // CONEXIÓN REAL AL BACKEND:
    // ---------------------------------------------------------
    const respuesta = await fetch('http://localhost:8080/api/auth/registro', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: formulario.value.nombre,
        apellidos: formulario.value.apellidos,
        correo: formulario.value.correo,
        password: formulario.value.password,
        rol: formulario.value.rol
      })
    })

    const data = await respuesta.json()

    if (!respuesta.ok) {
      // Spring Boot enviará los errores por campo en formato JSON si falla la validación
      errores.value = data 
      throw new Error('Por favor, corrige los errores en el formulario.')
    }

    mensajeExito.value = data.mensaje || '¡Cuenta creada exitosamente! Redirigiendo al login...'
    
    setTimeout(() => {
      formulario.value = { nombre: '', apellidos: '', correo: '', password: '', confirmarPassword: '', rol: 'ALUMNO' }
      router.push('/login')
    }, 2000)
    // ---------------------------------------------------------

  } catch (error) {
    if (!errores.value.correo) { // Si el error no es de un campo específico
      errorMessage.value = error.message
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <AuthLayout 
    title="Crea tu cuenta" 
    altText="¿Ya tienes cuenta?" 
    altLinkText="Inicia sesión aquí" 
    altLinkTo="/login"
  >
    
    <!-- Alertas -->
    <div v-if="mensajeExito" class="mb-4 bg-green-50 border-l-4 border-green-500 p-4 rounded-md">
      <p class="text-sm text-green-700">{{ mensajeExito }}</p>
    </div>
    
    <div v-if="errorMessage" class="mb-4 bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
      <p class="text-sm text-red-700">{{ errorMessage }}</p>
    </div>

    <form class="space-y-4" @submit.prevent="procesarRegistro">
      
      <!-- Selector de Rol (Pills Premium) -->
      <div class="mb-4">
        <label class="block text-sm font-bold text-slate-700 mb-2">Quiero registrarme como:</label>
        <div class="grid grid-cols-2 gap-4">
          <button 
            type="button"
            @click="formulario.rol = 'ALUMNO'"
            class="flex flex-col items-center justify-center p-4 border rounded-2xl transition-all duration-300 shadow-sm"
            :class="formulario.rol === 'ALUMNO' ? 'border-blue-600 bg-blue-50/50 text-blue-600 ring-2 ring-blue-600/20' : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'"
          >
            <span class="text-2xl mb-1">🎓</span>
            <span class="font-extrabold text-xs sm:text-sm">Alumno</span>
          </button>
          
          <button 
            type="button"
            @click="formulario.rol = 'DOCENTE'"
            class="flex flex-col items-center justify-center p-4 border rounded-2xl transition-all duration-300 shadow-sm"
            :class="formulario.rol === 'DOCENTE' ? 'border-blue-600 bg-blue-50/50 text-blue-600 ring-2 ring-blue-600/20' : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'"
          >
            <span class="text-2xl mb-1">👨‍🏫</span>
            <span class="font-extrabold text-xs sm:text-sm">Maestro / Docente</span>
          </button>
        </div>
      </div>
      
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <AuthInput 
          id="nombre" 
          label="Nombre" 
          v-model="formulario.nombre" 
          :errorMessage="errores.nombre"
        />
        <AuthInput 
          id="apellidos" 
          label="Apellidos" 
          v-model="formulario.apellidos" 
          :errorMessage="errores.apellidos"
        />
      </div>

      <AuthInput 
        id="correo" 
        label="Correo Electrónico" 
        type="email" 
        placeholder="ejemplo@correo.com" 
        v-model="formulario.correo" 
        :errorMessage="errores.correo"
      />

      <AuthInput 
        id="password" 
        label="Contraseña" 
        type="password" 
        v-model="formulario.password" 
        :errorMessage="errores.password"
      />

      <AuthInput 
        id="confirmarPassword" 
        label="Confirmar Contraseña" 
        type="password" 
        v-model="formulario.confirmarPassword" 
        :errorMessage="errores.confirmarPassword"
      />

      <div class="pt-2">
        <button 
          type="submit" 
          :disabled="isLoading || mensajeExito !== ''"
          class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="isLoading">Procesando...</span>
          <span v-else>Registrarse</span>
        </button>
      </div>
    </form>
  </AuthLayout>
</template>