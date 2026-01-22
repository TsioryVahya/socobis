<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const fabrications = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const total = ref(0)
const showAdvanced = ref(false)

// Filtres de recherche
const filters = ref({
  id: '',
  lancePar: '',
  cible: '',
  remarque: '',
  libelle: '',
  datyMin: '',
  datyMax: '',
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
  <div class="space-y-6">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Liste des Fabrications</h1>
        <p class="text-slate-500 text-sm mt-1">Gérez et suivez l'état de vos opérations de fabrication.</p>
      </div>
      <router-link to="/fabrications/nouveau" class="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-xl font-semibold shadow-lg shadow-indigo-600/20 hover:bg-indigo-700 transition-all duration-200">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Nouvelle Fabrication
      </router-link>
    </div>

    <!-- Filtres -->
    <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-200">
      <div class="flex flex-col gap-6">
        <!-- Filtres de base -->
        <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">ID Fabrication</label>
            <input v-model="filters.id" type="text" placeholder="Ex: FAB0001" class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Date Début</label>
            <input v-model="filters.datyMin" type="date" class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Date Fin</label>
            <input v-model="filters.datyMax" type="date" class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div class="flex items-end gap-2">
            <button @click="fetchFabrications" class="flex-1 px-4 py-2 bg-indigo-600 text-white rounded-xl font-semibold hover:bg-indigo-700 transition-all shadow-md shadow-indigo-600/20">
              Rechercher
            </button>
            <button @click="showAdvanced = !showAdvanced" class="p-2 bg-slate-100 text-slate-600 rounded-xl hover:bg-slate-200 transition-all" title="Plus de filtres">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 transform transition-transform duration-200" :class="{'rotate-180': showAdvanced}" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Filtres Avancés -->
        <div v-if="showAdvanced" class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4 pt-4 border-t border-slate-100 animate-fadeIn">
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Lancé Par</label>
            <input v-model="filters.lancePar" type="text" placeholder="Utilisateur..." class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Cible</label>
            <input v-model="filters.cible" type="text" placeholder="Machine / Atelier..." class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Libellé</label>
            <input v-model="filters.libelle" type="text" placeholder="Mots clés..." class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Remarque</label>
            <input v-model="filters.remarque" type="text" placeholder="Contenu remarque..." class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">ID OF</label>
            <input v-model="filters.idOf" type="text" placeholder="OF Principal..." class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">ID OF Fille</label>
            <input v-model="filters.idOffille" type="text" placeholder="Sous-OF..." class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Vue / État</label>
            <select v-model="filters.etatTable" class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
              <option value="FABRICATIONCPL">Toutes les fabrications</option>
              <option value="FABRICATIONCPLVALIDE">Validées</option>
              <option value="FABRICATIONCPLNONVALIDE">Non Validées</option>
              <option value="FABRICATIONCPLANNULE">Annulées</option>
            </select>
          </div>
          <div class="flex items-end">
            <button @click="Object.keys(filters).forEach(k => k !== 'etatTable' ? filters[k] = '' : null)" class="w-full px-4 py-2 bg-slate-200 text-slate-700 rounded-xl font-semibold hover:bg-slate-300 transition-all">
              Réinitialiser
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-200">
              <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">ID / Date</th>
              <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Libellé / Remarque</th>
              <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Lancé / Cible</th>
              <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">État</th>
              <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-if="loading" class="animate-pulse">
              <td colspan="5" class="px-6 py-10 text-center text-slate-400">Chargement...</td>
            </tr>
            <tr v-else-if="fabrications.length === 0" class="text-center">
              <td colspan="5" class="px-6 py-10 text-slate-400">Aucune fabrication trouvée</td>
            </tr>
            <tr v-for="f in fabrications" :key="f.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-6 py-4">
                <div class="text-sm font-bold text-slate-800">{{ f.id }}</div>
                <div class="text-xs text-slate-500">{{ f.daty }}</div>
              </td>
              <td class="px-6 py-4">
                <div class="text-sm font-medium text-slate-800">{{ f.libelle }}</div>
                <div class="text-xs text-slate-500 truncate max-w-xs">{{ f.remarque }}</div>
              </td>
              <td class="px-6 py-4">
                <div class="text-sm text-slate-700"><span class="font-medium">Par:</span> {{ f.lancePar }}</div>
                <div class="text-sm text-slate-700"><span class="font-medium">Cible:</span> {{ f.cible }}</div>
              </td>
              <td class="px-6 py-4">
                <span class="px-3 py-1 rounded-full text-xs font-bold uppercase tracking-tighter" :class="{
                        'bg-blue-100 text-blue-800': f.etat == 1,
                        'bg-green-100 text-green-800': f.etat == 10 || f.etat == 11
                      }">
                  {{ getStatusLabel(f.etatLib) }}
                </span>
              </td>
              <td class="px-6 py-4 text-right">
                <button @click="router.push(`/fabrications/${f.id}`)" class="p-2 text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
