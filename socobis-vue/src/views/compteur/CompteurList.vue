<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const compteurs = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const fetchCompteurs = async () => {
  loading.value = true
  try {
    const response = await axios.get('/CompteurServlet?action=list')
    if (response.data.status === 'success') {
      compteurs.value = response.data.data
    } else {
      error.value = response.data.message || "Erreur lors de la récupération des compteurs"
    }
  } catch (err: any) {
    console.error(err)
    error.value = err.response?.data?.message || "Erreur lors de la récupération des compteurs"
    if (err.response?.status === 401) {
      error.value = "Session expirée. Veuillez vous reconnecter."
      localStorage.removeItem('user')
      setTimeout(() => router.push('/login'), 2000)
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchCompteurs)
</script>

<template>
  <div class="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-bold text-slate-900 tracking-tight">Relevés de Compteurs</h1>
        <p class="mt-1 text-slate-500">Historique des relevés machines et production.</p>
      </div>
      <router-link 
        to="/compteurs/nouveau" 
        class="inline-flex items-center justify-center px-5 py-2.5 border border-transparent text-sm font-semibold rounded-xl shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all active:scale-95"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Nouveau Relevé
      </router-link>
    </div>

    <!-- Main Content -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
        <p class="text-slate-500 font-medium">Chargement des relevés...</p>
      </div>

      <div v-else-if="error" class="p-8 text-center">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50 text-red-500 mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <p class="text-slate-900 font-bold text-lg mb-2">Erreur</p>
        <p class="text-slate-500 mb-6">{{ error }}</p>
        <button @click="fetchCompteurs" class="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition-colors">
          Réessayer
        </button>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-slate-200">
          <thead class="bg-slate-50/50">
            <tr>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Date & Heure</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Machine</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Index / Nombre</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Origine / Source</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-slate-200">
            <tr v-for="c in compteurs" :key="c.id" class="hover:bg-slate-50/80 transition-colors group">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex flex-col">
                  <span class="text-sm font-semibold text-slate-900 flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {{ c.daty }}
                  </span>
                  <span class="text-xs text-slate-500 flex items-center mt-0.5">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 mr-1.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {{ c.heure }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="h-8 w-8 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600 mr-3 border border-blue-100">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
                    </svg>
                  </div>
                  <span class="text-sm font-medium text-slate-700">{{ c.idMachine }}</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-3 py-1 rounded-xl text-sm font-bold bg-slate-100 text-slate-900 group-hover:bg-blue-600 group-hover:text-white transition-all shadow-sm">
                  {{ c.nombre }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="text-sm text-slate-500 bg-slate-50 px-2 py-1 rounded-lg border border-slate-100">
                  {{ c.idOrigine }}
                </span>
              </td>
            </tr>
            <tr v-if="compteurs.length === 0">
              <td colspan="4" class="px-6 py-12 text-center">
                <div class="flex flex-col items-center">
                  <div class="bg-slate-50 p-4 rounded-full mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <p class="text-slate-500 font-medium">Aucun relevé de compteur trouvé</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

