<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const ofs = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const fetchOfs = async () => {
  loading.value = true
  try {
    const response = await axios.get('/OfServlet?action=list')
    if (response.data.status === 'success') {
      ofs.value = response.data.data
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
        <div class="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div class="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
            <div class="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Libellé</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Lancé par</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cible</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="of in ofs" :key="of.id">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ of.id }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.daty }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ of.libelle }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.lancePar }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ of.cible }}</td>
                  </tr>
                  <tr v-if="ofs.length === 0">
                    <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500">Aucun OF trouvé</td>
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
