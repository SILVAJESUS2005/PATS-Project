<template>
  <div class="registro-container">
    <h2>Crear tu cuenta</h2>
    
    <form @submit.prevent="procesarRegistro">
      <div v-if="mensajeExito" class="alerta-exito">{{ mensajeExito }}</div>

      <div class="campo">
        <label>Nombre</label>
        <input type="text" v-model="formulario.nombre" />
        <span class="error" v-if="errores.nombre">{{ errores.nombre }}</span>
      </div>

      <div class="campo">
        <label>Apellidos</label>
        <input type="text" v-model="formulario.apellidos" />
        <span class="error" v-if="errores.apellidos">{{ errores.apellidos }}</span>
      </div>

      <div class="campo">
        <label>Correo electrónico</label>
        <input type="email" v-model="formulario.correo" />
        <span class="error" v-if="errores.correo">{{ errores.correo }}</span>
      </div>

      <div class="campo">
        <label>Contraseña</label>
        <input type="password" v-model="formulario.password" />
        <span class="error" v-if="errores.password">{{ errores.password }}</span>
      </div>

      <div class="campo">
        <label>Confirmar contraseña</label>
        <input type="password" v-model="formulario.confirmarPassword" />
        <span class="error" v-if="errores.confirmarPassword">{{ errores.confirmarPassword }}</span>
      </div>

      <button type="submit">Registrarse</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// Estado del formulario
const formulario = ref({
  nombre: '',
  apellidos: '',
  correo: '',
  password: '',
  confirmarPassword: ''
});

// Estado de errores y mensajes
const errores = ref({});
const mensajeExito = ref('');

// Función de Validación en el Cliente
const validarCliente = () => {
  errores.value = {}; // Limpiar errores
  let esValido = true;

  if (!formulario.value.nombre) { errores.value.nombre = 'El nombre es requerido'; esValido = false; }
  if (!formulario.value.apellidos) { errores.value.apellidos = 'Los apellidos son requeridos'; esValido = false; }
  
  // Validación de formato de email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!formulario.value.correo) { 
    errores.value.correo = 'El correo es requerido'; 
    esValido = false; 
  } else if (!emailRegex.test(formulario.value.correo)) {
    errores.value.correo = 'Ingresa un correo electrónico válido';
    esValido = false;
  }

  if (!formulario.value.password) { errores.value.password = 'La contraseña es requerida'; esValido = false; }
  
  if (formulario.value.password !== formulario.value.confirmarPassword) {
    errores.value.confirmarPassword = 'Las contraseñas no coinciden';
    esValido = false;
  }

  return esValido;
};

// Procesamiento y envío al Backend
const procesarRegistro = async () => {
  if (!validarCliente()) return; // Si falla en el cliente, no enviamos al servidor

  try {
    const respuesta = await fetch('http://localhost:8080/api/auth/registro', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: formulario.value.nombre,
        apellidos: formulario.value.apellidos,
        correo: formulario.value.correo,
        password: formulario.value.password
      })
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
      // Capturamos los errores dinámicos del servidor (Spring Boot)
      errores.value = data; 
    } else {
      mensajeExito.value = data.mensaje;
      // Limpiar formulario tras éxito
      formulario.value = { nombre: '', apellidos: '', correo: '', password: '', confirmarPassword: '' };
    }
  } catch (error) {
    console.error('Error de conexión:', error);
  }
};
</script>

<style scoped>
.campo { margin-bottom: 15px; }
.campo input { width: 100%; padding: 8px; margin-top: 5px; }
.error { color: red; font-size: 0.85em; display: block; margin-top: 5px; }
.alerta-exito { background-color: #d4edda; color: #155724; padding: 10px; margin-bottom: 15px; }
button { width: 100%; padding: 10px; background-color: black; color: white; cursor: pointer; }
</style>