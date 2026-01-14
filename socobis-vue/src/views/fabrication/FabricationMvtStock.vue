<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const type = computed(() => (route.params.type as string) || '')
const idFab = computed(() => (route.params.id as string) || '')

const title = computed(() => {
  if (type.value === 'entree') return 'Mouvement de stock - Entrée'
  if (type.value === 'sortie') return 'Mouvement de stock - Sortie'
  if (type.value === 'residu') return 'Mouvement de stock - Résidu'
  return 'Mouvement de stock'
})

const loading = ref(true)
const error = ref<string | null>(null)
const saving = ref(false)
const saveMessage = ref<string | null>(null)
const mere = ref<any | null>(null)
const lignes = ref<any[]>([])
const magasins = ref<Array<{ id: string; libelle: string }>>([])

const fetchMvt = async () => {
  loading.value = true
  error.value = null
  saveMessage.value = null
  mere.value = null
  lignes.value = []

  try {
    const response = await axios.get('/MvtStockServlet', {
      params: {
        action: 'prepareFromFab',
        idFab: idFab.value,
        type: type.value || 'entree'
      }
    })

    let payload: any = response.data
    if (typeof response.data === 'string') {
      try {
        payload = JSON.parse(response.data)
      } catch (e) {
        const raw = response.data as string
        const start = raw.indexOf('{')
        const end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) {
          payload = JSON.parse(raw.substring(start, end + 1))
        } else {
          throw e
        }
      }
    }

    if (payload.status === 'success') {
      mere.value = payload.mere
      lignes.value = payload.filles || []
    } else {
      error.value = payload.message || 'Erreur lors du chargement du mouvement de stock'
    }
  } catch (err: any) {
    console.error(err)
    error.value = 'Erreur lors du chargement du mouvement de stock'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const resp = await axios.get('/MagasinPointServlet')
    magasins.value = resp.data || []
  } catch (e) {
    console.error('Erreur chargement magasins', e)
  } finally {
    await fetchMvt()
  }
})

const isEntreeType = computed(() => type.value === 'entree' || type.value === 'residu')

const handleSave = async () => {
  if (!idFab.value) {
    error.value = 'Identifiant fabrication manquant'
    return
  }

  saving.value = true
  error.value = null
  saveMessage.value = null

  try {
    const payload = {
      idFab: idFab.value,
      type: type.value || 'entree',
      mere: mere.value,
      lignes: lignes.value
    }

    const response = await axios.post('/MvtStockServlet', payload, {
      params: {
        action: 'saveFromFab'
      }
    })

    let data: any = response.data
    if (typeof data === 'string') {
      try {
        data = JSON.parse(data)
      } catch (e) {
        const raw = data as string
        const start = raw.indexOf('{')
        const end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) {
          data = JSON.parse(raw.substring(start, end + 1))
        } else {
          throw e
        }
      }
    }

    if (data.status === 'success') {
      saveMessage.value = data.message || 'Mouvement de stock enregistré avec succès.'
      // Rediriger l'utilisateur vers la page précédente après un court délai pour qu'il voie le message
      setTimeout(() => {
        router.back()
      }, 2000)
    } else {
      error.value = data.message || 'Erreur lors de l\'enregistrement du mouvement de stock'
    }
  } catch (err: any) {
    console.error(err)
    error.value = 'Erreur lors de l\'enregistrement du mouvement de stock'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="content-wrapper py-4 px-2 sm:px-4">
    <h1 class="box-title text-xl font-semibold mb-4 flex items-center gap-2">
      <button type="button" @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &#8592;
      </button>
      <span>{{ title }}</span>
    </h1>

    <div class="max-w-5xl mx-auto bg-white shadow rounded-md p-4 sm:p-6">
      <div v-if="loading" class="text-center py-8 text-gray-500 text-sm">
        Chargement du mouvement de stock...
      </div>

      <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
        {{ error }}
      </div>

      <div v-else>
        <div v-if="saveMessage" class="mb-3 bg-green-50 border border-green-200 text-green-700 px-4 py-2 rounded-md text-xs">
          {{ saveMessage }}
        </div>

        <div class="mb-4 grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-gray-700">
          <div class="flex flex-col">
            <label class="mb-0.5 font-medium">Fabrication</label>
            <input
              type="text"
              class="border border-gray-300 rounded px-2 py-1 text-xs bg-gray-50"
              :value="idFab"
              readonly
            />
          </div>

          <div class="flex flex-col" v-if="mere">
            <label class="mb-0.5 font-medium">Date</label>
            <input
              v-model="mere.daty"
              type="date"
              class="border border-gray-300 rounded px-2 py-1 text-xs"
            />
          </div>

          <div class="flex flex-col sm:col-span-2" v-if="mere">
            <label class="mb-0.5 font-medium">Désignation</label>
            <input
              v-model="mere.designation"
              type="text"
              class="border border-gray-300 rounded px-2 py-1 text-xs"
            />
          </div>

          <div class="flex flex-col" v-if="mere">
            <label class="mb-0.5 font-medium">Magasin</label>
            <select
              v-model="mere.idMagasin"
              class="border border-gray-300 rounded px-2 py-1 text-xs bg-white"
            >
              <option value="" disabled>Sélectionner un magasin</option>
              <option
                v-for="mag in magasins"
                :key="mag.id"
                :value="mag.id"
              >
                {{ mag.id }} - {{ mag.libelle }}
              </option>
            </select>
          </div>

          <div class="flex flex-col" v-if="mere">
            <label class="mb-0.5 font-medium">Type de mouvement</label>
            <input
              type="text"
              class="border border-gray-300 rounded px-2 py-1 text-xs bg-gray-50 uppercase"
              :value="type"
              readonly
            />
          </div>
        </div>

        <div class="mt-4">
          <h2 class="text-sm font-semibold text-gray-800 mb-2">Détails mouvement de stocks</h2>
          <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
            <table class="min-w-full divide-y divide-gray-200 text-xs">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-3 py-2 text-left font-medium text-gray-500">Produit</th>
                  <th class="px-3 py-2 text-left font-medium text-gray-500">Désignation</th>
                  <th class="px-3 py-2 text-right font-medium text-gray-500">Entrée</th>
                  <th class="px-3 py-2 text-right font-medium text-gray-500">Sortie</th>
                  <th class="px-3 py-2 text-right font-medium text-gray-500">Prix unitaire</th>
                  <th class="px-3 py-2 text-left font-medium text-gray-500">Mouvement source</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-100">
                <tr v-for="(l, idx) in lignes" :key="l.idProduit || idx" class="hover:bg-gray-50">
                  <td class="px-3 py-2 whitespace-nowrap">{{ l.idProduit }}</td>
                  <td class="px-3 py-2 whitespace-nowrap">{{ l.designation }}</td>
                  <td class="px-3 py-2 whitespace-nowrap text-right">
                    <input
                      v-model.number="l.entree"
                      type="number"
                      step="0.01"
                      class="w-20 border border-gray-300 rounded px-1 py-0.5 text-right text-xs"
                      :readonly="!isEntreeType"
                    />
                  </td>
                  <td class="px-3 py-2 whitespace-nowrap text-right">
                    <input
                      v-model.number="l.sortie"
                      type="number"
                      step="0.01"
                      class="w-20 border border-gray-300 rounded px-1 py-0.5 text-right text-xs"
                      :readonly="isEntreeType"
                    />
                  </td>
                  <td class="px-3 py-2 whitespace-nowrap text-right">
                    <input
                      v-model.number="l.pu"
                      type="number"
                      step="0.01"
                      class="w-24 border border-gray-300 rounded px-1 py-0.5 text-right text-xs"
                    />
                  </td>
                  <td class="px-3 py-2 whitespace-nowrap">{{ l.mvtSrc }}</td>
                </tr>
                <tr v-if="lignes.length === 0">
                  <td colspan="6" class="px-3 py-4 text-center text-[11px] text-gray-500 italic">
                    Aucun détail de mouvement de stock trouvé.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="mt-4 flex justify-end">
            <button
              type="button"
              class="inline-flex items-center px-4 py-1.5 border border-transparent text-xs font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-60"
              :disabled="saving || lignes.length === 0"
              @click="handleSave"
            >
              <span v-if="saving">Enregistrement...</span>
              <span v-else>Enregistrer</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
