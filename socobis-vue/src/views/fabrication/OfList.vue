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
      displayedOfs.value = response.data.data
      // Par défaut, on affiche tous les OF sans appliquer les filtres
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
  <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
    <div class="px-4 py-6 sm:px-0">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-semibold text-gray-900">Liste des Ordres de Fabrication</h1>
        <router-link to="/ofs/nouveau" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700">
          Nouvel OF
        </router-link>
      </div>

      <div v-if="loading" class="text-center py-10">
        <p class="text-gray-500">Chargement...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 p-4 rounded-md">
        <p class="text-red-700">{{ error }}</p>
      </div>

      <div v-else class="flex flex-col">
        <!-- Filtres de recherche -->
        <div class="mb-4 p-4 bg-gray-50 border border-gray-200 rounded-md">
          <h2 class="text-sm font-semibold text-gray-700 mb-3">Filtre de recherche</h2>
          <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4 text-sm">
            <div>
              <label class="block text-gray-700">ID</label>
              <input v-model="filters.id" type="text" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div>
              <label class="block text-gray-700">Lancé par</label>
              <input v-model="filters.lancePar" type="text" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div>
              <label class="block text-gray-700">Cible</label>
              <input v-model="filters.cible" type="text" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div>
              <label class="block text-gray-700">Remarque</label>
              <input v-model="filters.remarque" type="text" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div class="md:col-span-2">
              <label class="block text-gray-700">Libellé</label>
              <input v-model="filters.libelle" type="text" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div>
              <label class="block text-gray-700">Date de besoin min</label>
              <input v-model="filters.dateBesoinMin" type="date" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div>
              <label class="block text-gray-700">Date de besoin max</label>
              <input v-model="filters.dateBesoinMax" type="date" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div>
              <label class="block text-gray-700">Date min</label>
              <input v-model="filters.dateMin" type="date" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
            <div>
              <label class="block text-gray-700">Date max</label>
              <input v-model="filters.dateMax" type="date" class="mt-1 block w-full rounded-md border border-gray-300 px-2 py-1" />
            </div>
          </div>
          <div class="mt-3 flex justify-end">
            <button
              type="button"
              @click="applyFilters"
              class="inline-flex items-center px-3 py-1 border border-indigo-500 text-xs font-medium rounded-md text-indigo-700 bg-white hover:bg-indigo-50"
            >
              Filtrer
            </button>
          </div>
        </div>

        <!-- Récapitulatif -->
        <div class="mb-3 text-sm text-gray-700 flex items-center gap-4">
          <span class="font-semibold">Récapitulation</span>
          <span>Nombre total : <span class="font-medium">{{ displayedOfs.length }}</span></span>
        </div>

        <div class="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div class="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
            <div class="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date de besoin</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Désignation</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Lancé par</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cible</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Remarque</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="of in displayedOfs" :key="of.id">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ of.id }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.daty }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.besoin }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ of.libelle }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.lancePar }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.cible }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.remarque }}</td>
                  </tr>
                  <tr v-if="displayedOfs.length === 0">
                    <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500">Aucun OF trouvé</td>
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
