<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  altText: {
    type: String,
    required: false
  },
  altLinkText: {
    type: String,
    required: false
  },
  altLinkTo: {
    type: String,
    required: false
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        {{ title }}
      </h2>
      <p v-if="altText" class="mt-2 text-center text-sm text-gray-600">
        {{ altText }}
        <router-link v-if="altLinkTo" :to="altLinkTo" class="font-medium text-blue-600 hover:text-blue-500 transition-colors">
          {{ altLinkText }}
        </router-link>
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 border border-gray-100">
        <!-- El contenido dinámico (formulario) se inyectará aquí -->
        <slot></slot>
      </div>
    </div>
  </div>
</template>
