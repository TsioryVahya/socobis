<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
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
const charges = ref<any[]>([])
const activeTab = ref('details')

const tabs = [
  { id: 'details', label: 'Détails' },
  { id: 'mouvements', label: 'Mouvement de stock' },
  { id: 'charges', label: 'Charges' },
]

const totalCharges = computed(() => {
  return charges.value.reduce((sum, item) => sum + (Number(item.montant) || 0), 0)
})

const totalBudgetsFab = computed(() => {
  // On considère comme coût de revient tous les mouvements qui ne sont pas des entrées (le produit fini)
  return mouvements.value
    .filter(m => {
       const type = (m.typeMouvement || '').toUpperCase();
       return !type.includes('ENTREE');
    })
    .reduce((sum, item) => sum + (Number(item.montant) || 0), 0)
})

const totalPrixRevient = computed(() => {
  return Number(totalCharges.value) + Number(totalBudgetsFab.value)
})

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

const fetchCharges = async () => {
  const id = route.params.id as string
  try {
    const resp = await axios.get('/ChargeServlet', { params: { action: 'list', idFabrication: id } })
    if (resp.data.status === 'success') {
      charges.value = resp.data.data || []
    }
  } catch (err) {
    console.error("Erreur chargement charges:", err)
  }
}

const selectTab = (tabId: string) => {
  activeTab.value = tabId
}

watch(activeTab, (newTab) => {
  if (newTab === 'details' && details.value.length === 0) {
    fetchDetails();
  } else if (newTab === 'mouvements') {
    // Toujours rafraîchir les mouvements quand on clique sur l'onglet
    // pour voir les changements après une validation.
    fetchMouvements();
  } else if (newTab === 'charges') {
    fetchCharges();
  }
});

onMounted(async () => {
  loading.value = true
  error.value = null
  await fetchFabrication()
  if (fabrication.value) {
    // On charge tout en parallèle pour avoir les totaux disponibles partout
    await Promise.all([
      fetchDetails(),
      fetchMouvements(),
      fetchCharges()
    ])
  }
  loading.value = false
})
</script>

<template>
  <div class="space-y-6 max-w-6xl mx-auto">
    <!-- Header with Back Button -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
      <div class="flex items-center">
        <button @click="router.back()" class="p-2 mr-4 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <div>
          <h1 class="text-2xl font-bold text-slate-800">Fiche Fabrication</h1>
          <p class="text-slate-500 text-sm mt-1" v-if="fabrication">ID: {{ fabrication.id }}</p>
        </div>
      </div>
      
      <div class="flex items-center gap-3" v-if="fabrication">
        <span class="px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-widest border" :class="{
          'bg-indigo-50 text-indigo-600 border-indigo-100': fabrication.etat == 1,
          'bg-emerald-50 text-emerald-600 border-emerald-100': fabrication.etat == 10 || fabrication.etat == 11
        }">
          {{ getStatusLabel(fabrication.etat) }}
        </span>
      </div>
    </div>

    <div v-if="loading" class="flex flex-col items-center justify-center py-20 bg-white rounded-3xl border border-slate-100">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"></div>
      <p class="text-slate-400 font-medium">Chargement des données...</p>
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-100 p-6 rounded-3xl animate-shake">
      <div class="flex items-center text-red-700">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <p class="font-bold">Erreur</p>
      </div>
      <p class="text-red-600 mt-2 ml-9">{{ error }}</p>
    </div>

    <div v-else-if="fabrication" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Info Sidebar -->
      <div class="lg:col-span-1 space-y-6">
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-200">
          <h3 class="text-xs font-bold text-slate-400 uppercase tracking-widest mb-6">Informations de base</h3>
          
          <div class="space-y-6">
            <div>
              <label class="block text-xs font-semibold text-slate-400 uppercase mb-1">Désignation</label>
              <p class="text-slate-800 font-bold leading-tight">{{ fabrication.libelle || 'Sans libellé' }}</p>
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-400 uppercase mb-1">Date</label>
              <p class="text-slate-800 font-medium">{{ formatDate(fabrication.daty) }}</p>
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-400 uppercase mb-1">Magasin Cible</label>
              <p class="text-slate-800 font-medium">{{ fabrication.cible || '-' }}</p>
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-400 uppercase mb-1">Remarque</label>
              <p class="text-slate-600 text-sm italic">{{ fabrication.remarque || 'Aucune remarque' }}</p>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="bg-indigo-900 p-6 rounded-3xl shadow-xl shadow-indigo-900/20 text-white space-y-4">
          <h3 class="text-xs font-bold text-indigo-300 uppercase tracking-widest mb-2">Actions disponibles</h3>
          
          <button 
            v-if="fabrication.etat === 1 || fabrication.etat === '1'"
            @click="handleValidate"
            class="w-full py-4 bg-emerald-500 hover:bg-emerald-600 text-white rounded-2xl font-bold transition-all shadow-lg shadow-emerald-500/20 flex items-center justify-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            Valider la Fabrication
          </button>

          <div class="grid grid-cols-1 gap-2">
            <button @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'residu' } })" class="w-full py-3 bg-white/10 hover:bg-white/20 text-white rounded-xl text-sm font-semibold transition-all text-left px-4 flex items-center">
              <span class="w-2 h-2 bg-amber-400 rounded-full mr-3"></span> Résidu
            </button>
            <button 
              v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
              @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'entree' } })" 
              class="w-full py-3 bg-white/10 hover:bg-white/20 text-white rounded-xl text-sm font-semibold transition-all text-left px-4 flex items-center"
            >
              <span class="w-2 h-2 bg-emerald-400 rounded-full mr-3"></span> Mouvement entrée
            </button>
            <button 
              v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
              @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'sortie' } })" 
              class="w-full py-3 bg-white/10 hover:bg-white/20 text-white rounded-xl text-sm font-semibold transition-all text-left px-4 flex items-center"
            >
              <span class="w-2 h-2 bg-rose-400 rounded-full mr-3"></span> Mouvement sortie
            </button>
            <button 
              @click="router.push({ name: 'ChargeSaisie', params: { id: fabrication.id } })" 
              class="w-full py-3 bg-white/10 hover:bg-white/20 text-white rounded-xl text-sm font-semibold transition-all text-left px-4 flex items-center"
            >
              <span class="w-2 h-2 bg-indigo-400 rounded-full mr-3"></span> Saisie des charges
            </button>
          </div>
        </div>
      </div>

      <!-- Content Area -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Résumé Financier -->
        <div v-if="fabrication" class="bg-gradient-to-br from-indigo-600 to-violet-700 rounded-3xl p-8 text-white shadow-xl shadow-indigo-200">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
            <div>
              <p class="text-indigo-100 text-xs font-bold uppercase tracking-widest mb-1">Prix de Revient Total</p>
              <h2 class="text-4xl font-black">{{ formatCurrency(totalPrixRevient) }}</h2>
            </div>
            <div class="flex gap-8">
              <div class="text-right">
                <p class="text-indigo-200 text-[10px] font-bold uppercase tracking-widest mb-1">Budgets Fab (Mvt)</p>
                <p class="text-xl font-bold">{{ formatCurrency(totalBudgetsFab) }}</p>
              </div>
              <div class="text-right">
                <p class="text-indigo-200 text-[10px] font-bold uppercase tracking-widest mb-1">Total Charges</p>
                <p class="text-xl font-bold">{{ formatCurrency(totalCharges) }}</p>
              </div>
            </div>
          </div>
          <div class="mt-6 pt-6 border-t border-white/10 flex items-center text-xs text-indigo-100 italic">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Le prix de revient est calculé en additionnant les charges rattachées et les mouvements de sortie de stock.
          </div>
        </div>

        <!-- Tabs -->
        <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
          <div class="flex border-b border-slate-100 bg-slate-50/50 p-2">
            <button 
              v-for="tab in tabs" 
              :key="tab.id"
              @click="selectTab(tab.id)"
              class="px-6 py-3 rounded-2xl text-sm font-bold transition-all"
              :class="activeTab === tab.id ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="p-0">
            <!-- Details Tab -->
            <div v-if="activeTab === 'details'" class="animate-fadeIn">
              <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                  <thead>
                    <tr class="text-xs font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">
                      <th class="px-6 py-4">Composant</th>
                      <th class="px-6 py-4 text-right">Quantité</th>
                      <th class="px-6 py-4">Unité</th>
                      <th class="px-6 py-4 text-right">Montant</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-50">
                    <tr v-for="d in details" :key="d.id" class="hover:bg-slate-50 transition-colors group">
                      <td class="px-6 py-4">
                        <router-link :to="{ name: 'IngredientDetail', params: { id: d.idIngredients } }" class="text-indigo-600 font-bold group-hover:underline">
                          {{ d.idIngredients }}
                        </router-link>
                      </td>
                      <td class="px-6 py-4 text-right font-medium text-slate-700">{{ d.qte }}</td>
                      <td class="px-6 py-4 text-slate-500">{{ d.idunitelib }}</td>
                      <td class="px-6 py-4 text-right">
                        <div class="text-slate-800 font-bold">{{ formatCurrency(d.montant) }}</div>
                        <div class="text-[10px] text-slate-400">PU: {{ formatCurrency(d.pu) }}</div>
                      </td>
                    </tr>
                    <tr v-if="details.length === 0">
                      <td colspan="4" class="px-6 py-12 text-center text-slate-400 italic">Aucun détail disponible</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Movements Tab -->
            <div v-if="activeTab === 'mouvements'" class="animate-fadeIn">
              <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                  <thead>
                    <tr class="text-xs font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">
                      <th class="px-6 py-4">Mouvement</th>
                      <th class="px-6 py-4">Type</th>
                      <th class="px-6 py-4 text-right">Montant</th>
                      <th class="px-6 py-4 text-center">État</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-50">
                    <tr v-for="mvt in mouvements" :key="mvt.id" class="hover:bg-slate-50 transition-colors">
                      <td class="px-6 py-4">
                        <router-link :to="{ name: 'MvtStockDetail', params: { id: mvt.id } }" class="text-indigo-600 font-bold hover:underline block">
                          {{ mvt.id }}
                        </router-link>
                        <span class="text-[10px] text-slate-400">{{ formatDate(mvt.daty) }}</span>
                      </td>
                      <td class="px-6 py-4">
                        <span class="text-xs font-medium text-slate-600">{{ mvt.typeMouvement }}</span>
                        <div class="text-[10px] text-slate-400">{{ mvt.magasin }}</div>
                      </td>
                      <td class="px-6 py-4 text-right font-bold text-slate-800">{{ formatCurrency(mvt.montant) }}</td>
                      <td class="px-6 py-4 text-center">
                        <span class="px-2 py-1 rounded-lg text-[10px] font-bold uppercase tracking-tighter" :class="{
                          'bg-indigo-50 text-indigo-600': mvt.etat == 1,
                          'bg-emerald-50 text-emerald-600': mvt.etat >= 10
                        }">
                          {{ getMvtStatusLabel(mvt.etat) }}
                        </span>
                      </td>
                    </tr>
                    <tr v-if="mouvements.length > 0" class="bg-slate-50 font-bold border-t-2 border-slate-200">
                      <td colspan="2" class="px-6 py-4 text-slate-500 uppercase text-[10px] tracking-widest">Sous-total (Mouvements Sorties/Résidus)</td>
                      <td class="px-6 py-4 text-right text-indigo-600">{{ formatCurrency(totalBudgetsFab) }}</td>
                      <td></td>
                    </tr>
                    <tr v-if="mouvements.length === 0">
                      <td colspan="4" class="px-6 py-12 text-center text-slate-400 italic">Aucun mouvement enregistré</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Charges Tab -->
            <div v-if="activeTab === 'charges'" class="animate-fadeIn">
              <div class="p-6 bg-slate-50/50 border-b border-slate-100 flex justify-between items-center">
                <span class="text-sm font-bold text-slate-500 uppercase tracking-widest">Récapitulatif des charges</span>
                <div class="text-right">
                  <span class="text-xs text-slate-400 uppercase block">Total des charges</span>
                  <span class="text-2xl font-black text-indigo-600">{{ formatCurrency(totalCharges) }}</span>
                </div>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                  <thead>
                    <tr class="text-xs font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">
                      <th class="px-6 py-4">Date</th>
                      <th class="px-6 py-4">Ingrédient</th>
                      <th class="px-6 py-4">Libellé</th>
                      <th class="px-6 py-4 text-right">PU</th>
                      <th class="px-6 py-4 text-right">Quantité</th>
                      <th class="px-6 py-4 text-right">Montant</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-50">
                    <tr v-for="c in charges" :key="c.id" class="hover:bg-slate-50 transition-colors">
                      <td class="px-6 py-4 text-sm text-slate-600">{{ formatDate(c.daty) }}</td>
                      <td class="px-6 py-4 font-bold text-slate-800">{{ c.idingredients }}</td>
                      <td class="px-6 py-4 text-sm text-slate-500 italic">{{ c.libelle || '-' }}</td>
                      <td class="px-6 py-4 text-right text-slate-700 font-medium">{{ formatCurrency(c.pu) }}</td>
                      <td class="px-6 py-4 text-right text-slate-700 font-medium">{{ c.qte }}</td>
                      <td class="px-6 py-4 text-right font-black text-slate-900">{{ formatCurrency(c.montant) }}</td>
                    </tr>
                    <tr v-if="charges.length > 0" class="bg-indigo-50/30 font-bold border-t-2 border-indigo-100">
                      <td colspan="5" class="px-6 py-4 text-indigo-600 uppercase text-[10px] tracking-widest text-right">Total Charges rattachées</td>
                      <td class="px-6 py-4 text-right text-indigo-700 font-black">{{ formatCurrency(totalCharges) }}</td>
                    </tr>
                    <tr v-if="charges.length === 0">
                      <td colspan="6" class="px-6 py-12 text-center text-slate-400 italic">Aucune charge enregistrée pour cette fabrication</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
