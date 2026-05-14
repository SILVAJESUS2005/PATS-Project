<template>
  <header>
    <nav>
      <RouterLink to="/">Inicio</RouterLink> | 
      <RouterLink to="/login">Iniciar Sesión</RouterLink> | 
      <RouterLink to="/registro">Registro</RouterLink>
    </nav>
  </header>

  <main>
    <RouterView />
  </main>
</template>

<style>
nav { padding: 20px; border-bottom: 1px solid #ccc; margin-bottom: 20px; }
main { padding: 20px; }
</style>