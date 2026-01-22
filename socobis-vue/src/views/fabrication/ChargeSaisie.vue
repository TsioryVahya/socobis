<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()
const idFabrication = route.params.id as string

const loading = ref(true)
const submitting = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const typesCharge = ref<any[]>([])
const ingredients = ref<any[]>([])

// Initialiser 10 lignes comme dans la JSP
const rows = ref(Array.from({ length: 10 }, () => ({
  daty: format(new Date(), 'yyyy-MM-dd'),
  libelle: '',
  idIngredients: '',
  pu: 0,
  qte: 1,
  type: '1', // Valeur par défaut dans la JSP
  idFabrication: idFabrication,
  searchQuery: '', // Champ pour la recherche
  searchResults: [] as any[] // Résultats de recherche pour cette ligne
})))

const fetchInitialData = async () => {
  try {
    const [typesResp] = await Promise.all([
      axios.get('/ChargeServlet', { params: { action: 'getTypes' } })
    ])

    if (typesResp.data.status === 'success') {
      typesCharge.value = typesResp.data.data
    }
  } catch (err) {
    console.error("Erreur chargement données initiales:", err)
    error.value = "Impossible de charger les données (types de charges)"
  }
}

const onSearchIngredients = async (index: number) => {
  const row = rows.value[index]
  if (!row.searchQuery || row.searchQuery.length < 1) {
    row.searchResults = []
    return
  }

  try {
    const resp = await axios.get('/IngredientsServlet', { 
      params: { q: row.searchQuery } 
    })
    // L'API renvoie maintenant une liste filtrée
    row.searchResults = resp.data || []
  } catch (err) {
    console.error("Erreur recherche ingrédients:", err)
  }
}

const selectIngredient = async (index: number, ing: any) => {
  const row = rows.value[index]
  row.idIngredients = ing.id
  row.searchQuery = `${ing.id} - ${ing.libelle}`
  row.searchResults = []
  
  // Appeler onIngredientChange pour avoir le PU
  await onIngredientChange(index)
}

const onIngredientChange = async (index: number) => {
  const row = rows.value[index]
  if (!row.idIngredients) {
    row.pu = 0
    return
  }

  try {
    const resp = await axios.get('/IngredientServlet', { 
      params: { action: 'getDetail', id: row.idIngredients } 
    })
    
    if (resp.data.status === 'success' && resp.data.data) {
      row.pu = resp.data.data.pu || 0
    }
  } catch (err) {
    console.error("Erreur chargement détail ingrédient:", err)
  }
}

const handleSubmit = async () => {
  submitting.value = true
  error.value = null
  success.value = null

  // Filtrer les lignes vides (sans ingrédient)
  const dataToSubmit = rows.value.filter(r => r.idIngredients)

  if (dataToSubmit.length === 0) {
    error.value = "Veuillez saisir au moins une ligne complète (Ingrédient obligatoire)"
    submitting.value = false
    return
  }

  try {
    const response = await axios.post('/ChargeServlet', {
      idFabrication: idFabrication,
      charges: dataToSubmit
    })

    if (response.data.status === 'success') {
      success.value = response.data.message
      // Retour après 2 secondes
      setTimeout(() => {
        router.push({ name: 'FabricationDetail', params: { id: idFabrication } })
      }, 2000)
    } else {
      error.value = response.data.message || "Erreur lors de l'enregistrement"
    }
  } catch (err: any) {
    error.value = "Erreur réseau lors de l'enregistrement"
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await fetchInitialData()
  loading.value = false
})
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center">
        <button @click="router.back()" class="p-2 mr-4 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <div>
          <h1 class="text-2xl font-bold text-slate-800">Saisie des charges multiples</h1>
          <p class="text-slate-500 text-sm mt-1">Fabrication: {{ idFabrication }}</p>
        </div>
      </div>
    </div>

    <!-- Messages -->
    <div v-if="error" class="bg-red-50 border border-red-100 p-4 rounded-2xl text-red-700 flex items-center animate-shake">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
      </svg>
      {{ error }}
    </div>

    <div v-if="success" class="bg-emerald-50 border border-emerald-100 p-4 rounded-2xl text-emerald-700 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
      </svg>
      {{ success }}
    </div>

    <!-- Main Form -->
    <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
      <div v-if="loading" class="py-20 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
        <p class="text-slate-400">Chargement des référentiels...</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50/50 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-widest">
              <th class="px-4 py-4 w-40">Date</th>
              <th class="px-4 py-4">Description</th>
              <th class="px-4 py-4 w-60">Ingrédient</th>
              <th class="px-4 py-4 w-32 text-right">PU</th>
              <th class="px-4 py-4 w-32 text-right">Quantité</th>
              <th class="px-4 py-4 w-48">Type</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row, index) in rows" :key="index" class="hover:bg-indigo-50/30 transition-colors">
              <td class="px-2 py-3">
                <input 
                  type="date" 
                  v-model="row.daty" 
                  class="w-full bg-slate-100/50 border-none rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 transition-all font-medium"
                />
              </td>
              <td class="px-2 py-3">
                <input 
                  type="text" 
                  v-model="row.libelle" 
                  placeholder="Détail de la charge..."
                  class="w-full bg-slate-100/50 border-none rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 transition-all"
                />
              </td>
              <td class="px-2 py-3 relative">
                <div class="flex gap-1">
                  <input 
                    type="text" 
                    v-model="row.searchQuery" 
                    placeholder="Chercher..."
                    @keyup.enter="onSearchIngredients(index)"
                    class="flex-1 bg-slate-100/50 border-none rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 transition-all"
                  />
                  <button 
                    @click="onSearchIngredients(index)"
                    class="p-2 bg-indigo-50 text-indigo-600 rounded-lg hover:bg-indigo-100 transition-colors"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                  </button>
                </div>
                
                <!-- Dropdown results -->
                <div v-if="row.searchResults && row.searchResults.length > 0" class="absolute z-50 left-0 right-0 top-full mt-1 bg-white rounded-xl shadow-xl border border-slate-100 max-h-48 overflow-y-auto">
                  <div 
                    v-for="ing in row.searchResults" 
                    :key="ing.id"
                    @click="selectIngredient(index, ing)"
                    class="px-4 py-2 hover:bg-indigo-50 cursor-pointer text-sm border-b border-slate-50 last:border-none"
                  >
                    <div class="font-bold text-slate-800">{{ ing.id }}</div>
                    <div class="text-xs text-slate-500">{{ ing.libelle }}</div>
                  </div>
                </div>
              </td>
              <td class="px-2 py-3">
                <input 
                  type="number" 
                  v-model.number="row.pu" 
                  step="0.01"
                  class="w-full bg-slate-100/50 border-none rounded-lg px-3 py-2 text-sm text-right font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500 transition-all"
                />
              </td>
              <td class="px-2 py-3">
                <input 
                  type="number" 
                  v-model.number="row.qte" 
                  step="0.01"
                  class="w-full bg-slate-100/50 border-none rounded-lg px-3 py-2 text-sm text-right font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500 transition-all"
                />
              </td>
              <td class="px-2 py-3">
                <select 
                  v-model="row.type" 
                  class="w-full bg-slate-100/50 border-none rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 transition-all"
                >
                  <option v-for="t in typesCharge" :key="t.id" :value="t.id">
                    {{ t.val }}
                  </option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Footer Actions -->
      <div class="p-6 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
        <p class="text-xs text-slate-400 font-medium italic">
          * Ne saisir que les lignes nécessaires. L'ingrédient est obligatoire pour valider une ligne.
        </p>
        <div class="flex gap-4">
          <button 
            @click="router.back()" 
            class="px-6 py-3 rounded-2xl text-sm font-bold text-slate-500 hover:bg-slate-100 transition-all"
          >
            Annuler
          </button>
          <button 
            @click="handleSubmit" 
            :disabled="submitting"
            class="px-8 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl text-sm font-bold shadow-lg shadow-indigo-600/20 transition-all flex items-center disabled:opacity-50"
          >
            <svg v-if="submitting" class="animate-spin h-4 w-4 mr-2" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Enregistrer les charges
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}
.animate-shake {
  animation: shake 0.5s ease-in-out;
}
</style>
