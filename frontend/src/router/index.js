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
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/MisClasesView.vue'),
      meta: { requiresAuth: true } // Esta etiqueta es para proteger la ruta
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
  // Verificamos si la ruta a la que quiere ir el usuario requiere autenticación
  const requiereAutenticacion = to.matched.some(record => record.meta.requiresAuth)

  // Leemos si existe nuestro token falso en el almacenamiento local
  const token = localStorage.getItem('auth_token')

  if (requiereAutenticacion && !token) {
    // Si la página es secreta y NO hay token, lo mandamos al login
    next('/login')
  } else {
    // En cualquier otro caso, lo dejamos pasar libremente
    next()
  }
})

export default router