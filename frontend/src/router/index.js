import { createRouter, createWebHistory } from 'vue-router'
// Importamos tus vistas
import RegistroView from '../views/RegistroView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/registro',
      name: 'registro',
      component: RegistroView
    },

    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/MisClasesView.vue'),
      meta: { requiresAuth: true } // Esta etiqueta es para proteger la ruta
    },
    {
      path: '/clase/:id',
      name: 'clase-detalle',
      component: () => import('../views/ClaseDetalleView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/portafolio/:id/entregas',
      name: 'entregas-portafolio',
      component: () => import('../views/EntregasPortafolioView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/portafolios',
      name: 'portafolios',
      component: () => import('../views/DetallePortafoliosView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// === EL GUARDIA DE NAVEGACIÓN (Protección de Rutas) ===
router.beforeEach((to, from, next) => {
  const requiereAutenticacion = to.matched.some(record => record.meta.requiresAuth)
  const token = localStorage.getItem('auth_token')
  const rol = localStorage.getItem('user_role')

  if (requiereAutenticacion && !token) {
    next('/login')
  } else if (to.name === 'entregas-portafolio' && rol !== 'DOCENTE') {
    next('/dashboard')
  } else {
    next()
  }
})

export default router