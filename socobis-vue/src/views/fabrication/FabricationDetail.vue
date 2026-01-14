<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref<string | null>(null)
const fabrication = ref<any | null>(null)
const details = ref<any[]>([])

const getStatusLabel = (status: any) => {
  if (status === 1 || status === '1') return 'CREE'
  if (status === 11 || status === '11') return 'VALIDEE'
  if (status === 10 || status === '10') return 'VALIDEE'
  return status || 'INCONNU'
}

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
      try {
        payload = JSON.parse(resp.data)
      } catch (e) {
        const raw = resp.data as string
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
      // Recharger la fiche après validation pour mettre à jour l'état
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
  loading.value = true
  error.value = null
  fabrication.value = null
  details.value = []

  const id = route.params.id as string
  try {
    const response = await axios.get('/FabricationServlet', {
      params: { action: 'list', id }
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

    // Charger les lignes de détail (FabricationFilleCpl)
    const detailsResp = await axios.get('/FabricationServlet', {
      params: { action: 'details', id }
    })

    let detailsPayload: any = detailsResp.data
    if (typeof detailsResp.data === 'string') {
      try {
        detailsPayload = JSON.parse(detailsResp.data)
      } catch (e) {
        const raw = detailsResp.data as string
        const start = raw.indexOf('{')
        const end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) {
          detailsPayload = JSON.parse(raw.substring(start, end + 1))
        } else {
          throw e
        }
      }
    }

    if (detailsPayload.status === 'success') {
      details.value = detailsPayload.data || []
    }
  } catch (err: any) {
    console.error(err)
    error.value = "Erreur lors du chargement de la fabrication"
  } finally {
    loading.value = false
  }
}

onMounted(fetchFabrication)
</script>

<template>
  <div class="content-wrapper py-4 px-2 sm:px-4">
    <h1 class="box-title text-xl font-semibold mb-4 flex items-center gap-2">
      <button type="button" @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &#8592;
      </button>
      <span>Fiche de Fabrication</span>
    </h1>

    <div v-if="loading" class="text-center py-10 text-gray-500">
      Chargement...
    </div>

    <div v-else-if="error" class="max-w-3xl mx-auto bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
      {{ error }}
    </div>

    <div v-else-if="fabrication" class="max-w-5xl mx-auto">
      <div class="box box-fiche">
        <div class="box-body">
          <div class="bg-white p-4 sm:p-6 rounded-md shadow">
            <!-- Récapitulatif principal -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">ID</div>
                <div class="mt-1 text-sm font-medium text-gray-900">{{ fabrication.id }}</div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Remarque</div>
                <div class="mt-1 text-sm text-gray-900">{{ fabrication.remarque || '-' }}</div>
              </div>
              <div class="sm:col-span-2">
                <div class="text-xs font-semibold text-gray-500 uppercase">Désignation</div>
                <div class="mt-1 text-sm text-gray-900">{{ fabrication.libelle || '-' }}</div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Date de besoin</div>
                <div class="mt-1 text-sm text-gray-900">{{ fabrication.besoin || '-' }}</div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Date</div>
                <div class="mt-1 text-sm text-gray-900">{{ fabrication.daty || '-' }}</div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Ordre de fabrication fille associé</div>
                <div class="mt-1 text-sm text-blue-600" v-if="fabrication.idOffille">
                  {{ fabrication.idOffille }}
                </div>
                <div class="mt-1 text-sm text-gray-400" v-else>
                  -
                </div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Ordre de fabrication associé</div>
                <div class="mt-1 text-sm text-blue-600" v-if="fabrication.idOf">
                  {{ fabrication.idOf }}
                </div>
                <div class="mt-1 text-sm text-gray-400" v-else>
                  -
                </div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Lancée par</div>
                <div class="mt-1 text-sm text-gray-900">{{ fabrication.lancePar || '-' }}</div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Cible</div>
                <div class="mt-1 text-sm text-gray-900">{{ fabrication.cible || '-' }}</div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">ÉTAT</div>
                <div class="mt-1 text-sm font-medium text-gray-900">{{ getStatusLabel(fabrication.etat) }}</div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Fabrication précédente</div>
                <div class="mt-1 text-sm text-blue-600" v-if="fabrication.fabricationPrec">
                  {{ fabrication.fabricationPrec }}
                </div>
                <div class="mt-1 text-sm text-gray-400" v-else>
                  -
                </div>
              </div>
              <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Fabrication Suivant</div>
                <div class="mt-1 text-sm text-blue-600" v-if="fabrication.fabricationSuiv">
                  {{ fabrication.fabricationSuiv }}
                </div>
                <div class="mt-1 text-sm text-gray-400" v-else>
                  -
                </div>
              </div>
            </div>

            <!-- Onglets (non fonctionnels, visuels seulement) -->
            <div class="border-b border-gray-200 mb-4">
              <nav class="-mb-px flex flex-wrap space-x-4 text-sm" aria-label="Tabs">
                <span class="border-indigo-500 text-indigo-600 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Détails</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Mouvement de stock</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Charges rattachées</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Rapprochement</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Historique</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Rapprochement Globale</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Déchets</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Résidu</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Attribution</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Heure Supplémentaire</span>
                <span class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-3 border-b-2 font-medium">Charge personnelle</span>
              </nav>
            </div>

            <!-- Tableau Détails (structure comme ERP, sans données pour l'instant) -->
            <div class="mt-4">
              <h2 class="text-sm font-semibold text-gray-800 mb-2">Détails</h2>
              <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
                <table class="min-w-full divide-y divide-gray-200 text-xs">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">id</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">ID Ingrédient</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Composants</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Désignation</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Date de besoin</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Prix unitaire</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Quantité</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Montant</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Unité</th>
                      <th class="px-3 py-2 text-left font-medium text-gray-500">Machine</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-100">
                    <tr v-for="(d, idx) in details" :key="d.id || idx" class="hover:bg-gray-50">
                      <td class="px-3 py-2 whitespace-nowrap">{{ d.id }}</td>
                      <td class="px-3 py-2 whitespace-nowrap">{{ d.idIngredients }}</td>
                      <td class="px-3 py-2 whitespace-nowrap">{{ d.idingredientsLib }}</td>
                      <td class="px-3 py-2 whitespace-nowrap">{{ d.libelle }}</td>
                      <td class="px-3 py-2 whitespace-nowrap">{{ d.datybesoin }}</td>
                      <td class="px-3 py-2 whitespace-nowrap text-right">{{ d.pu }}</td>
                      <td class="px-3 py-2 whitespace-nowrap text-right">{{ d.qte }}</td>
                      <td class="px-3 py-2 whitespace-nowrap text-right">{{ d.montant }}</td>
                      <td class="px-3 py-2 whitespace-nowrap">{{ d.idunitelib }}</td>
                      <td class="px-3 py-2 whitespace-nowrap">{{ d.idMachineLib }}</td>
                    </tr>
                    <tr v-if="details.length === 0">
                      <td colspan="10" class="px-3 py-4 text-center text-[11px] text-gray-500 italic">
                        Aucune ligne de détails trouvée pour cette fabrication.
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="mt-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
              <div class="flex flex-wrap gap-2">
                <button
                  type="button"
                  class="inline-flex justify-center py-1.5 px-3 border border-gray-300 shadow-sm text-xs font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'residu' } })"
                >
                  Résidu
                </button>
                <button
                  v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
                  type="button"
                  class="inline-flex justify-center py-1.5 px-3 border border-gray-300 shadow-sm text-xs font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'entree' } })"
                >
                  Mouvement entrée
                </button>
                <button
                  v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
                  type="button"
                  class="inline-flex justify-center py-1.5 px-3 border border-gray-300 shadow-sm text-xs font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'sortie' } })"
                >
                  Mouvement sortie
                </button>
              </div>

              <div class="flex justify-end space-x-3">
              <button
                v-if="fabrication.etat === 1 || fabrication.etat === '1'"
                type="button"
                @click="handleValidate"
                class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
              >
                Valider
              </button>
              <button
                type="button"
                @click="router.back()"
                class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
              >
                Retour à la liste
              </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
