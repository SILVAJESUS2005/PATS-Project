<script setup>
import { ref } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  },
  maxWidthClass: {
    type: String,
    default: 'max-w-md' // e.g., 'max-w-sm', 'max-w-lg', etc.
  }
})

const emit = defineEmits(['close'])

// Lógica para el resplandor que sigue al ratón
const boxRef = ref(null)
const mouseX = ref(0)
const mouseY = ref(0)
const isHovering = ref(false)

const handleMouseMove = (e) => {
  if (!boxRef.value) return
  const rect = boxRef.value.getBoundingClientRect()
  mouseX.value = e.clientX - rect.left
  mouseY.value = e.clientY - rect.top
}

const handleMouseEnter = () => {
  isHovering.value = true
}

const handleMouseLeave = () => {
  isHovering.value = false
}

const handleOverlayClick = (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    emit('close')
  }
}
</script>

<template>
  <Transition name="mac-modal">
    <!-- El div principal controla el overlay y el centrado -->
    <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-0 perspective-1000">
      
      <!-- Fondo oscuro desenfocado -->
      <div 
        class="modal-overlay absolute inset-0 bg-slate-900/40 backdrop-blur-sm"
        @click="handleOverlayClick"
      ></div>

      <!-- Wrapper para el Modal (Efecto Resplandor al Ratón) -->
      <!-- P-[2px] define el grosor del borde -->
      <div 
        ref="boxRef"
        class="modal-box relative w-full mx-auto z-10 p-[2px] rounded-[2rem] shadow-[0_20px_50px_rgba(0,0,0,0.5)] bg-slate-200/50 overflow-hidden"
        :class="maxWidthClass"
        @mousemove="handleMouseMove"
        @mouseenter="handleMouseEnter"
        @mouseleave="handleMouseLeave"
      >
        <!-- Borde base estático y tenue (por si el mouse no está encima) -->
        <div class="absolute inset-0 bg-gradient-to-br from-slate-200 to-slate-300 opacity-50 z-0"></div>

        <!-- Borde Resplandeciente (Glow) que sigue al ratón -->
        <div 
          class="absolute inset-0 z-0 transition-opacity duration-500 ease-out pointer-events-none"
          :style="{
            opacity: isHovering ? 1 : 0,
            background: `radial-gradient(250px circle at ${mouseX}px ${mouseY}px, rgba(59, 130, 246, 1), rgba(236, 72, 153, 1), transparent 100%)`
          }"
        ></div>

        <!-- Caja Interna del Modal (Fondo Sólido Blanco) -->
        <div class="relative bg-white/95 backdrop-blur-3xl rounded-[calc(2rem-2px)] p-8 h-full flex flex-col justify-between z-10">
          
          <div class="flex items-center justify-between mb-6">
            <slot name="header"></slot>
            
            <!-- Botón estilo macOS (Círculo sutil) -->
            <button 
              @click="$emit('close')"
              class="w-8 h-8 flex items-center justify-center rounded-full bg-slate-100 text-slate-500 hover:bg-slate-200 hover:text-slate-800 transition-all duration-200 transform hover:scale-110 active:scale-95"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <slot name="content"></slot>

        </div>
      </div>
    </div>
  </Transition>
</template>

<style>
/* Animaciones globales compartidas para los modales premium */

/* 1. Transición general del contenedor (Opacidad) */
.mac-modal-enter-active,
.mac-modal-leave-active {
  transition: opacity 0.4s ease;
}
.mac-modal-enter-from,
.mac-modal-leave-to {
  opacity: 0;
}

/* 2. Transición explícita sobre el hijo (Efecto Resorte) */
.mac-modal-enter-active .modal-box {
  transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.mac-modal-leave-active .modal-box {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.mac-modal-enter-from .modal-box {
  transform: scale(0.9) translateY(30px);
}
.mac-modal-leave-to .modal-box {
  transform: scale(0.95) translateY(10px);
}

/* Soporte de perspectiva */
.perspective-1000 {
  perspective: 1000px;
}

/* Animación de slide para alertas de error que puedan ir dentro de un slot */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}
</style>
