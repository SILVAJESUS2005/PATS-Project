<script setup>
import { RouterView } from 'vue-router'
import MainNavbar from './components/MainNavbar.vue'
</script>

<template>
  <!-- Aquí inyectamos nuestra nueva barra de navegación global -->
  <MainNavbar />

  <!-- RouterView es donde se dibujarán el Login, Registro o Dashboard -->
  <main>
    <RouterView />
  </main>
</template>

<style>
/* 
  Hemos borrado el CSS antiguo y feo que tenías aquí.
  Ahora todo el proyecto respira y se maneja con Tailwind CSS 
  desde los componentes directamente.
*/
body {
  margin: 0;
  padding: 0;
  background-color: #f9fafb; /* gris muy claro de Tailwind (gray-50) */
}
</style>