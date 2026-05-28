<script setup>
import { ref, reactive } from 'vue'
import BasePremiumModal from './BasePremiumModal.vue'

const props = defineProps({
  show: Boolean,
  portafolioId: {
    type: Number,
    required: true
  },
  portafolioName: {
    type: String,
    default: 'Portafolio'
  }
})

const emit = defineEmits(['close', 'uploaded'])

const form = reactive({
  introduccion: '',
  conclusiones: '',
  actividadesClase: [],
  tareasCasa: [],
  examenProyecto: []
})

const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleFileSelect = (event, category) => {
  const files = Array.from(event.target.files)
  if (files.length > 0) {
    form[category] = [...form[category], ...files]
  }
}

const removeFile = (category, index) => {
  form[category].splice(index, 1)
}

const closeModal = () => {
  form.introduccion = ''
  form.conclusiones = ''
  form.actividadesClase = []
  form.tareasCasa = []
  form.examenProyecto = []
  errorMessage.value = ''
  successMessage.value = ''
  emit('close')
}

const submitEvidencia = async () => {
  if (form.actividadesClase.length === 0 && form.tareasCasa.length === 0 && form.examenProyecto.length === 0) {
    errorMessage.value = "Debes subir al menos una imagen de evidencia en alguna categoría."
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  const formData = new FormData()
  formData.append('portafolioId', props.portafolioId)
  formData.append('introduccion', form.introduccion)
  formData.append('conclusiones', form.conclusiones)

  form.actividadesClase.forEach(file => formData.append('actividadesClase', file))
  form.tareasCasa.forEach(file => formData.append('tareasCasa', file))
  form.examenProyecto.forEach(file => formData.append('examenProyecto', file))

  try {
    const response = await fetch('http://localhost:8080/api/entregas/subir', {
      method: 'POST',
      body: formData,
      credentials: 'include'
    })

    const data = await response.text()

    if (!response.ok) {
      throw new Error(data || 'Ocurrió un error al subir el archivo')
    }

    successMessage.value = '¡Evidencia multicategoría enviada con éxito!'
    
    setTimeout(() => {
      emit('uploaded')
      closeModal()
    }, 1500)
    
  } catch (error) {
    errorMessage.value = error.message || 'Fallo de conexión con el servidor.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <BasePremiumModal :show="show" @close="closeModal" maxWidthClass="max-w-4xl">
    
    <template #header>
      <h3 class="text-xl font-bold text-slate-800 tracking-tight border-b border-slate-100 pb-2 w-full pr-4">
        Subir Evidencia Estructurada - <span class="text-blue-600 font-extrabold">{{ portafolioName }}</span>
      </h3>
    </template>

    <template #content>
      <form @submit.prevent="submitEvidencia" class="space-y-6 mt-4 max-h-[70vh] overflow-y-auto px-2">
        
        <!-- INTRODUCCIÓN -->
        <div>
          <label class="block text-sm font-bold text-slate-700 mb-2">Introducción</label>
          <textarea 
            v-model="form.introduccion" 
            rows="3" 
            class="block w-full px-4 py-3 border-2 border-slate-100 rounded-xl bg-slate-50 text-slate-800 focus:ring-4 focus:ring-blue-500/10 text-sm resize-y" 
            placeholder="Escribe la introducción de tu evidencia..."
            required
          ></textarea>
        </div>

        <!-- CATEGORÍAS DE IMÁGENES -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <!-- Actividades Clase -->
          <div class="border border-slate-200 bg-slate-50 rounded-xl p-4">
            <h4 class="font-bold text-sm text-slate-700 mb-2">Actividades de Clase</h4>
            <label class="cursor-pointer block text-center py-4 border-2 border-dashed border-slate-300 rounded-lg hover:bg-slate-100 transition">
              <span class="text-xs text-blue-600 font-bold">+ Subir Imágenes</span>
              <input type="file" multiple accept="image/*" class="hidden" @change="e => handleFileSelect(e, 'actividadesClase')">
            </label>
            <div class="mt-2 space-y-2">
              <div v-for="(file, idx) in form.actividadesClase" :key="idx" class="flex items-center justify-between bg-white p-2 rounded border border-slate-200 text-xs">
                <span class="truncate w-24" :title="file.name">{{ file.name }}</span>
                <button type="button" @click="removeFile('actividadesClase', idx)" class="text-red-500 hover:text-red-700 font-bold">X</button>
              </div>
            </div>
          </div>

          <!-- Tareas Casa -->
          <div class="border border-slate-200 bg-slate-50 rounded-xl p-4">
            <h4 class="font-bold text-sm text-slate-700 mb-2">Tareas en Casa</h4>
            <label class="cursor-pointer block text-center py-4 border-2 border-dashed border-slate-300 rounded-lg hover:bg-slate-100 transition">
              <span class="text-xs text-blue-600 font-bold">+ Subir Imágenes</span>
              <input type="file" multiple accept="image/*" class="hidden" @change="e => handleFileSelect(e, 'tareasCasa')">
            </label>
            <div class="mt-2 space-y-2">
              <div v-for="(file, idx) in form.tareasCasa" :key="idx" class="flex items-center justify-between bg-white p-2 rounded border border-slate-200 text-xs">
                <span class="truncate w-24" :title="file.name">{{ file.name }}</span>
                <button type="button" @click="removeFile('tareasCasa', idx)" class="text-red-500 hover:text-red-700 font-bold">X</button>
              </div>
            </div>
          </div>

          <!-- Examen/Proyecto -->
          <div class="border border-slate-200 bg-slate-50 rounded-xl p-4">
            <h4 class="font-bold text-sm text-slate-700 mb-2">Examen / Proyecto</h4>
            <label class="cursor-pointer block text-center py-4 border-2 border-dashed border-slate-300 rounded-lg hover:bg-slate-100 transition">
              <span class="text-xs text-blue-600 font-bold">+ Subir Imágenes</span>
              <input type="file" multiple accept="image/*" class="hidden" @change="e => handleFileSelect(e, 'examenProyecto')">
            </label>
            <div class="mt-2 space-y-2">
              <div v-for="(file, idx) in form.examenProyecto" :key="idx" class="flex items-center justify-between bg-white p-2 rounded border border-slate-200 text-xs">
                <span class="truncate w-24" :title="file.name">{{ file.name }}</span>
                <button type="button" @click="removeFile('examenProyecto', idx)" class="text-red-500 hover:text-red-700 font-bold">X</button>
              </div>
            </div>
          </div>
        </div>

        <!-- CONCLUSIÓN -->
        <div>
          <label class="block text-sm font-bold text-slate-700 mb-2">Conclusiones</label>
          <textarea 
            v-model="form.conclusiones" 
            rows="3" 
            class="block w-full px-4 py-3 border-2 border-slate-100 rounded-xl bg-slate-50 text-slate-800 focus:ring-4 focus:ring-blue-500/10 text-sm resize-y" 
            placeholder="Escribe tus conclusiones..."
            required
          ></textarea>
        </div>

        <!-- ALERTAS -->
        <Transition name="slide-fade">
          <div v-if="errorMessage" class="rounded-xl bg-red-50/80 p-3 border border-red-200/50">
            <p class="text-sm font-semibold text-red-700 text-center">{{ errorMessage }}</p>
          </div>
        </Transition>
        <Transition name="slide-fade">
          <div v-if="successMessage" class="rounded-xl bg-green-50/80 p-3 border border-green-200/50">
            <p class="text-sm font-semibold text-green-700 text-center">{{ successMessage }}</p>
          </div>
        </Transition>

        <!-- BOTONES -->
        <div class="mt-4 pt-4 flex justify-end space-x-3 border-t">
          <button 
            type="button" 
            @click="closeModal"
            class="px-6 py-2.5 rounded-xl text-slate-500 font-bold hover:bg-slate-100 transition-all"
            :disabled="isLoading"
          >
            Cancelar
          </button>
          <button 
            type="submit" 
            class="relative px-6 py-2.5 bg-slate-900 rounded-xl text-white font-bold hover:bg-slate-800 transition-all disabled:opacity-50"
            :disabled="isLoading"
          >
            {{ isLoading ? 'Subiendo...' : 'Entregar Evidencia' }}
          </button>
        </div>
      </form>
    </template>
  </BasePremiumModal>
</template>
