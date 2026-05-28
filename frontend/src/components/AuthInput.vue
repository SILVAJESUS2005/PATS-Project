<script setup>
defineProps({
  id: {
    type: String,
    required: true
  },
  label: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  modelValue: {
    type: [String, Number],
    required: true
  },
  errorMessage: {
    type: String,
    default: ''
  }
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div class="mb-4">
    <label :for="id" class="block text-sm font-medium text-gray-700 mb-1">
      {{ label }}
    </label>
    <input 
      :id="id" 
      :name="id" 
      :type="type" 
      :placeholder="placeholder"
      :value="modelValue"
      v-bind="$attrs"
      @input="$emit('update:modelValue', $event.target.value)"
      class="appearance-none block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:border-transparent sm:text-sm transition-all duration-200"
      :class="[
        errorMessage 
          ? 'border-red-300 text-red-900 focus:ring-red-500' 
          : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500'
      ]"
    />
    <p v-if="errorMessage" class="mt-1 text-sm text-red-600">
      {{ errorMessage }}
    </p>
  </div>
</template>
