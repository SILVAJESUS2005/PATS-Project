<script setup>
import { ref, onMounted } from 'vue'
import BasePremiumModal from './BasePremiumModal.vue'

const props = defineProps({
  show: Boolean,
  preselectedClaseId: {
    type: [String, Number],
    default: ''
  }
})

const emit = defineEmits(['close', 'created'])
const isLoading = ref(false)

const form = ref({
  titulo: '',
  descripcion: '',
  fechaLimite: '',
  claseId: props.preselectedClaseId
})

const clases = ref([])
const errorMessage = ref('')
const successMessage = ref('')

const fetchClases = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/clases', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    })
    if (response.ok) {
      clases.value = await response.json()
    }
  } catch (error) {
    console.error("Error al cargar las clases:", error)
  }
}

onMounted(() => {
  fetchClases()
})

const closeModal = () => {
  form.value.titulo = ''
  form.value.descripcion = ''
  form.value.fechaLimite = ''
  form.value.claseId = ''
  errorMessage.value = ''
  successMessage.value = ''
  emit('close')
}

const crearPortafolio = async () => {
  if (!form.value.titulo.trim() || !form.value.fechaLimite || !form.value.claseId) {
    errorMessage.value = "Por favor, completa todos los campos requeridos."
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  
  try {
    // Formatear la fecha a ISO si es necesario, o enviarla como venga del input datetime-local
    // asumiendo que Spring Boot acepta el formato de datetime-local (yyyy-MM-dd'T'HH:mm)
    let payload = {
        titulo: form.value.titulo,
        descripcion: form.value.descripcion,
        fechaLimite: form.value.fechaLimite, // Ojo: enviar en el formato correcto
        claseId: form.value.claseId
    }

    const response = await fetch('http://localhost:8080/api/portafolios', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      credentials: 'include'
    })

    const data = await response.json()

    if (!response.ok) {
        if (typeof data === 'object' && !data.error && !data.mensaje) {
            const firstError = Object.values(data)[0];
            throw new Error(firstError || 'Error al crear el portafolio');
        }
        throw new Error(data.error || 'Error al crear el portafolio')
    }

    successMessage.value = `¡Portafolio '${data.titulo}' creado y asignado con éxito!`

    emit('created', data)

    setTimeout(() => {
        closeModal()
    }, 2500)

  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <BasePremiumModal :show="show" @close="closeModal" maxWidthClass="max-w-md">
    
    <template #header>
      <h3 class="text-2xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-slate-800 to-slate-600 tracking-tight">Nuevo Portafolio</h3>
    </template>

    <template #content>
      <p class="text-slate-500 mb-6 text-sm font-medium leading-relaxed">
        Asigna una nueva tarea o evidencia a una de tus clases.
      </p>

      <form @submit.prevent="crearPortafolio" class="space-y-5">
        
        <!-- Selección de Clase -->
        <div class="group/input">
          <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Clase / Materia *</label>
          <select 
            v-model="form.claseId"
            class="block w-full px-4 py-3 border-2 border-slate-100 rounded-xl bg-slate-50 text-slate-800 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all font-semibold shadow-inner appearance-none cursor-pointer"
            required
            :disabled="isLoading || successMessage !== ''"
          >
            <option value="" disabled selected>-- Selecciona una clase --</option>
            <option v-for="clase in clases" :key="clase.id" :value="clase.id">
              {{ clase.nombre }} ({{ clase.codigoAcceso }})
            </option>
          </select>
        </div>

        <!-- Título -->
        <div class="group/input">
          <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Título de la Tarea *</label>
          <input 
            type="text" 
            v-model="form.titulo"
            class="block w-full px-4 py-3 border-2 border-slate-100 rounded-xl bg-slate-50 text-slate-800 placeholder-slate-300 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all font-semibold shadow-inner" 
            placeholder="Ej. Avance del Proyecto Final"
            required
            :disabled="isLoading || successMessage !== ''"
          >
        </div>

        <!-- Descripción -->
        <div class="group/input">
          <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Descripción (Opcional)</label>
          <textarea 
            v-model="form.descripcion"
            rows="2"
            class="block w-full px-4 py-3 border-2 border-slate-100 rounded-xl bg-slate-50 text-slate-800 placeholder-slate-300 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all shadow-inner resize-none text-sm" 
            placeholder="Instrucciones para los alumnos..."
            :disabled="isLoading || successMessage !== ''"
          ></textarea>
        </div>

        <!-- Fecha Límite -->
        <div class="group/input">
          <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Fecha y Hora Límite *</label>
          <input 
            type="datetime-local" 
            v-model="form.fechaLimite"
            class="block w-full px-4 py-3 border-2 border-slate-100 rounded-xl bg-slate-50 text-slate-800 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all font-semibold shadow-inner" 
            required
            :disabled="isLoading || successMessage !== ''"
          >
        </div>

        <!-- Alertas -->
        <Transition name="slide-fade">
          <div v-if="errorMessage" class="rounded-xl bg-red-50/80 p-3 border border-red-200/50 shadow-sm mt-4">
            <p class="text-sm font-semibold text-red-700 text-center">{{ errorMessage }}</p>
          </div>
        </Transition>
        <Transition name="slide-fade">
          <div v-if="successMessage" class="rounded-xl bg-green-50/80 p-3 border border-green-200/50 shadow-sm mt-4">
            <p class="text-sm font-semibold text-green-700 text-center">{{ successMessage }}</p>
          </div>
        </Transition>

        <!-- Botones -->
        <div class="mt-6 pt-2 flex justify-end space-x-3">
          <button 
            type="button" 
            @click="closeModal"
            class="px-5 py-2 rounded-xl text-slate-500 font-bold hover:bg-slate-100 transition-all"
          >
            {{ successMessage ? 'Cerrar' : 'Cancelar' }}
          </button>
          <button 
            v-if="!successMessage"
            type="submit" 
            class="relative px-6 py-2 bg-slate-900 rounded-xl text-white font-bold hover:bg-black hover:-translate-y-0.5 focus:ring-4 focus:ring-slate-900/30 transition-all disabled:opacity-50 flex items-center"
            :disabled="isLoading"
          >
            <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ isLoading ? 'Asignando...' : 'Asignar Tarea' }}
          </button>
        </div>
      </form>
    </template>

  </BasePremiumModal>
</template>
