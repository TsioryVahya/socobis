<script setup lang="ts">
import { ref, watch } from 'vue'
import axios from 'axios'
import { format } from 'date-fns'

const props = defineProps({
  mouvementId: {
    type: String,
    required: true
  },
  visible: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['close', 'movement-validated'])

const loading = ref(false)
const error = ref<string | null>(null)
const mouvement = ref<any | null>(null)
const rawResponse = ref<any | null>(null)
const parsedPayload = ref<any | null>(null)

const getStatusLabel = (status: any) => {
  if (status === 1 || status === '1') return 'CRÉÉ'
  if (status === 10 || status === '10') return 'VALIDÉ'
  if (status === 20 || status === '20') return 'TERMINÉ'
  return status || 'INCONNU'
}

const formatCurrency = (value: number) => {
  if (typeof value !== 'number') return '-'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MGA' }).format(value)
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  try {
    return format(new Date(dateString), 'dd/MM/yyyy')
  } catch (e) {
    return dateString
  }
}

const fetchMouvementDetail = async () => {
  if (!props.mouvementId) return
  loading.value = true
  error.value = null
  mouvement.value = null
  rawResponse.value = null
  parsedPayload.value = null

  try {
    const response = await axios.get('/MvtStockServlet', {
      params: { action: 'getDetail', id: props.mouvementId }
    })

    console.log('Raw response from /MvtStockServlet?action=getDetail:', response.data)
    rawResponse.value = response.data

    let payload: any = response.data
    if (typeof response.data === 'string') {
      try {
        payload = JSON.parse(response.data)
      } catch (e) {
        const raw = response.data as string,
          start = raw.indexOf('{'),
          end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) {
          payload = JSON.parse(raw.substring(start, end + 1))
        } else {
          throw e
        }
      }
    }

    if (payload.status === 'success') {
      if (payload.data) {
        mouvement.value = payload.data
      } else {
        error.value = 'Aucune donnée retournée pour ce mouvement.'
      }
    } else {
      error.value = payload?.message || 'Réponse inattendue du serveur.'
    }

    parsedPayload.value = payload
  } catch (err: any) {
    error.value = 'Erreur technique lors du chargement du mouvement.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleViser = async () => {
  if (!mouvement.value) return
  loading.value = true
  error.value = null

  try {
    const response = await axios.get('/MvtStockServlet', {
      params: { action: 'viserMouvement', id: props.mouvementId }
    })

    let payload: any = response.data
    if (typeof response.data === 'string') {
      try {
        payload = JSON.parse(response.data)
      } catch (e) {
        const raw = response.data as string,
          start = raw.indexOf('{'),
          end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) {
          payload = JSON.parse(raw.substring(start, end + 1))
        } else {
          throw e
        }
      }
    }

    if (payload.status === 'success') {
      emit('movement-validated')
      emit('close')
    } else {
      error.value = payload.message || 'Erreur lors de la validation.'
    }
  } catch (err: any) {
    error.value = 'Erreur technique lors de la validation.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      fetchMouvementDetail()
    }
  }
)
</script>

<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50 flex items-center justify-center"
  >
    <div class="relative mx-auto p-5 border w-full max-w-3xl shadow-lg rounded-md bg-white">
      <div class="mt-3 text-center">
        <h3 class="text-lg leading-6 font-medium text-gray-900">Fiche du mouvement de stock</h3>
        <div class="mt-2 px-7 py-3">
          <!-- DEBUG -->
          <div class="my-4 p-2 bg-gray-100 border border-gray-300 text-xs text-left">
            <p><strong>Debug Info:</strong></p>
            <p>Loading: {{ loading }}</p>
            <p>Error: {{ error }}</p>
            <p>Raw Response: <pre>{{ JSON.stringify(rawResponse, null, 2) }}</pre></p>
            <p>Parsed Payload: <pre>{{ JSON.stringify(parsedPayload, null, 2) }}</pre></p>
            <p>Mouvement Data: <pre>{{ JSON.stringify(mouvement, null, 2) }}</pre></p>
          </div>
          <!-- FIN DEBUG -->

          <div v-if="loading" class="text-center py-10 text-gray-500">Chargement...</div>
          <div v-else-if="error" class="text-red-500">{{ error }}</div>
          <div v-else-if="mouvement" class="text-left text-sm">
            <div class="grid grid-cols-2 gap-x-4 gap-y-2 mb-4">
              <div><strong>ID:</strong> {{ mouvement.id }}</div>
              <div><strong>Date:</strong> {{ formatDate(mouvement.daty) }}</div>
              <div class="col-span-2"><strong>Désignation:</strong> {{ mouvement.designation }}</div>
              <div><strong>Magasin:</strong> {{ mouvement.libelleMagasin }}</div>
              <div><strong>Type:</strong> {{ mouvement.libelleTypeMvtStock }}</div>
              <div><strong>Fabrication Associée:</strong> {{ mouvement.idobjet }}</div>
              <div>
                <strong>État:</strong>
                <span class="font-semibold">{{ getStatusLabel(mouvement.etat) }}</span>
              </div>
            </div>

            <div class="mt-4">
              <h4 class="font-semibold mb-2">Détails du mouvement</h4>
              <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
                <table class="min-w-full divide-y divide-gray-200 text-xs">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-2 py-2 text-left font-medium text-gray-500">Ingrédient</th>
                      <th class="px-2 py-2 text-left font-medium text-gray-500">Entrée</th>
                      <th class="px-2 py-2 text-left font-medium text-gray-500">Sortie</th>
                      <th class="px-2 py-2 text-left font-medium text-gray-500">PU</th>
                      <th class="px-2 py-2 text-left font-medium text-gray-500">Montant</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-100">
                    <tr v-for="fille in mouvement.filles" :key="fille.id">
                      <td class="px-2 py-2">{{ fille.designation || fille.idProduit }}</td>
                      <td class="px-2 py-2 text-right">{{ fille.entree }}</td>
                      <td class="px-2 py-2 text-right">{{ fille.sortie }}</td>
                      <td class="px-2 py-2 text-right">{{ formatCurrency(fille.pu) }}</td>
                      <td class="px-2 py-2 text-right">{{ formatCurrency((fille.entree + fille.sortie) * fille.pu) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="text-right font-bold mt-2">Montant Total : {{ formatCurrency(mouvement.montant) }}</div>
            </div>
          </div>
        </div>
        <div class="items-center px-4 py-3">
          <button
            v-if="mouvement && (mouvement.etat === 1 || mouvement.etat === '1')"
            @click="handleViser"
            class="px-4 py-2 bg-green-500 text-white text-base font-medium rounded-md w-full shadow-sm hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-300"
          >
            Viser le mouvement
          </button>
          <button
            @click="emit('close')"
            class="mt-2 px-4 py-2 bg-gray-200 text-gray-800 text-base font-medium rounded-md w-full shadow-sm hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-300"
          >
            Fermer
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
