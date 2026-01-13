import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import CompteurList from '../views/compteur/CompteurList.vue'
import CompteurCreate from '../views/compteur/CompteurCreate.vue'
import FabricationList from '../views/fabrication/FabricationList.vue'
import FabricationCreate from '../views/fabrication/FabricationCreate.vue'
import OfList from '../views/fabrication/OfList.vue'
import OfCreate from '../views/fabrication/OfCreate.vue'

const routes = [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', component: Home },
  { path: '/compteurs', component: CompteurList },
  { path: '/compteurs/nouveau', component: CompteurCreate },
  { path: '/fabrications', component: FabricationList },
  { path: '/fabrications/nouveau', component: FabricationCreate },
  { path: '/ofs', component: OfList },
  { path: '/ofs/nouveau', component: OfCreate },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('user') !== null
  
  if (!to.meta.public && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
