<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const ingredients = ref<Array<{ id: string; libelle: string; unite?: string }>>([])
const magasins = ref<Array<{ id: string; libelle: string }>>([])

// Partie "mere" (OF) préremplie
const mere = ref({
  daty: new Date().toISOString().split('T')[0],
  lancePar: 'ADMIN',
  cible: '',
  libelle: 'OF_PRODUCTION_001',
  remarque: 'Production prioritaire'
})

// Lignes "filles" préremplies
type FilleForm = {
  idIngredients: string
  qte: number
  idunite: string
  libelle: string
}

const filles = ref<FilleForm[]>([
  {
    idIngredients: 'IG000446',
    qte: 150.0,
    idunite: 'UNT001104',
    libelle: 'Feulle cuit gauffrette smack'
  },
  {
    idIngredients: 'IG000447',
    qte: 50.0,
    idunite: 'UNT001104',
    libelle: 'Smack fromage'
  }
])

const addFille = () => {
  filles.value.push({
    idIngredients: '',
    qte: 0,
    idunite: '',
    libelle: ''
  })
}

const handleIngredientChange = (index: number) => {
  const f = filles.value[index]
  if (!f || !f.idIngredients) return
  const ing = ingredients.value.find(i => i.id === f.idIngredients)
  if (ing) {
    if (ing.unite && !f.idunite) {
      f.idunite = ing.unite
    }
    if (ing.libelle && !f.libelle) {
      f.libelle = ing.libelle
    }
  }
}

const submitForm = async () => {
  loading.value = true
  error.value = null
  success.value = null
  try {
    const response = await axios.post('/OfServlet', {
      mere: mere.value,
      filles: filles.value.map(f => ({
        idIngredients: f.idIngredients,
        qte: f.qte,
        idunite: f.idunite,
        libelle: f.libelle
      }))
    })
    // ... rest of the code remains the same ...
    if (response.data.status === 'success') {
      success.value = "Ordre de Fabrication créé avec succès !"
      setTimeout(() => {
        router.push('/ofs')
      }, 2000)
    } else {
      error.value = response.data.message
    }
  } catch (err: any) {
    console.error(err)
    error.value = "Erreur lors de la création de l'OF."
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const [ingResp, magResp] = await Promise.all([
      axios.get('/IngredientsServlet'),
      axios.get('/MagasinPointServlet')
    ])
    ingredients.value = ingResp.data || []
    magasins.value = magResp.data || []
    if (!mere.value.cible && magasins.value.length > 0) {
      mere.value.cible = magasins.value[0].id
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<template>
  <div class="max-w-6xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
    <!-- Header -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-3xl font-bold text-slate-900 tracking-tight">Nouvel Ordre de Fabrication</h1>
        <p class="mt-1 text-slate-500">Créez et configurez un nouvel OF pour la production.</p>
      </div>
      <router-link to="/ofs" class="inline-flex items-center px-4 py-2 text-sm font-semibold text-slate-600 hover:text-slate-900 transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        Retour à la liste
      </router-link>
    </div>

    <form @submit.prevent="submitForm" class="space-y-8">
      <!-- Status Messages -->
      <transition enter-active-class="transition duration-300 ease-out" enter-from-class="transform -translate-y-4 opacity-0" enter-to-class="transform translate-y-0 opacity-100">
        <div v-if="error" class="bg-red-50 border border-red-200 text-red-800 px-6 py-4 rounded-2xl flex items-center gap-3 shadow-sm">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
          </svg>
          <span class="font-medium">{{ error }}</span>
        </div>
      </transition>

      <transition enter-active-class="transition duration-300 ease-out" enter-from-class="transform -translate-y-4 opacity-0" enter-to-class="transform translate-y-0 opacity-100">
        <div v-if="success" class="bg-emerald-50 border border-emerald-200 text-emerald-800 px-6 py-4 rounded-2xl flex items-center gap-3 shadow-sm">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-emerald-500" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          <span class="font-medium">{{ success }}</span>
        </div>
      </transition>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Informations Générales -->
        <div class="lg:col-span-1 space-y-6">
          <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-200">
            <h3 class="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
              <span class="w-8 h-8 bg-blue-100 text-blue-600 rounded-lg flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd" />
                </svg>
              </span>
              Général
            </h3>
            
            <div class="space-y-5">
              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Date de lancement</label>
                <input v-model="mere.daty" type="date" required class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all text-sm">
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Magasin Cible</label>
                <select v-model="mere.cible" required class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all text-sm">
                  <option value="" disabled>Sélectionner un magasin</option>
                  <option v-for="mag in magasins" :key="mag.id" :value="mag.id">{{ mag.libelle }} ({{ mag.id }})</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Désignation</label>
                <input v-model="mere.libelle" type="text" required placeholder="Ex: Production Gaufrettes" class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all text-sm">
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Remarque</label>
                <textarea v-model="mere.remarque" rows="3" placeholder="Informations complémentaires..." class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all text-sm"></textarea>
              </div>
            </div>
          </div>
        </div>

        <!-- Détails des Produits -->
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-200">
            <div class="flex items-center justify-between mb-8">
              <h3 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span class="w-8 h-8 bg-indigo-100 text-indigo-600 rounded-lg flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M7 3a1 1 0 000 2h6a1 1 0 100-2H7zM4 7a1 1 0 011-1h10a1 1 0 110 2H5a1 1 0 01-1-1zM2 11a2 2 0 012-2h12a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z" />
                  </svg>
                </span>
                Composition de l'Ordre
              </h3>
              <button type="button" @click="addFille" class="inline-flex items-center px-4 py-2 bg-blue-50 text-blue-700 text-sm font-bold rounded-xl hover:bg-blue-100 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1.5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                </svg>
                Ajouter un produit
              </button>
            </div>

            <div class="overflow-x-auto -mx-8">
              <table class="w-full border-collapse">
                <thead>
                  <tr class="bg-slate-50 border-y border-slate-100">
                    <th class="px-8 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Produit</th>
                    <th class="px-4 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Libellé</th>
                    <th class="px-4 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Unité</th>
                    <th class="px-4 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider w-32">Quantité</th>
                    <th class="px-8 py-4 text-center text-xs font-bold text-slate-500 uppercase tracking-wider w-20"></th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="(f, index) in filles" :key="index" class="group hover:bg-slate-50/50 transition-colors">
                    <td class="px-8 py-4">
                      <input 
                        v-model="f.idIngredients" 
                        list="ingredients-of-list" 
                        @change="handleIngredientChange(index)"
                        class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
                        placeholder="Code produit"
                      >
                    </td>
                    <td class="px-4 py-4">
                      <input v-model="f.libelle" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm">
                    </td>
                    <td class="px-4 py-4">
                      <input v-model="f.idunite" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm">
                    </td>
                    <td class="px-4 py-4">
                      <input v-model.number="f.qte" type="number" step="0.01" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm font-medium text-right">
                    </td>
                    <td class="px-8 py-4 text-center">
                      <button @click="filles.splice(index, 1)" type="button" class="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all opacity-0 group-hover:opacity-100">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="mt-8 p-6 bg-slate-50 rounded-2xl border border-slate-100">
              <div class="flex items-center gap-4">
                <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center text-slate-400 border border-slate-200">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                  </svg>
                </div>
                <div>
                  <h4 class="text-sm font-bold text-slate-900">Import Excel rapide</h4>
                  <p class="text-xs text-slate-500 mb-2">Gagnez du temps en important votre liste de produits via un fichier Excel.</p>
                  <input type="file" class="block w-full text-xs text-slate-500 file:mr-4 file:py-1.5 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-bold file:bg-blue-600 file:text-white hover:file:bg-blue-700 transition-all cursor-pointer">
                </div>
              </div>
            </div>
          </div>

          <div class="flex items-center justify-end gap-4">
            <button type="button" @click="router.push('/ofs')" class="px-6 py-3 text-sm font-bold text-slate-600 hover:text-slate-900 transition-colors">
              Annuler
            </button>
            <button 
              type="submit" 
              :disabled="loading" 
              class="px-10 py-3 bg-blue-600 text-white text-sm font-bold rounded-2xl shadow-lg shadow-blue-200 hover:bg-blue-700 transform hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center gap-2 disabled:opacity-50"
            >
              <svg v-if="loading" class="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ loading ? 'Création en cours...' : 'Enregistrer l\'OF' }}
            </button>
          </div>
        </div>
      </div>
    </form>

    <datalist id="ingredients-of-list">
      <option v-for="ing in ingredients" :key="ing.id" :value="ing.id">
        {{ ing.id }} - {{ ing.libelle }}<span v-if="ing.unite"> ({{ ing.unite }})</span>
      </option>
    </datalist>
  </div>
</template>
