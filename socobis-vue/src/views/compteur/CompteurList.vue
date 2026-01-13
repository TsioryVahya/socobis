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
  <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
    <div class="px-4 py-6 sm:px-0">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-semibold text-gray-900">Liste des Compteurs</h1>
        <router-link to="/compteurs/nouveau" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700">
          Nouveau Compteur
        </router-link>
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
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Machine</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Heure</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Origine</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="c in compteurs" :key="c.id">
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ c.daty }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ c.idMachine }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">{{ c.nombre }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ c.heure }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ c.idOrigine }}</td>
                  </tr>
                  <tr v-if="compteurs.length === 0">
                    <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500">Aucun compteur trouvé</td>
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
