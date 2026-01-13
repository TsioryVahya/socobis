<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const fabrications = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const fetchFabrications = async () => {
  loading.value = true
  try {
    const response = await axios.get('/FabricationServlet?action=list')
    console.log('Réponse brute axios:', response)

    // Certains réglages axios renvoient une chaîne JSON au lieu d'un objet
    if (typeof response.data === 'string') {
      const raw = response.data as string
      try {
        // On cherche la position du mot-clé "data" puis du premier '[' qui suit
        const dataIndex = raw.indexOf('"data"')
        if (dataIndex === -1) {
          console.error('Impossible de trouver la clé "data" dans la réponse brute')
          throw new Error('Format de réponse inattendu (clé data manquante)')
        }

        const bracketStart = raw.indexOf('[', dataIndex)
        if (bracketStart === -1) {
          console.error('Impossible de trouver le début du tableau [ après "data"')
          throw new Error('Format de réponse inattendu (aucun [ trouvé)')
        }

        // On parcourt la chaîne à partir du '[' pour trouver le ']' qui referme le tableau principal
        let depth = 0
        let inString = false
        let escapeNext = false
        let endIndex = -1

        for (let i = bracketStart; i < raw.length; i++) {
          const ch = raw[i]

          if (escapeNext) {
            escapeNext = false
            continue
          }

          if (ch === '\\') {
            escapeNext = true
            continue
          }

          if (ch === '"') {
            inString = !inString
            continue
          }

          if (inString) continue

          if (ch === '[') depth++
          if (ch === ']') {
            depth--
            if (depth === 0) {
              endIndex = i
              break
            }
          }
        }

        if (endIndex === -1) {
          console.error('Impossible de trouver la fin du tableau data (crochet fermant)')
          throw new Error('Format de réponse inattendu (fin de tableau non trouvée)')
        }

        const arrayJson = raw.substring(bracketStart, endIndex + 1)
        console.log('Extrait JSON data (début):', arrayJson.slice(0, 100), '...')
        console.log('Extrait JSON data (fin):', arrayJson.slice(-200))

        const dataArray = JSON.parse(arrayJson)
        console.log('Nombre de fabrications:', Array.isArray(dataArray) ? dataArray.length : 'non tableau')
        fabrications.value = Array.isArray(dataArray) ? dataArray : []
        error.value = null
        return
      } catch (e: any) {
        console.error('Erreur lors de l\'extraction/parsing du tableau data:', e)
        error.value = 'Erreur lors du parsing des données de fabrication'
        fabrications.value = []
        return
      }
    }

    // Cas normal: axios a déjà parsé le JSON
    const payload: any = response.data
    console.log('Payload parsé (objet):', payload)
    console.log('Status:', payload.status)
    console.log('Type data:', Array.isArray(payload.data), payload.data?.length)

    if (payload.status === 'success') {
      console.log('Données reçues (data):', payload.data)
      fabrications.value = payload.data || []
      error.value = null
    } else {
      console.log('Status != success, message:', payload.message)
      error.value = payload.message
      fabrications.value = []
    }
  } catch (err: any) {
    console.error('Erreur axios:', err)
    error.value = err.response?.data?.message || 'Erreur lors de la récupération des fabrications'
    if (err.response?.status === 401) {
      error.value = 'Session expirée. Veuillez vous reconnecter.'
      localStorage.removeItem('user')
      setTimeout(() => router.push('/login'), 2000)
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchFabrications)
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
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">BC</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ordre Fab</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="(f, index) in fabrications" :key="f.id || index">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ f.id }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ f.daty }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ f.libelle }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ f.lancePar }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ f.idBc }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ f.ordreDeFab }}</td>
                  </tr>
                  <tr v-if="fabrications.length === 0">
                    <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500">Aucune fabrication trouvée</td>
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
