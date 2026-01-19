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
  <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
    <div class="px-4 py-6 sm:px-0">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-semibold text-gray-900">Liste des Fabrications</h1>
        <router-link to="/fabrications/nouveau" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700">
          Nouvelle Fabrication
        </router-link>
      </div>

      <!-- Filtres de recherche -->
      <div class="bg-white shadow sm:rounded-lg mb-6 p-4">
        <h2 class="text-lg font-medium text-gray-900 mb-4 border-b pb-2">Filtre de recherche</h2>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">id</label>
            <input v-model="filters.id" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">Lancée par</label>
            <input v-model="filters.lancePar" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">cible</label>
            <input v-model="filters.cible" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">remarque</label>
            <input v-model="filters.remarque" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">libelle</label>
            <input v-model="filters.libelle" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">Date min</label>
            <input v-model="filters.datyMin" type="date" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">Date max</label>
            <input v-model="filters.datyMax" type="date" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">Id Ordre de fabrication</label>
            <input v-model="filters.idOf" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">Id Ordre de fabrication fille</label>
            <input v-model="filters.idOffille" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 uppercase">État</label>
            <select v-model="filters.etatTable" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-1 px-2 text-sm focus:ring-green-500 focus:border-green-500">
              <option value="FABRICATIONCPL">Tous</option>
              <option value="FABRICATIONCPLCREE">Créée(s)</option>
              <option value="FABRICATIONCPLVISEE">Validée(s)</option>
              <option value="FABRICATIONCPLANNULE">Annulée(s)</option>
              <option value="FABRICATIONCPLENTAMEE">Entamée(s)</option>
              <option value="FABRICATIONCPLBLOQUEE">Bloquée(s)</option>
              <option value="FABRICATIONCPLBTERMINEE">Terminée(s)</option>
            </select>
          </div>
          <div class="md:col-span-4 flex justify-end space-x-2">
            <button @click="fetchFabrications" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              Rechercher
            </button>
          </div>
        </div>
      </div>

      <!-- Récapitulation -->
      <div class="bg-gray-50 border border-gray-200 rounded-md mb-6 p-3 flex justify-between items-center">
        <div class="text-sm font-medium text-gray-700">
          Récapitulation
        </div>
        <div class="flex space-x-4 text-sm">
          <span class="text-gray-600">Nombre : <span class="font-bold text-gray-900">{{ fabrications.length }}</span></span>
          <span class="text-gray-600">Total : <span class="font-bold text-gray-900">{{ total }}</span></span>
        </div>
      </div>

      <div v-if="loading" class="text-center py-10">
        <p class="text-gray-500">Chargement...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 p-4 rounded-md">
        <p class="text-red-700">{{ error }}</p>
      </div>

      <div v-else class="flex flex-col">
        <div class="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div class="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
            <div class="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">ID</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">Lancée par</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">Cible</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">Remarque</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">Désignation</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">Date</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">Id ordre de fab</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">Id Ordre fab fille</th>
                    <th scope="col" class="px-4 py-2 text-left text-xs font-bold text-gray-500 uppercase">État</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="(f, index) in fabrications" :key="f.id || index" class="hover:bg-gray-50">
                    <td class="px-4 py-2 whitespace-nowrap text-sm font-medium text-blue-600">
                      <router-link :to="`/fabrications/${f.id}`" class="hover:underline">+{{ f.id }}</router-link>
                    </td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-700">{{ f.lancePar }}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-700">{{ f.cible }}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-700">{{ f.remarque }}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-700">{{ f.libelle }}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-700">{{ f.daty }}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-700">{{ f.idOf || '' }}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-700">{{ f.idOffille || '' }}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm">
                      <span :class="{
                        'px-2 py-1 text-xs rounded-full': true,
                        'bg-blue-100 text-blue-800': f.etat == 1,
                        'bg-green-100 text-green-800': f.etat == 10 || f.etat == 11
                      }">
                        {{ getStatusLabel(f.etatLib) }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="fabrications.length === 0">
                    <td colspan="9" class="px-4 py-10 text-center text-sm text-gray-500">Aucune fabrication trouvée</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
