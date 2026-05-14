<template>
  <div class="login-container">
    <h1>PATS</h1>
    <p>Ingresa con tu cuenta para continuar</p>
    
    <form @submit.prevent="handleLogin">
      <div class="campo">
        <label>Correo electrónico</label>
        <input type="email" v-model="correo" required>
      </div>

      <div class="campo">
        <label>Contraseña</label>
        <input type="password" v-model="password" required>
      </div>

      <div class="opciones">
        <label><input type="checkbox"> Recordarme</label>
        <a href="#">¿Olvidaste contraseña?</a>
      </div>

      <button type="submit" class="btn-entrar">Entrar</button>
    </form>

    <hr>
    <button class="btn-google" @click="loginConGoogle">
      Continuar con Google
    </button>

    <p class="footer">
      ¿No tienes una cuenta? 
      <router-link to="/registro">Regístrate aquí</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const correo = ref('');
const password = ref('');

const handleLogin = () => {
  console.log("Intentando entrar con:", correo.value);
  // Aquí llamarás a tu API de Spring Boot
};

const loginConGoogle = () => {
  // Aquí redirigirás a la autenticación de Google
  alert("Redirigiendo a Google Auth...");
};
</script>

<style scoped>
.login-container { max-width: 400px; margin: 50px auto; text-align: center; font-family: sans-serif; }
.campo { text-align: left; margin-bottom: 15px; }
.campo input { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px; }
.btn-entrar { width: 100%; padding: 12px; background: black; color: white; border: none; border-radius: 5px; cursor: pointer; }
.btn-google { width: 100%; padding: 12px; background: white; border: 1px solid #ccc; margin-top: 10px; cursor: pointer; }
.opciones { display: flex; justify-content: space-between; font-size: 0.8em; margin-bottom: 15px; }
</style>