<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const ofs = ref<any[]>([])
const displayedOfs = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const filters = ref({
  id: '',
  lancePar: '',
  cible: '',
  remarque: '',
  libelle: '',
  dateBesoinMin: '',
  dateBesoinMax: '',
  dateMin: '',
  dateMax: ''
})

const normalizeDate = (value: any): string | null => {
  if (!value) return null
  const d = String(value)
  if (!d.trim()) return null
  // formats possibles: YYYY-MM-DD ou DD/MM/YYYY
  if (d.includes('/')) {
    const parts = d.split('/')
    if (parts.length < 3) return null
    const [day, month, year] = parts
    if (!day || !month || !year) return null
    return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`
  }
  if (d.includes('-')) {
    // on suppose déjà au format ISO ou proche
    return d.substring(0, 10)
  }
  return null
}

const fetchOfs = async (params?: any) => {
  loading.value = true
  try {
    const response = await axios.get('/OfServlet?action=list', { params })
    if (response.data.status === 'success') {
      ofs.value = response.data.data || []
      displayedOfs.value = ofs.value
    } else {
      error.value = response.data.message
    }
  } catch (err: any) {
    console.error(err)
    error.value = err.response?.data?.message || "Erreur lors de la récupération des Ordres de Fabrication"
    if (err.response?.status === 401) {
      error.value = "Session expirée. Veuillez vous reconnecter."
      localStorage.removeItem('user')
      setTimeout(() => router.push('/login'), 2000)
    }
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  // Construire les paramètres comme dans la JSP (ordre-fabrication-liste.jsp)
  const params: any = { action: 'list' }
  if (filters.value.id) params.id = filters.value.id
  if (filters.value.lancePar) params.lancepar = filters.value.lancePar
  if (filters.value.cible) params.cible = filters.value.cible
  if (filters.value.remarque) params.remarque = filters.value.remarque
  if (filters.value.libelle) params.libelle = filters.value.libelle
  if (filters.value.dateBesoinMin) params.besoin1 = filters.value.dateBesoinMin
  if (filters.value.dateBesoinMax) params.besoin2 = filters.value.dateBesoinMax
  if (filters.value.dateMin) params.daty1 = filters.value.dateMin
  if (filters.value.dateMax) params.daty2 = filters.value.dateMax

  loading.value = true
  axios.get('/OfServlet', { params })
    .then(response => {
      if (response.data.status === 'success') {
        ofs.value = response.data.data
        displayedOfs.value = ofs.value
      } else {
        error.value = response.data.message
      }
    })
    .catch((err: any) => {
      console.error(err)
      error.value = err.response?.data?.message || 'Erreur lors du filtrage des Ordres de Fabrication'
    })
    .finally(() => {
      loading.value = false
    })
}

onMounted(fetchOfs)
</script>

<template>
  <div class="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-bold text-slate-900 tracking-tight">Ordres de Fabrication</h1>
        <p class="mt-1 text-slate-500">Gérez et suivez vos ordres de fabrication en cours.</p>
      </div>
      <router-link 
        to="/ofs/nouveau" 
        class="inline-flex items-center justify-center px-5 py-2.5 border border-transparent text-sm font-semibold rounded-xl shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all active:scale-95"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Nouvel OF
      </router-link>
    </div>

    <!-- Filters Section -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden mb-8 transition-all hover:shadow-md">
      <div class="p-6">
        <div class="flex items-center gap-2 mb-6 text-slate-800">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
          </svg>
          <h2 class="font-bold uppercase tracking-wider text-xs">Filtres de recherche</h2>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-x-6 gap-y-4">
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">ID</label>
            <input v-model="filters.id" type="text" placeholder="Ex: OF-001" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-indigo-500 focus:ring-indigo-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Lancé par</label>
            <input v-model="filters.lancePar" type="text" placeholder="Nom du responsable" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-indigo-500 focus:ring-indigo-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Cible</label>
            <input v-model="filters.cible" type="text" placeholder="Destination" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-indigo-500 focus:ring-indigo-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Remarque</label>
            <input v-model="filters.remarque" type="text" placeholder="Mots clés..." class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-indigo-500 focus:ring-indigo-500 transition-colors" />
          </div>
          <div class="sm:col-span-2 space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Libellé</label>
            <input v-model="filters.libelle" type="text" placeholder="Désignation de l'OF" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-indigo-500 focus:ring-indigo-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Date besoin (min)</label>
            <input v-model="filters.dateBesoinMin" type="date" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-indigo-500 focus:ring-indigo-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Date besoin (max)</label>
            <input v-model="filters.dateBesoinMax" type="date" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-indigo-500 focus:ring-indigo-500 transition-colors" />
          </div>
        </div>

        <div class="mt-8 flex justify-end">
          <button
            type="button"
            @click="applyFilters"
            class="inline-flex items-center px-6 py-2.5 border border-indigo-200 text-sm font-bold rounded-xl text-indigo-700 bg-indigo-50 hover:bg-indigo-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all active:scale-95"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            Rechercher
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="flex items-center justify-between mb-4 px-2">
      <div class="flex items-center gap-4">
        <span class="text-sm font-medium text-slate-500">Résultats : <span class="text-slate-900 font-bold">{{ displayedOfs.length }}</span></span>
      </div>
    </div>

    <!-- Main Content -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"></div>
        <p class="text-slate-500 font-medium">Récupération des données...</p>
      </div>

      <div v-else-if="error" class="p-8 text-center">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50 text-red-500 mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <p class="text-slate-900 font-bold text-lg mb-2">Une erreur est survenue</p>
        <p class="text-slate-500 mb-6">{{ error }}</p>
        <button @click="fetchOfs" class="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition-colors">
          Réessayer
        </button>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-slate-200">
          <thead class="bg-slate-50/50">
            <tr>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">ID</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Dates</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Désignation</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Intervenants</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Cible & Remarque</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-slate-200">
            <tr v-for="of in displayedOfs" :key="of.id" class="hover:bg-slate-50/80 transition-colors group">
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-slate-100 text-slate-700 group-hover:bg-indigo-50 group-hover:text-indigo-700 transition-colors">
                  {{ of.id }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex flex-col space-y-1">
                  <span class="text-sm text-slate-900 flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {{ of.daty }}
                  </span>
                  <span class="text-xs text-indigo-600 font-medium flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Besoin: {{ of.besoin }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4">
                <span class="text-sm font-semibold text-slate-900 line-clamp-2 group-hover:text-indigo-600 transition-colors">
                  {{ of.libelle }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="h-8 w-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 border border-slate-200 mr-3 group-hover:bg-indigo-100 group-hover:text-indigo-600 group-hover:border-indigo-200 transition-all">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                    </svg>
                  </div>
                  <span class="text-sm font-medium text-slate-700">{{ of.lancePar }}</span>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="flex flex-col space-y-1">
                  <span class="text-sm text-slate-900 font-medium">{{ of.cible }}</span>
                  <span class="text-xs text-slate-500 italic">{{ of.remarque || 'Aucune remarque' }}</span>
                </div>
              </td>
            </tr>
            <tr v-if="displayedOfs.length === 0">
              <td colspan="5" class="px-6 py-12 text-center">
                <div class="flex flex-col items-center">
                  <div class="bg-slate-50 p-4 rounded-full mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 9.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <p class="text-slate-500 font-medium">Aucun Ordre de Fabrication trouvé</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

