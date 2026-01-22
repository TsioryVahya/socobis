<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref<string | null>(null)
const fabrication = ref<any | null>(null)
const details = ref<any[]>([])
const mouvements = ref<any[]>([])
const activeTab = ref('details')

const tabs = [
  { id: 'details', label: 'Détails' },
  { id: 'mouvements', label: 'Mouvement de stock' },
  { id: 'recette', label: 'Recette & Reviens' },
  // { id: 'charges', label: 'Charges rattachées' },
  // { id: 'rapprochement', label: 'Rapprochement' },
  // { id: 'historique', label: 'Historique' }
]

const getStatusLabel = (status: any) => {
  if (status === 1 || status === '1') return 'CREE'
  if (status === 11 || status === '11') return 'VALIDEE'
  if (status === 10 || status === '10') return 'VALIDEE'
  return status || 'INCONNU'
}

const getMvtStatusLabel = (status: any) => {
  // Se base sur les états standards du framework
  if (status === 1 || status === '1') return 'CREE'
  if (status === 10 || status === '10') return 'VALIDE'
  if (status === 20 || status === '20') return 'TERMINE'
  return status || 'INCONNU'
};

const formatCurrency = (value: number) => {
  if (typeof value !== 'number') return '-';
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MGA' }).format(value);
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  try {
    return format(new Date(dateString), 'dd/MM/yyyy');
  } catch (e) {
    return dateString;
  }
};

const handleValidate = async () => {
  if (!fabrication.value) return
  try {
    loading.value = true
    error.value = null
    const id = fabrication.value.id
    const resp = await axios.get('/FabricationServlet', {
      params: { action: 'valider', id }
    })

    let payload: any = resp.data
    if (typeof resp.data === 'string') {
      try { payload = JSON.parse(resp.data) } catch (e) {
        const raw = resp.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start !== -1 && end !== -1 && end > start) { payload = JSON.parse(raw.substring(start, end + 1)) } else { throw e }
      }
    }

    if (payload.status === 'success') {
      await fetchFabrication()
    } else {
      error.value = payload.message || "Erreur lors de la validation de la fabrication"
    }
  } catch (err: any) {
    console.error(err)
    error.value = "Erreur lors de la validation de la fabrication"
  } finally {
    loading.value = false
  }
}

const fetchFabrication = async () => {
  const id = route.params.id as string
  try {
    const response = await axios.get('/FabricationServlet', { params: { action: 'list', id } })
    let payload: any = response.data
    if (typeof response.data === 'string') {
      try { payload = JSON.parse(response.data) } catch (e) {
        const raw = response.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start !== -1 && end !== -1 && end > start) { payload = JSON.parse(raw.substring(start, end + 1)) } else { throw e }
      }
    }
    if (payload.status === 'success') {
      const list = payload.data || []
      fabrication.value = Array.isArray(list) ? list.find((f: any) => f.id === id) || list[0] : list
      if (!fabrication.value) {
        error.value = `Aucune fabrication trouvée pour l'ID ${id}`
        return
      }
    } else {
      error.value = payload.message || "Erreur lors du chargement de la fabrication"
      return
    }
  } catch (err: any) {
    console.error(err)
    error.value = "Erreur lors du chargement de la fabrication"
  }
}

const fetchDetails = async () => {
  const id = route.params.id as string
  try {
    const detailsResp = await axios.get('/FabricationServlet', { params: { action: 'details', id } })
    let detailsPayload: any = detailsResp.data
     if (typeof detailsResp.data === 'string') {
      try { detailsPayload = JSON.parse(detailsResp.data) } catch (e) {
        const raw = detailsResp.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start !== -1 && end !== -1 && end > start) { detailsPayload = JSON.parse(raw.substring(start, end + 1)) } else { throw e }
      }
    }
    if (detailsPayload.status === 'success') {
      details.value = detailsPayload.data || []
    }
  } catch (err) {
    console.error("Erreur chargement détails:", err)
  }
}

const fetchMouvements = async () => {
  const id = route.params.id as string
  try {
    const mvtResp = await axios.get('/FabricationServlet', { params: { action: 'mouvements', id } })
    let mvtPayload: any = mvtResp.data
    if (typeof mvtResp.data === 'string') {
      try { mvtPayload = JSON.parse(mvtResp.data) } catch (e) {
        const raw = mvtResp.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start !== -1 && end !== -1 && end > start) { mvtPayload = JSON.parse(raw.substring(start, end + 1)) } else { throw e }
      }
    }
    if (mvtPayload.status === 'success') {
      mouvements.value = mvtPayload.data || []
    }
  } catch (err) {
    console.error("Erreur chargement mouvements:", err)
  }
}

const selectTab = async (tabId: string) => {
  if (tabId === 'recette') {
    // Charger les mouvements si nécessaire et rediriger vers le mouvement de sortie
    if (mouvements.value.length === 0) {
      await fetchMouvements();
    }
    const sortie = mouvements.value.find((m: any) => {
      const t = (m.typeMouvement || '').toString().toLowerCase();
      return t.includes('sortie');
    });
    if (sortie && sortie.id) {
      router.push({ name: 'RecetteReviensDetail', params: { id: sortie.id } });
    } else {
      error.value = "Aucun mouvement de sortie trouvé pour cette fabrication.";
    }
    return;
  }
  activeTab.value = tabId
}

watch(activeTab, (newTab) => {
  if (newTab === 'details' && details.value.length === 0) {
    fetchDetails();
  } else if (newTab === 'mouvements') {
    // Toujours rafraîchir les mouvements quand on clique sur l'onglet
    // pour voir les changements après une validation.
    fetchMouvements();
  }
});

onMounted(async () => {
  loading.value = true
  error.value = null
  await fetchFabrication()
  if (fabrication.value) {
    if (activeTab.value === 'details') {
      await fetchDetails()
    } else if (activeTab.value === 'mouvements') {
      await fetchMouvements()
    }
  }
  loading.value = false
})
</script>

<template>
  <div class="min-h-screen bg-slate-50/50 pb-12">
    <!-- Header -->
    <div class="bg-white border-b border-slate-200 sticky top-0 z-10 backdrop-blur-md bg-white/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center gap-4">
            <button 
              @click="router.back()" 
              class="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500 hover:text-slate-700"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </button>
            <h1 class="text-xl font-bold text-slate-900">Fiche de Fabrication</h1>
          </div>
          
          <div v-if="fabrication" class="flex items-center gap-3">
            <span :class="{
              'px-3 py-1 text-xs font-bold rounded-full border': true,
              'bg-blue-50 text-blue-700 border-blue-100': fabrication.etat == 1,
              'bg-emerald-50 text-emerald-700 border-emerald-100': fabrication.etat == 10 || fabrication.etat == 11,
              'bg-amber-50 text-amber-700 border-amber-100': fabrication.etat != 1 && fabrication.etat != 10 && fabrication.etat != 11
            }">
              {{ getStatusLabel(fabrication.etat) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        <p class="mt-4 text-slate-500 font-medium">Chargement des données...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 border border-red-100 rounded-2xl p-4 flex items-start gap-3 text-red-700">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
        </svg>
        <p class="font-medium">{{ error }}</p>
      </div>

      <div v-else-if="fabrication" class="space-y-6">
        <!-- Informations Générales -->
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden transition-all hover:shadow-md">
          <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
            <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Informations Générales</h2>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Référence</p>
                <p class="text-base font-bold text-indigo-600">#{{ fabrication.id }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Date de création</p>
                <p class="text-base font-medium text-slate-900">{{ formatDate(fabrication.daty) }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Désignation</p>
                <p class="text-base font-medium text-slate-900">{{ fabrication.libelle || '-' }}</p>
              </div>
              <div class="md:col-span-3 space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Remarque</p>
                <p class="text-base text-slate-600 bg-slate-50 p-3 rounded-xl border border-slate-100">{{ fabrication.remarque || 'Aucune remarque' }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Onglets -->
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div class="border-b border-slate-200">
            <nav class="flex -mb-px px-6" aria-label="Tabs">
              <button
                v-for="tab in tabs"
                :key="tab.id"
                @click="selectTab(tab.id)"
                :class="[
                  'whitespace-nowrap py-4 px-6 border-b-2 font-bold text-sm transition-all',
                  activeTab === tab.id
                    ? 'border-indigo-600 text-indigo-600 bg-indigo-50/30'
                    : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'
                ]"
              >
                {{ tab.label }}
              </button>
            </nav>
          </div>

          <div class="p-6">
            <transition name="fade" mode="out-in">
              <!-- Onglet Détails -->
              <div v-if="activeTab === 'details'" key="details" class="overflow-x-auto">
                <table class="min-w-full divide-y divide-slate-200">
                  <thead>
                    <tr class="bg-slate-50">
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">ID</th>
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Composant</th>
                      <th class="px-4 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Quantité</th>
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Unité</th>
                      <th class="px-4 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">P.U.</th>
                      <th class="px-4 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Montant</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-slate-100">
                    <tr v-for="(d, idx) in details" :key="d.id || idx" class="hover:bg-slate-50/80 transition-colors group">
                      <td class="px-4 py-3 whitespace-nowrap text-xs text-slate-500 font-mono">{{ d.id }}</td>
                      <td class="px-4 py-3 whitespace-nowrap">
                        <router-link :to="{ name: 'IngredientDetail', params: { id: d.idIngredients } }" class="text-sm font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1">
                          {{ d.idIngredients }}
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 opacity-0 group-hover:opacity-100 transition-opacity" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                          </svg>
                        </router-link>
                      </td>
                      <td class="px-4 py-3 whitespace-nowrap text-sm text-right font-bold text-slate-900">{{ d.qte }}</td>
                      <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-600">{{ d.idunitelib }}</td>
                      <td class="px-4 py-3 whitespace-nowrap text-sm text-right text-slate-600">{{ formatCurrency(d.pu) }}</td>
                      <td class="px-4 py-3 whitespace-nowrap text-sm text-right font-bold text-slate-900">{{ formatCurrency(d.montant) }}</td>
                    </tr>
                    <tr v-if="details.length === 0">
                      <td colspan="6" class="px-4 py-12 text-center">
                        <div class="flex flex-col items-center">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-slate-200 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                          </svg>
                          <p class="text-slate-400 font-medium italic">Aucun détail pour cette fabrication.</p>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Onglet Mouvements -->
              <div v-else-if="activeTab === 'mouvements'" key="mouvements" class="overflow-x-auto">
                <table class="min-w-full divide-y divide-slate-200">
                  <thead>
                    <tr class="bg-slate-50">
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">ID</th>
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Date</th>
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Désignation</th>
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Type</th>
                      <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Magasin</th>
                      <th class="px-4 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Montant</th>
                      <th class="px-4 py-3 text-center text-xs font-bold text-slate-500 uppercase tracking-wider">État</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-slate-100">
                    <tr v-for="(mvt, idx) in mouvements" :key="mvt.id || idx" class="hover:bg-slate-50/80 transition-colors group">
                      <td class="px-4 py-3 whitespace-nowrap">
                        <router-link :to="{ name: 'MvtStockDetail', params: { id: mvt.id } }" class="text-xs font-mono font-bold text-indigo-600 hover:text-indigo-800">
                          #{{ mvt.id }}
                        </router-link>
                      </td>
                      <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-600">{{ formatDate(mvt.daty) }}</td>
                      <td class="px-4 py-3 text-sm font-medium text-slate-900">{{ mvt.designation }}</td>
                      <td class="px-4 py-3 whitespace-nowrap">
                        <span class="px-2 py-0.5 text-[10px] font-bold rounded bg-slate-100 text-slate-600 border border-slate-200">
                          {{ mvt.typeMouvement }}
                        </span>
                      </td>
                      <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-600">{{ mvt.magasin }}</td>
                      <td class="px-4 py-3 whitespace-nowrap text-sm text-right font-bold text-slate-900">{{ formatCurrency(mvt.montant) }}</td>
                      <td class="px-4 py-3 whitespace-nowrap text-center">
                        <span :class="{
                          'px-2 py-0.5 text-[10px] font-bold rounded-full': true,
                          'bg-blue-50 text-blue-600': mvt.etat == 1,
                          'bg-emerald-50 text-emerald-600': mvt.etat >= 10,
                          'bg-slate-50 text-slate-600': mvt.etat < 1 && mvt.etat != null
                        }">
                          {{ getMvtStatusLabel(mvt.etat) }}
                        </span>
                      </td>
                    </tr>
                    <tr v-if="mouvements.length === 0">
                      <td colspan="7" class="px-4 py-12 text-center">
                        <div class="flex flex-col items-center">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-slate-200 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                          </svg>
                          <p class="text-slate-400 font-medium italic">Aucun mouvement de stock pour cette fabrication.</p>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </transition>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
          <div class="flex flex-wrap items-center gap-2">
            <button
              @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'residu' } })"
              class="inline-flex items-center px-4 py-2 text-sm font-bold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-50 transition-all active:scale-95 shadow-sm"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Résidu
            </button>
            <template v-if="fabrication.etat >= 11 || fabrication.etat === '11'">
              <button
                @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'entree' } })"
                class="inline-flex items-center px-4 py-2 text-sm font-bold text-emerald-700 bg-emerald-50 border border-emerald-100 rounded-xl hover:bg-emerald-100 transition-all active:scale-95 shadow-sm"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                </svg>
                Entrée Stock
              </button>
              <button
                @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'sortie' } })"
                class="inline-flex items-center px-4 py-2 text-sm font-bold text-amber-700 bg-amber-50 border border-amber-100 rounded-xl hover:bg-amber-100 transition-all active:scale-95 shadow-sm"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
                Sortie Stock
              </button>
            </template>
          </div>

          <div class="flex items-center gap-3 w-full sm:w-auto">
            <button
              v-if="fabrication.etat === 1 || fabrication.etat === '1'"
              @click="handleValidate"
              class="flex-1 sm:flex-none inline-flex items-center justify-center px-6 py-2.5 text-sm font-bold text-white bg-indigo-600 rounded-xl hover:bg-indigo-700 transition-all active:scale-95 shadow-indigo-200 shadow-lg"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Valider la fabrication
            </button>
            <button
              @click="router.push('/fabrications')"
              class="flex-1 sm:flex-none inline-flex items-center justify-center px-6 py-2.5 text-sm font-bold text-slate-700 bg-slate-100 rounded-xl hover:bg-slate-200 transition-all active:scale-95"
            >
              Retour
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}
</style>

