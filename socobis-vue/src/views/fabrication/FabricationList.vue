<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const fabrications = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const total = ref(0)

// Filtres de recherche
// etatTable correspond aux vues FABRICATIONCPL* comme dans la JSP ERP
const filters = ref({
  id: '',
  lancePar: '',
  cible: '',
  remarque: '',
  libelle: '',
  datyMin: new Date().toISOString().split('T')[0],
  datyMax: new Date().toISOString().split('T')[0],
  idOf: '',
  idOffille: '',
  etatTable: 'FABRICATIONCPL'
})

const fetchFabrications = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('action', 'list')
    if (filters.value.id) params.append('id', filters.value.id)
    if (filters.value.lancePar) params.append('lancePar', filters.value.lancePar)
    if (filters.value.cible) params.append('cible', filters.value.cible)
    if (filters.value.remarque) params.append('remarque', filters.value.remarque)
    if (filters.value.libelle) params.append('libelle', filters.value.libelle)
    if (filters.value.datyMin) params.append('datyMin', filters.value.datyMin)
    if (filters.value.datyMax) params.append('datyMax', filters.value.datyMax)
    if (filters.value.idOf) params.append('idOf', filters.value.idOf)
    if (filters.value.idOffille) params.append('idOffille', filters.value.idOffille)
    if (filters.value.etatTable) params.append('etatTable', filters.value.etatTable)

    const response = await axios.get(`/FabricationServlet?${params.toString()}`)
    
    let payload: any
    if (typeof response.data === 'string') {
      console.log('Réponse brute reçue (string):', response.data)
      // On essaie de parser directement. Si ça échoue, on tente de nettoyer.
      try {
        payload = JSON.parse(response.data)
      } catch (e) {
        const raw = response.data
        const start = raw.indexOf('{')
        // Au lieu de lastIndexOf, on cherche la fin du PREMIER objet JSON valide
        // car le bug du backend envoyait deux objets à la suite.
        if (start !== -1) {
          let depth = 0
          let end = -1
          for (let i = start; i < raw.length; i++) {
            if (raw[i] === '{') depth++
            else if (raw[i] === '}') {
              depth--
              if (depth === 0) {
                end = i
                break
              }
            }
          }
          if (end !== -1) {
            payload = JSON.parse(raw.substring(start, end + 1))
          } else {
            throw new Error('Impossible de trouver un objet JSON valide')
          }
        } else {
          throw new Error('Aucun objet JSON trouvé dans la réponse')
        }
      }
    } else {
      payload = response.data
    }

    if (payload.status === 'success') {
      fabrications.value = payload.data || []
      total.value = payload.total || fabrications.value.length
      error.value = null
    } else {
      error.value = payload.message
      fabrications.value = []
    }
  } catch (err: any) {
    console.error('Erreur lors de la récupération des fabrications:', err)
    error.value = 'Erreur lors de la récupération des fabrications'
  } finally {
    loading.value = false
  }
}

const getStatusLabel = (etatLib: any) => {
  // etatLib vient directement de la vue FABRICATIONCPL* (ex: CREE, VALIDEE, ANNULEE...)
  if (!etatLib) return 'INCONNU'
  return etatLib
}

onMounted(() => {
  // Initialiser les dates (ex: du 12 au 14 comme dans l'exemple)
  const today = new Date()
  filters.value.datyMax = today.toISOString().split('T')[0]
  const twoDaysAgo = new Date()
  twoDaysAgo.setDate(today.getDate() - 2)
  filters.value.datyMin = twoDaysAgo.toISOString().split('T')[0]
  
  fetchFabrications()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-bold text-slate-900 tracking-tight">Fabrications</h1>
        <p class="mt-1 text-slate-500">Suivez l'état d'avancement de vos opérations de fabrication.</p>
      </div>
      <router-link 
        to="/fabrications/nouveau" 
        class="inline-flex items-center justify-center px-5 py-2.5 border border-transparent text-sm font-semibold rounded-xl shadow-sm text-white bg-emerald-600 hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 transition-all active:scale-95"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Nouvelle Fabrication
      </router-link>
    </div>

    <!-- Filters Section -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden mb-8 transition-all hover:shadow-md">
      <div class="p-6">
        <div class="flex items-center gap-2 mb-6 text-slate-800">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-emerald-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
          </svg>
          <h2 class="font-bold uppercase tracking-wider text-xs">Filtres de recherche</h2>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-x-6 gap-y-4">
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">ID Fabrication</label>
            <input v-model="filters.id" type="text" placeholder="Ex: FAB-001" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-emerald-500 focus:ring-emerald-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Lancée par</label>
            <input v-model="filters.lancePar" type="text" placeholder="Responsable" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-emerald-500 focus:ring-emerald-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Cible</label>
            <input v-model="filters.cible" type="text" placeholder="Destination" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-emerald-500 focus:ring-emerald-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">État</label>
            <select v-model="filters.etatTable" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-emerald-500 focus:ring-emerald-500 transition-colors">
              <option value="FABRICATIONCPL">Tous les états</option>
              <option value="FABRICATIONCPLCREE">Créée(s)</option>
              <option value="FABRICATIONCPLVISEE">Validée(s)</option>
              <option value="FABRICATIONCPLANNULE">Annulée(s)</option>
              <option value="FABRICATIONCPLENTAMEE">Entamée(s)</option>
              <option value="FABRICATIONCPLBLOQUEE">Bloquée(s)</option>
              <option value="FABRICATIONCPLBTERMINEE">Terminée(s)</option>
            </select>
          </div>
          <div class="space-y-1 lg:col-span-2">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Libellé / Désignation</label>
            <input v-model="filters.libelle" type="text" placeholder="Rechercher une fabrication..." class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-emerald-500 focus:ring-emerald-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Date min</label>
            <input v-model="filters.datyMin" type="date" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-emerald-500 focus:ring-emerald-500 transition-colors" />
          </div>
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-500 uppercase ml-1">Date max</label>
            <input v-model="filters.datyMax" type="date" class="block w-full rounded-xl border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm focus:border-emerald-500 focus:ring-emerald-500 transition-colors" />
          </div>
        </div>

        <div class="mt-8 flex justify-end">
          <button
            @click="fetchFabrications"
            class="inline-flex items-center px-6 py-2.5 border border-emerald-200 text-sm font-bold rounded-xl text-emerald-700 bg-emerald-50 hover:bg-emerald-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 transition-all active:scale-95"
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
      <div class="flex items-center gap-6">
        <span class="text-sm font-medium text-slate-500">Total : <span class="text-slate-900 font-bold">{{ total }}</span></span>
        <span class="text-sm font-medium text-slate-500">Affichés : <span class="text-slate-900 font-bold">{{ fabrications.length }}</span></span>
      </div>
    </div>

    <!-- Main Content -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600 mb-4"></div>
        <p class="text-slate-500 font-medium">Chargement des fabrications...</p>
      </div>

      <div v-else-if="error" class="p-8 text-center">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50 text-red-500 mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <p class="text-slate-900 font-bold text-lg mb-2">Erreur</p>
        <p class="text-slate-500 mb-6">{{ error }}</p>
        <button @click="fetchFabrications" class="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition-colors">
          Réessayer
        </button>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-slate-200">
          <thead class="bg-slate-50/50">
            <tr>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">ID</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Info & Dates</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Désignation</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Responsable</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Ordres Associés</th>
              <th scope="col" class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">État</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-slate-200">
            <tr v-for="(f, index) in fabrications" :key="f.id || index" class="hover:bg-slate-50/80 transition-colors group">
              <td class="px-6 py-4 whitespace-nowrap">
                <router-link 
                  :to="`/fabrications/${f.id}`" 
                  class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-700 hover:bg-emerald-100 transition-colors"
                >
                  #{{ f.id }}
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </router-link>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex flex-col space-y-1">
                  <span class="text-sm text-slate-900 flex items-center font-medium">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {{ f.daty }}
                  </span>
                  <span class="text-xs text-slate-500 flex items-center">
                    Cible: {{ f.cible }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="flex flex-col">
                  <span class="text-sm font-semibold text-slate-900 group-hover:text-emerald-600 transition-colors line-clamp-1">
                    {{ f.libelle }}
                  </span>
                  <span class="text-xs text-slate-500 italic line-clamp-1">{{ f.remarque || 'Aucune remarque' }}</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-700">
                <div class="flex items-center">
                  <div class="h-7 w-7 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 mr-2 border border-slate-200">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                    </svg>
                  </div>
                  {{ f.lancePar }}
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex flex-col space-y-1">
                  <span v-if="f.idOf" class="text-xs font-medium text-slate-600 flex items-center bg-slate-50 px-2 py-0.5 rounded border border-slate-100 w-fit">
                    OF: {{ f.idOf }}
                  </span>
                  <span v-if="f.idOffille" class="text-xs font-medium text-slate-600 flex items-center bg-slate-50 px-2 py-0.5 rounded border border-slate-100 w-fit">
                    OF Fille: {{ f.idOffille }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="{
                  'px-2.5 py-1 text-xs font-bold rounded-full inline-flex items-center': true,
                  'bg-blue-50 text-blue-700 border border-blue-100': f.etat == 1,
                  'bg-emerald-50 text-emerald-700 border border-emerald-100': f.etat == 10 || f.etat == 11,
                  'bg-red-50 text-red-700 border border-red-100': f.etat == -1,
                  'bg-amber-50 text-amber-700 border border-amber-100': f.etat != 1 && f.etat != 10 && f.etat != 11 && f.etat != -1
                }">
                  <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="{
                    'bg-blue-500': f.etat == 1,
                    'bg-emerald-500': f.etat == 10 || f.etat == 11,
                    'bg-red-500': f.etat == -1,
                    'bg-amber-500': f.etat != 1 && f.etat != 10 && f.etat != 11 && f.etat != -1
                  }"></span>
                  {{ getStatusLabel(f.etatLib) }}
                </span>
              </td>
            </tr>
            <tr v-if="fabrications.length === 0">
              <td colspan="6" class="px-6 py-12 text-center">
                <div class="flex flex-col items-center">
                  <div class="bg-slate-50 p-4 rounded-full mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0a2 2 0 01-2 2H6a2 2 0 01-2-2m16 0l-8 4-8-4" />
                    </svg>
                  </div>
                  <p class="text-slate-500 font-medium">Aucune fabrication trouvée</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

