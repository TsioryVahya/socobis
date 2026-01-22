<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const idFab = computed(() => (route.params.id as string) || '')

const loading = ref(true)
const error = ref<string | null>(null)
const saving = ref(false)
const saveMessage = ref<string | null>(null)

const types = ref<Array<{ id: string; val: string }>>([])
const lignes = ref<
  Array<{
    daty: string
    libelle: string
    type: string
    idIngredients: string
    ingredientsLabel: string
    qte: number | null
    pu: number | null
  }>
>([])

const ingredientLoading = ref(false)
const ingredientSuggestions = ref<Record<number, Array<{ id: string; label: string; retour?: string }>>>({})
const ingredientLastQuery = ref<Record<number, string>>({})

const todayISO = () => {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

const addLine = () => {
  lignes.value.push({
    daty: todayISO(),
    libelle: '',
    type: types.value[0]?.id || '1',
    idIngredients: '',
    ingredientsLabel: '',
    qte: null,
    pu: null
  })
}

const parseRetourValue = (retour: string | undefined, idx: number) => {
  if (!retour) return
  const parts = retour.split(';')
  const v = parts[idx]
  if (v == null || v === '') return
  const n = Number(String(v).replace(',', '.'))
  if (!Number.isNaN(n)) return n
}

const fetchIngredientSuggestions = async (rowIndex: number) => {
  const q = (lignes.value[rowIndex]?.ingredientsLabel || '').trim()
  ingredientLastQuery.value[rowIndex] = q

  if (!q || q.length < 1) {
    ingredientSuggestions.value[rowIndex] = []
    return
  }

  ingredientLoading.value = true
  try {
    const resp = await axios.get('/autocomplete', {
      params: {
        libelle: q,
        affiche: 'null',
        valeur: 'id',
        colFiltre: 'null',
        nomTable: 'ST_INGREDIENTSAUTO',
        classe: 'produits.IngredientsLib',
        useMotcle: 'true',
        champRetour: 'pu'
      }
    })

    const data: any = resp.data
    const arr: any[] = Array.isArray(data?.valeure) ? data.valeure : []
    const mapped = arr
      .map((it) => ({
        id: String(it.id ?? ''),
        label: String(it.valeur ?? ''),
        retour: it.retour != null ? String(it.retour) : undefined
      }))
      .filter((x) => x.id)

    if (ingredientLastQuery.value[rowIndex] === q) {
      ingredientSuggestions.value[rowIndex] = mapped
    }
  } catch (e) {
    ingredientSuggestions.value[rowIndex] = []
  } finally {
    ingredientLoading.value = false
  }
}

const selectIngredient = (rowIndex: number, s: { id: string; label: string; retour?: string }) => {
  const row = lignes.value[rowIndex]
  if (!row) return
  row.idIngredients = s.id
  row.ingredientsLabel = s.label
  const pu = parseRetourValue(s.retour, 0)
  if (pu != null) row.pu = pu
  ingredientSuggestions.value[rowIndex] = []
}

const hideIngredientSuggestionsLater = (rowIndex: number) => {
  window.setTimeout(() => {
    ingredientSuggestions.value[rowIndex] = []
  }, 200)
}

const removeLine = (idx: number) => {
  lignes.value.splice(idx, 1)
}

const fetchPrepare = async () => {
  loading.value = true
  error.value = null
  saveMessage.value = null

  try {
    const resp = await axios.get('/ChargeServlet', {
      params: { action: 'prepareFromFab', idFab: idFab.value }
    })

    let payload: any = resp.data
    if (typeof payload === 'string') {
      payload = JSON.parse(payload)
    }

    if (payload.status !== 'success') {
      error.value = payload.message || 'Erreur lors du chargement des charges'
      return
    }

    types.value = payload.types || []
    lignes.value = []
    addLine()
  } catch (e: any) {
    console.error(e)
    error.value = 'Erreur lors du chargement des charges'
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!idFab.value) {
    error.value = 'Identifiant fabrication manquant'
    return
  }

  if (lignes.value.length === 0) {
    error.value = 'Aucune ligne à enregistrer'
    return
  }

  saving.value = true
  error.value = null
  saveMessage.value = null

  try {
    const payload = {
      idFab: idFab.value,
      lignes: lignes.value
    }

    const resp = await axios.post('/ChargeServlet', payload, {
      params: { action: 'saveFromFab' }
    })

    let data: any = resp.data
    if (typeof data === 'string') {
      data = JSON.parse(data)
    }

    if (data.status === 'success') {
      saveMessage.value = data.message || 'Charges enregistrées avec succès.'
      setTimeout(() => {
        router.back()
      }, 1500)
    } else {
      error.value = data.message || "Erreur lors de l'enregistrement des charges"
    }
  } catch (e: any) {
    console.error(e)
    error.value = "Erreur lors de l'enregistrement des charges"
  } finally {
    saving.value = false
  }
}

onMounted(fetchPrepare)
</script>

<template>
  <div class="min-h-screen bg-slate-50/50 pb-12">
    <div class="bg-white border-b border-slate-200 sticky top-0 z-10 backdrop-blur-md bg-white/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center gap-4">
            <button
              @click="router.back()"
              class="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500 hover:text-slate-700"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </button>
            <h1 class="text-xl font-bold text-slate-900">Saisie des charges</h1>
          </div>

          <div class="flex items-center gap-3">
            <button
              @click="addLine"
              class="inline-flex items-center px-4 py-2 text-sm font-bold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-50 transition-all active:scale-95 shadow-sm"
              :disabled="loading || saving"
            >
              Ajouter ligne
            </button>
            <button
              @click="handleSave"
              class="inline-flex items-center px-5 py-2 text-sm font-bold text-white bg-indigo-600 rounded-xl hover:bg-indigo-700 transition-all active:scale-95 shadow-sm"
              :disabled="loading || saving"
            >
              {{ saving ? 'Enregistrement...' : 'Enregistrer' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        <p class="mt-4 text-slate-500 font-medium">Chargement...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 border border-red-100 rounded-2xl p-4 flex items-start gap-3 text-red-700">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
        </svg>
        <p class="font-medium">{{ error }}</p>
      </div>

      <div v-else class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
          <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Lignes de charges</h2>
          <p class="text-xs text-slate-500 mt-1">Fabrication: <span class="font-mono font-semibold">{{ idFab }}</span></p>
        </div>

        <div class="p-6">
          <div v-if="saveMessage" class="mb-4 bg-emerald-50 border border-emerald-100 rounded-xl p-3 text-emerald-700 font-medium">
            {{ saveMessage }}
          </div>

          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200">
              <thead>
                <tr class="bg-slate-50">
                  <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Date</th>
                  <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Description</th>
                  <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Type</th>
                  <th class="px-4 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Ingrédient</th>
                  <th class="px-4 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Qté</th>
                  <th class="px-4 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">P.U.</th>
                  <th class="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-slate-100">
                <tr v-for="(l, idx) in lignes" :key="idx" class="hover:bg-slate-50/80 transition-colors">
                  <td class="px-4 py-2">
                    <input v-model="l.daty" type="date" class="w-40 px-3 py-2 border border-slate-300 rounded-lg text-sm" />
                  </td>
                  <td class="px-4 py-2">
                    <input v-model="l.libelle" type="text" class="w-80 px-3 py-2 border border-slate-300 rounded-lg text-sm" placeholder="Description" />
                  </td>
                  <td class="px-4 py-2">
                    <select v-model="l.type" class="w-56 px-3 py-2 border border-slate-300 rounded-lg text-sm">
                      <option v-for="t in types" :key="t.id" :value="t.id">{{ t.val }}</option>
                    </select>
                  </td>
                  <td class="px-4 py-2">
                    <div class="relative w-80">
                      <input
                        v-model="l.ingredientsLabel"
                        type="text"
                        class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                        placeholder="Rechercher ingrédient..."
                        @input="() => { l.idIngredients = ''; fetchIngredientSuggestions(idx) }"
                        @focus="() => fetchIngredientSuggestions(idx)"
                        @blur="() => hideIngredientSuggestionsLater(idx)"
                      />

                      <div
                        v-if="(ingredientSuggestions[idx] || []).length > 0"
                        class="absolute z-20 mt-1 w-full bg-white border border-slate-200 rounded-lg shadow-lg max-h-56 overflow-auto"
                      >
                        <button
                          v-for="s in ingredientSuggestions[idx]"
                          :key="s.id"
                          type="button"
                          class="w-full text-left px-3 py-2 text-sm hover:bg-slate-50"
                          @click="selectIngredient(idx, s)"
                        >
                          <span class="font-mono font-semibold text-slate-700">{{ s.id }}</span>
                          <span class="text-slate-700"> - {{ s.label }}</span>
                        </button>
                      </div>

                      <div v-else-if="ingredientLoading && (l.ingredientsLabel || '').length > 0" class="absolute right-3 top-3 text-xs text-slate-400">
                        ...
                      </div>
                    </div>
                  </td>
                  <td class="px-4 py-2 text-right">
                    <input v-model.number="l.qte" type="number" step="0.000001" class="w-28 px-3 py-2 border border-slate-300 rounded-lg text-sm text-right" />
                  </td>
                  <td class="px-4 py-2 text-right">
                    <input v-model.number="l.pu" type="number" step="0.01" class="w-28 px-3 py-2 border border-slate-300 rounded-lg text-sm text-right" />
                  </td>
                  <td class="px-4 py-2 text-right">
                    <button
                      @click="removeLine(idx)"
                      class="px-3 py-2 text-xs font-bold text-red-600 hover:bg-red-50 rounded-lg"
                      :disabled="saving"
                    >
                      Supprimer
                    </button>
                  </td>
                </tr>
                <tr v-if="lignes.length === 0">
                  <td colspan="7" class="px-4 py-10 text-center text-slate-400 italic">Aucune ligne.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
