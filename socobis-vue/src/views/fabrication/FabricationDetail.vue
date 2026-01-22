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
  <div class="space-y-8 max-w-7xl mx-auto">
    <!-- Header with Back Button - Redesign -->
    <div class="bg-gradient-to-r from-teal-500 via-emerald-500 to-green-500 rounded-2xl p-8 shadow-2xl">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        <div class="flex items-center">
          <button @click="router.back()" class="p-3 mr-4 text-white/80 hover:text-white hover:bg-white/20 rounded-xl transition-all">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <div>
            <h1 class="text-3xl font-black text-white drop-shadow-lg">Fiche Fabrication</h1>
            <p class="text-emerald-100 font-semibold text-lg mt-1" v-if="fabrication">{{ fabrication.id }}</p>
          </div>
        </div>
        
        <div class="flex items-center gap-4" v-if="fabrication">
          <span class="px-6 py-3 rounded-2xl text-sm font-black uppercase tracking-wider shadow-xl" :class="{
            'bg-amber-400 text-amber-900': fabrication.etat == 1,
            'bg-white text-emerald-600': fabrication.etat == 10 || fabrication.etat == 11
          }">
            {{ getStatusLabel(fabrication.etat) }}
          </span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex flex-col items-center justify-center py-24 bg-white rounded-2xl shadow-xl border-2 border-emerald-100">
      <div class="animate-spin rounded-full h-16 w-16 border-4 border-emerald-200 border-t-emerald-600 mb-6"></div>
      <p class="text-slate-600 font-bold text-lg">Chargement des données...</p>
    </div>

    <div v-else-if="error" class="bg-red-50 border-2 border-red-200 p-8 rounded-2xl shadow-lg">
      <div class="flex items-center text-red-700">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 mr-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <p class="font-black text-xl">Erreur</p>
      </div>
      <p class="text-red-600 mt-3 ml-12 font-semibold">{{ error }}</p>
    </div>

    <div v-else-if="fabrication" class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <!-- Info Sidebar -->
      <div class="lg:col-span-1 space-y-6">
        <div class="bg-gradient-to-br from-white to-teal-50 p-8 rounded-2xl shadow-lg border-2 border-teal-100">
          <h3 class="text-xs font-black text-teal-600 uppercase tracking-widest mb-6 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Informations
          </h3>
          
          <div class="space-y-5">
            <div class="bg-white p-4 rounded-xl shadow-sm border border-teal-100">
              <label class="block text-[10px] font-black text-teal-600 uppercase mb-2 tracking-wider">Désignation</label>
              <p class="text-slate-900 font-bold text-lg leading-tight">{{ fabrication.libelle || 'Sans libellé' }}</p>
            </div>
            <div class="bg-white p-4 rounded-xl shadow-sm border border-teal-100">
              <label class="block text-[10px] font-black text-teal-600 uppercase mb-2 tracking-wider">Date</label>
              <p class="text-slate-800 font-bold">{{ formatDate(fabrication.daty) }}</p>
            </div>
            <div class="bg-white p-4 rounded-xl shadow-sm border border-teal-100">
              <label class="block text-[10px] font-black text-teal-600 uppercase mb-2 tracking-wider">Magasin Cible</label>
              <p class="text-slate-800 font-bold">{{ fabrication.cible || '-' }}</p>
            </div>
            <div class="bg-white p-4 rounded-xl shadow-sm border border-teal-100">
              <label class="block text-[10px] font-black text-teal-600 uppercase mb-2 tracking-wider">Remarque</label>
              <p class="text-slate-600 text-sm italic">{{ fabrication.remarque || 'Aucune remarque' }}</p>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="bg-gradient-to-br from-teal-600 to-emerald-700 p-6 rounded-2xl shadow-2xl text-white space-y-4">
          <h3 class="text-xs font-black text-teal-100 uppercase tracking-widest mb-4 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            Actions Rapides
          </h3>
          
          <button 
            v-if="fabrication.etat === 1 || fabrication.etat === '1'"
            @click="handleValidate"
            class="w-full py-4 bg-white text-emerald-700 hover:bg-emerald-50 rounded-xl font-black transition-all shadow-lg transform hover:scale-105 flex items-center justify-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
            </svg>
            Valider la Fabrication
          </button>

          <div class="grid grid-cols-1 gap-3">
            <button @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'residu' } })" class="w-full py-3 bg-amber-500 hover:bg-amber-600 text-white rounded-xl text-sm font-bold transition-all shadow-md transform hover:scale-105 text-left px-4 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Résidu
            </button>
            <button 
              v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
              @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'entree' } })" 
              class="w-full py-3 bg-green-500 hover:bg-green-600 text-white rounded-xl text-sm font-bold transition-all shadow-md transform hover:scale-105 text-left px-4 flex items-center"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
              </svg>
              Mouvement entrée
            </button>
            <button 
              v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
              @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'sortie' } })" 
              class="w-full py-3 bg-red-500 hover:bg-red-600 text-white rounded-xl text-sm font-bold transition-all shadow-md transform hover:scale-105 text-left px-4 flex items-center"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
              </svg>
              Mouvement sortie
            </button>
            <button 
              @click="router.push({ name: 'ChargeSaisie', params: { id: fabrication.id } })" 
              class="w-full py-3 bg-blue-500 hover:bg-blue-600 text-white rounded-xl text-sm font-bold transition-all shadow-md transform hover:scale-105 text-left px-4 flex items-center"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Saisie des charges
            </button>
          </div>
        </div>
      </div>

      <!-- Content Area -->
      <div class="lg:col-span-3 space-y-8">
        <!-- Résumé Financier -->
        <div v-if="fabrication" class="bg-gradient-to-br from-emerald-500 via-teal-500 to-cyan-600 rounded-2xl p-10 text-white shadow-2xl">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-end gap-8">
            <div class="flex-1">
              <p class="text-emerald-100 text-xs font-black uppercase tracking-widest mb-3 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Prix de Revient Total
              </p>
              <h2 class="text-5xl font-black drop-shadow-lg">{{ formatCurrency(totalPrixRevient) }}</h2>
            </div>
            <div class="grid grid-cols-2 gap-6">
              <div class="bg-white/10 backdrop-blur-sm rounded-xl p-5 text-center border border-white/20">
                <p class="text-emerald-100 text-[10px] font-black uppercase tracking-widest mb-2">Budgets Fab</p>
                <p class="text-2xl font-black">{{ formatCurrency(totalBudgetsFab) }}</p>
              </div>
              <div class="bg-white/10 backdrop-blur-sm rounded-xl p-5 text-center border border-white/20">
                <p class="text-emerald-100 text-[10px] font-black uppercase tracking-widest mb-2">Charges</p>
                <p class="text-2xl font-black">{{ formatCurrency(totalCharges) }}</p>
              </div>
            </div>
          </div>
          <div class="mt-6 pt-6 border-t border-white/20 flex items-start text-xs text-emerald-50">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3 flex-shrink-0 opacity-70" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span class="leading-relaxed">Le prix de revient est calculé en additionnant les charges rattachées et les mouvements de sortie de stock.</span>
          </div>
        </div>

        <!-- Tabs -->
        <div class="bg-white rounded-2xl shadow-xl border-2 border-teal-100 overflow-hidden">
          <div class="flex border-b-2 border-teal-100 bg-gradient-to-r from-teal-50 to-emerald-50 p-3 gap-2">
            <button 
              v-for="tab in tabs" 
              :key="tab.id"
              @click="selectTab(tab.id)"
              class="px-8 py-4 rounded-xl text-sm font-black uppercase tracking-wide transition-all transform"
              :class="activeTab === tab.id ? 'bg-gradient-to-r from-teal-500 to-emerald-500 text-white shadow-lg scale-105' : 'text-slate-600 hover:bg-white hover:text-teal-600 hover:shadow-md'"
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
                    <tr class="text-xs font-black text-teal-700 uppercase tracking-wider bg-teal-50 border-b-2 border-teal-200">
                      <th class="px-6 py-5">Composant</th>
                      <th class="px-6 py-5 text-right">Quantité</th>
                      <th class="px-6 py-5">Unité</th>
                      <th class="px-6 py-5 text-right">Montant</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-teal-50">
                    <tr v-for="d in details" :key="d.id" class="hover:bg-teal-50/50 transition-colors group">
                      <td class="px-6 py-4">
                        <router-link :to="{ name: 'IngredientDetail', params: { id: d.idIngredients } }" class="text-teal-600 font-black group-hover:underline">
                          {{ d.idIngredients }}
                        </router-link>
                      </td>
                      <td class="px-6 py-4 text-right font-bold text-slate-800">{{ d.qte }}</td>
                      <td class="px-6 py-4 text-slate-600 font-semibold">{{ d.idunitelib }}</td>
                      <td class="px-6 py-4 text-right">
                        <div class="text-slate-900 font-black text-lg">{{ formatCurrency(d.montant) }}</div>
                        <div class="text-[10px] text-slate-500 font-medium">PU: {{ formatCurrency(d.pu) }}</div>
                      </td>
                    </tr>
                    <tr v-if="details.length === 0">
                      <td colspan="4" class="px-6 py-16 text-center text-slate-400 italic">Aucun détail disponible</td>
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
                    <tr class="text-xs font-black text-teal-700 uppercase tracking-wider bg-teal-50 border-b-2 border-teal-200">
                      <th class="px-6 py-5">Mouvement</th>
                      <th class="px-6 py-5">Type</th>
                      <th class="px-6 py-5 text-right">Montant</th>
                      <th class="px-6 py-5 text-center">État</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-teal-50">
                    <tr v-for="mvt in mouvements" :key="mvt.id" class="hover:bg-teal-50/50 transition-colors">
                      <td class="px-6 py-4">
                        <router-link :to="{ name: 'MvtStockDetail', params: { id: mvt.id } }" class="text-teal-600 font-black hover:underline block">
                          {{ mvt.id }}
                        </router-link>
                        <span class="text-[10px] text-slate-500 font-medium">{{ formatDate(mvt.daty) }}</span>
                      </td>
                      <td class="px-6 py-4">
                        <span class="text-sm font-bold text-slate-700">{{ mvt.typeMouvement }}</span>
                        <div class="text-[10px] text-slate-500 font-medium">{{ mvt.magasin }}</div>
                      </td>
                      <td class="px-6 py-4 text-right font-black text-slate-900 text-lg">{{ formatCurrency(mvt.montant) }}</td>
                      <td class="px-6 py-4 text-center">
                        <span class="px-3 py-2 rounded-xl text-xs font-black uppercase tracking-tight shadow-sm" :class="{
                          'bg-amber-100 text-amber-700 border border-amber-200': mvt.etat == 1,
                          'bg-emerald-100 text-emerald-700 border border-emerald-200': mvt.etat >= 10
                        }">
                          {{ getMvtStatusLabel(mvt.etat) }}
                        </span>
                      </td>
                    </tr>
                    <tr v-if="mouvements.length > 0" class="bg-teal-50 font-black border-t-2 border-teal-200">
                      <td colspan="2" class="px-6 py-5 text-teal-700 uppercase text-xs tracking-widest">Sous-total (Mouvements Sorties/Résidus)</td>
                      <td class="px-6 py-5 text-right text-teal-700 text-xl">{{ formatCurrency(totalBudgetsFab) }}</td>
                      <td></td>
                    </tr>
                    <tr v-if="mouvements.length === 0">
                      <td colspan="4" class="px-6 py-16 text-center text-slate-400 italic">Aucun mouvement enregistré</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Charges Tab -->
            <div v-if="activeTab === 'charges'" class="animate-fadeIn">
              <div class="p-8 bg-gradient-to-r from-teal-50 to-emerald-50 border-b-2 border-teal-100 flex justify-between items-center">
                <span class="text-sm font-black text-teal-700 uppercase tracking-wider">Récapitulatif des charges</span>
                <div class="text-right">
                  <span class="text-xs text-teal-600 uppercase block font-bold">Total des charges</span>
                  <span class="text-3xl font-black text-teal-600">{{ formatCurrency(totalCharges) }}</span>
                </div>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                  <thead>
                    <tr class="text-xs font-black text-teal-700 uppercase tracking-wider bg-teal-50 border-b-2 border-teal-200">
                      <th class="px-6 py-5">Date</th>
                      <th class="px-6 py-5">Ingrédient</th>
                      <th class="px-6 py-5">Libellé</th>
                      <th class="px-6 py-5 text-right">PU</th>
                      <th class="px-6 py-5 text-right">Quantité</th>
                      <th class="px-6 py-5 text-right">Montant</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-teal-50">
                    <tr v-for="c in charges" :key="c.id" class="hover:bg-teal-50/50 transition-colors">
                      <td class="px-6 py-4 text-sm text-slate-700 font-semibold">{{ formatDate(c.daty) }}</td>
                      <td class="px-6 py-4 font-black text-teal-600">{{ c.idingredients }}</td>
                      <td class="px-6 py-4 text-sm text-slate-600 italic">{{ c.libelle || '-' }}</td>
                      <td class="px-6 py-4 text-right text-slate-800 font-bold">{{ formatCurrency(c.pu) }}</td>
                      <td class="px-6 py-4 text-right text-slate-800 font-bold">{{ c.qte }}</td>
                      <td class="px-6 py-4 text-right font-black text-slate-900 text-lg">{{ formatCurrency(c.montant) }}</td>
                    </tr>
                    <tr v-if="charges.length > 0" class="bg-teal-50 font-black border-t-2 border-teal-200">
                      <td colspan="5" class="px-6 py-5 text-teal-700 uppercase text-xs tracking-widest text-right">Total Charges rattachées</td>
                      <td class="px-6 py-5 text-right text-teal-700 font-black text-xl">{{ formatCurrency(totalCharges) }}</td>
                    </tr>
                    <tr v-if="charges.length === 0">
                      <td colspan="6" class="px-6 py-16 text-center text-slate-400 italic">Aucune charge enregistrée pour cette fabrication</td>
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
