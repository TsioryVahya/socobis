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
  <div class="min-h-screen bg-slate-50/50 pb-12">
    <!-- Header -->
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
            <h1 class="text-xl font-bold text-slate-900">{{ title }}</h1>
          </div>
          <div class="flex items-center gap-2">
            <span :class="{
              'px-3 py-1 text-xs font-bold rounded-full border': true,
              'bg-blue-50 text-blue-700 border-blue-100': type === 'entree',
              'bg-amber-50 text-amber-700 border-amber-100': type === 'sortie',
              'bg-emerald-50 text-emerald-700 border-emerald-100': type === 'residu'
            }">
              {{ type.toUpperCase() }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Loading State -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20 bg-white rounded-2xl border border-slate-200 shadow-sm">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600 mb-4"></div>
        <p class="text-slate-500 font-medium">Chargement des données...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-100 rounded-2xl p-6 flex items-start gap-4">
        <div class="p-2 bg-red-100 rounded-lg">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <div>
          <h3 class="text-red-800 font-bold mb-1">Une erreur est survenue</h3>
          <p class="text-red-700">{{ error }}</p>
          <button @click="fetchMvt" class="mt-4 px-4 py-2 bg-red-600 text-white text-sm font-semibold rounded-xl hover:bg-red-700 transition-colors">
            Réessayer
          </button>
        </div>
      </div>

      <div v-else class="space-y-8">
        <!-- Messages -->
        <transition enter-active-class="transition duration-300 ease-out" enter-from-class="transform -translate-y-4 opacity-0" enter-to-class="transform translate-y-0 opacity-100">
          <div v-if="saveMessage" class="bg-emerald-50 border border-emerald-100 rounded-2xl p-4 flex items-center gap-3 shadow-sm">
            <div class="p-1.5 bg-emerald-100 rounded-full">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-emerald-600" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
            </div>
            <p class="text-emerald-800 font-medium">{{ saveMessage }}</p>
          </div>
        </transition>

        <!-- Form Section -->
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div class="p-6 sm:p-8 space-y-8">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <!-- Fabrication Info -->
              <div class="space-y-2">
                <label class="block text-sm font-medium text-slate-700">Fabrication</label>
                <div class="px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-600 font-mono text-sm">
                  {{ idFab }}
                </div>
              </div>

              <!-- Date Info -->
              <div class="space-y-2" v-if="mere">
                <label class="block text-sm font-medium text-slate-700">Date du mouvement</label>
                <input 
                  v-model="mere.daty" 
                  type="date" 
                  class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                >
              </div>

              <!-- Magasin Selection -->
              <div class="space-y-2" v-if="mere">
                <label class="block text-sm font-medium text-slate-700">Magasin de destination</label>
                <select 
                  v-model="mere.idMagasin" 
                  class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                >
                  <option value="" disabled>Choisir un magasin</option>
                  <option v-for="mag in magasins" :key="mag.id" :value="mag.id">
                    {{ mag.libelle }} ({{ mag.id }})
                  </option>
                </select>
              </div>

              <!-- Designation -->
              <div class="md:col-span-2 lg:col-span-3 space-y-2" v-if="mere">
                <label class="block text-sm font-medium text-slate-700">Désignation / Motif</label>
                <input 
                  v-model="mere.designation" 
                  type="text" 
                  placeholder="Ex: Entrée de produits finis..."
                  class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                >
              </div>
            </div>

            <!-- Table Section -->
            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <h3 class="text-lg font-bold text-slate-900">Articles concernés</h3>
                <span class="text-sm text-slate-500">{{ lignes.length }} article(s)</span>
              </div>
              
              <div class="border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
                <table class="min-w-full divide-y divide-slate-200">
                  <thead class="bg-slate-50">
                    <tr>
                      <th class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Produit</th>
                      <th class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Désignation</th>
                      <th class="px-6 py-4 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Quantité</th>
                      <th class="px-6 py-4 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Prix Unitaire</th>
                      <th class="px-6 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Origine</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-slate-100">
                    <tr v-for="(l, idx) in lignes" :key="l.idProduit || idx" class="hover:bg-slate-50/50 transition-colors">
                      <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-indigo-600">{{ l.idProduit }}</td>
                      <td class="px-6 py-4 text-sm text-slate-600 font-medium">{{ l.designation }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-right">
                        <div class="inline-flex items-center gap-2">
                          <input 
                            v-if="isEntreeType"
                            v-model.number="l.entree" 
                            type="number" 
                            step="0.01" 
                            class="w-24 px-3 py-1.5 rounded-lg border-slate-200 bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all text-right text-sm font-bold"
                          >
                          <input 
                            v-else
                            v-model.number="l.sortie" 
                            type="number" 
                            step="0.01" 
                            class="w-24 px-3 py-1.5 rounded-lg border-slate-200 bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all text-right text-sm font-bold"
                          >
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-right">
                        <input 
                          v-model.number="l.pu" 
                          type="number" 
                          step="0.01" 
                          class="w-28 px-3 py-1.5 rounded-lg border-slate-200 bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all text-right text-sm"
                        >
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-400 italic">{{ l.mvtSrc || '-' }}</td>
                    </tr>
                    <tr v-if="lignes.length === 0">
                      <td colspan="5" class="px-6 py-12 text-center">
                        <div class="flex flex-col items-center">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-slate-200 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 00-2 2H6a2 2 0 00-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                          </svg>
                          <p class="text-slate-400 italic">Aucun article à mouvementer</p>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Footer Actions -->
          <div class="px-8 py-6 bg-slate-50 border-t border-slate-200 flex flex-col sm:flex-row justify-end gap-3">
            <button 
              type="button" 
              @click="router.back()" 
              class="px-6 py-2.5 rounded-xl border border-slate-200 bg-white text-sm font-semibold text-slate-600 hover:bg-slate-50 transition-colors"
            >
              Annuler
            </button>
            <button 
              @click="handleSave"
              :disabled="saving || lignes.length === 0" 
              class="px-6 py-2.5 rounded-xl bg-indigo-600 text-sm font-semibold text-white hover:bg-indigo-700 focus:ring-4 focus:ring-indigo-500/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 shadow-lg shadow-indigo-500/25"
            >
              <svg v-if="saving" class="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ saving ? 'Enregistrement en cours...' : 'Valider le mouvement' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
