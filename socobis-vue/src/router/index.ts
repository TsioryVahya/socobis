import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import CompteurList from '../views/compteur/CompteurList.vue'
import CompteurCreate from '../views/compteur/CompteurCreate.vue'
import FabricationList from '../views/fabrication/FabricationList.vue'
import FabricationCreate from '../views/fabrication/FabricationCreate.vue'
import FabricationDetail from '../views/fabrication/FabricationDetail.vue'
import FabricationMvtStock from '../views/fabrication/FabricationMvtStock.vue'
import OfList from '../views/fabrication/OfList.vue'
import OfCreate from '../views/fabrication/OfCreate.vue'
import MvtStockDetail from '../views/stock/MvtStockDetail.vue'
import IngredientDetail from '../views/produits/IngredientDetail.vue'
import ChargeSaisie from '../views/fabrication/ChargeSaisie.vue'

const routes = [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', component: Home },
  { path: '/compteurs', component: CompteurList },
  { path: '/compteurs/nouveau', component: CompteurCreate },
  { path: '/fabrications', component: FabricationList },
  { path: '/fabrications/nouveau', component: FabricationCreate, name: 'FabricationCreate', meta: { requiresAuth: true } },
  {
    path: '/fabrications/:id',
    name: 'FabricationDetail',
    component: FabricationDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/fabrications/:id/mvt-stock/:type',
    name: 'FabricationMvtStock',
    component: FabricationMvtStock,
    meta: { requiresAuth: true }
  },
  {
    path: '/fabrications/:id/charges/saisie',
    name: 'ChargeSaisie',
    component: ChargeSaisie,
    meta: { requiresAuth: true }
  },
  { path: '/ofs', component: OfList },
  { path: '/ofs/nouveau', component: OfCreate },
  {
    path: '/mouvements/:id',
    name: 'MvtStockDetail',
    component: MvtStockDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/ingredients/:id',
    name: 'IngredientDetail',
    component: IngredientDetail,
    meta: { requiresAuth: true }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const isAuthenticated = localStorage.getItem('user') !== null

  if (!to.meta.public && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
