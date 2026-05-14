import { createRouter, createWebHistory } from 'vue-router'
// Importamos tus vistas
import RegistroView from '../views/RegistroView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/registro',
      name: 'registro',
      component: RegistroView
    },
    
    {
        path: '/login',
        name: 'login',
        component: () => import('../views/LoginView.vue')
    }
]
})

export default router