<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const magasins = ref<Array<{ id: string; libelle: string }>>([])
const bonsDeCommande = ref<Array<{ id: string; designation: string; client?: string; reference?: string }>>([])
const ingredients = ref<Array<{ id: string; libelle: string; unite?: string }>>([])
const ofFilles = ref<Array<{ id: string; libelle: string }>>([])

const form = ref({
  daty: new Date().toISOString().split('T')[0],
  lancePar: '',
  cible: '',
  libelle: 'Test Creation Neuve',
  remarque: '',
  equipe: '',
  idBonDeCommande: '',
  ordreDeFab: (route.query.idOffille as string) || '',
  filles: [
    {
      idIngredients: 'IG000448',
      qte: 1,
      idunite: 'UNT001',
      idMachine: 'MACHN000004',
      libelle: 'Test Creation Neuve',
      remarque: '',
      idBcFille: ''
    }
  ]
})

// ofFilles will be used via a simple HTML datalist, like Bon de commande associé

const addFille = () => {
  form.value.filles.push({
    idIngredients: '',
    qte: 0,
    idunite: 'UNT001',
    idMachine: 'MACHN000004',
    libelle: form.value.libelle,
    remarque: '',
    idBcFille: ''
  })
}

const handleIngredientChange = (index: number) => {
  const fille = form.value.filles[index]
  if (!fille || !fille.idIngredients) return
  const ing = ingredients.value.find(i => i.id === fille.idIngredients)
  if (ing && ing.unite && !fille.idunite) {
    fille.idunite = ing.unite
  }
}

const removeFille = (index: number) => {
  if (form.value.filles.length > 1) {
    form.value.filles.splice(index, 1)
  }
}

const submitForm = async () => {
  loading.value = true
  error.value = null
  success.value = null
  try {
    const payload = {
      mere: {
        daty: form.value.daty,
        lancePar: form.value.lancePar,
        cible: form.value.cible,
        libelle: form.value.libelle,
        remarque: form.value.remarque,
        equipe: form.value.equipe,
        idBc: form.value.idBonDeCommande,
        ordreDeFab: form.value.ordreDeFab
      },
      filles: form.value.filles
    }

    const response = await axios.post('/FabricationServlet', payload)
    
    if (response.data.status === 'success') {
      success.value = "Fabrication créée avec succès !"
      setTimeout(() => {
        router.push('/fabrications')
      }, 2000)
    } else {
      error.value = response.data.message || "Erreur lors de la création."
    }
  } catch (err: any) {
    console.error(err)
    error.value = err.response?.data?.message || "Erreur lors de la création de la fabrication."
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const response = await axios.get('/MagasinPointServlet')
    magasins.value = response.data || []
    if (!form.value.lancePar && magasins.value.length > 0) {
      form.value.lancePar = magasins.value[0].id
    }
    if (!form.value.cible && magasins.value.length > 0) {
      form.value.cible = magasins.value[0].id
    }
    const bcResponse = await axios.get('/BonDeCommandeServlet')
    bonsDeCommande.value = bcResponse.data || []
    const ingResponse = await axios.get('/IngredientsServlet')
    ingredients.value = ingResponse.data || []
    const ofResponse = await axios.get('/OfFilleServlet')
    ofFilles.value = ofResponse.data || []
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
        <h1 class="text-3xl font-bold text-slate-900 tracking-tight">Nouvelle Fabrication</h1>
        <p class="mt-1 text-slate-500">Lancez une nouvelle opération de fabrication.</p>
      </div>
      <router-link to="/fabrications" class="inline-flex items-center px-4 py-2 text-sm font-semibold text-slate-600 hover:text-slate-900 transition-colors">
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
              <span class="w-8 h-8 bg-green-100 text-green-600 rounded-lg flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clip-rule="evenodd" />
                </svg>
              </span>
              Lancement
            </h3>
            
            <div class="space-y-5">
              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Date</label>
                <input v-model="form.daty" type="date" required class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:bg-white transition-all text-sm">
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Magasin Cible</label>
                <select v-model="form.cible" required class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:bg-white transition-all text-sm">
                  <option value="" disabled>Sélectionner un magasin</option>
                  <option v-for="mag in magasins" :key="mag.id" :value="mag.id">{{ mag.libelle }} ({{ mag.id }})</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Bon de commande</label>
                <select v-model="form.idBonDeCommande" class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:bg-white transition-all text-sm">
                  <option value="">Aucun</option>
                  <option v-for="bc in bonsDeCommande" :key="bc.id" :value="bc.id">{{ bc.designation }} ({{ bc.id }})</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Ordre de Fab (OF)</label>
                <input v-model="form.ordreDeFab" list="of-filles-list" class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:bg-white transition-all text-sm" placeholder="Code OF">
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Désignation</label>
                <input v-model="form.libelle" type="text" required class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:bg-white transition-all text-sm">
              </div>

              <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1">Équipe / Remarque</label>
                <input v-model="form.equipe" type="text" placeholder="Équipe de production" class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:bg-white transition-all text-sm mb-2">
                <textarea v-model="form.remarque" rows="2" placeholder="Notes..." class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:bg-white transition-all text-sm"></textarea>
              </div>
            </div>
          </div>
        </div>

        <!-- Lignes de Fabrication -->
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-200">
            <div class="flex items-center justify-between mb-8">
              <h3 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span class="w-8 h-8 bg-emerald-100 text-emerald-600 rounded-lg flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M5 3a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2V5a2 2 0 00-2-2H5zM5 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2H5zM11 5a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V5zM14 11a1 1 0 011 1v1h1a1 1 0 110 2h-1v1a1 1 0 11-2 0v-1h-1a1 1 0 110-2h1v-1a1 1 0 011-1z" />
                  </svg>
                </span>
                Opérations
              </h3>
              <button type="button" @click="addFille" class="inline-flex items-center px-4 py-2 bg-emerald-50 text-emerald-700 text-sm font-bold rounded-xl hover:bg-emerald-100 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1.5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                </svg>
                Ajouter une opération
              </button>
            </div>

            <div class="overflow-x-auto -mx-8">
              <table class="w-full border-collapse">
                <thead>
                  <tr class="bg-slate-50 border-y border-slate-100">
                    <th class="px-8 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Produit</th>
                    <th class="px-4 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Machine</th>
                    <th class="px-4 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider w-32">Quantité</th>
                    <th class="px-4 py-4 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Unité</th>
                    <th class="px-8 py-4 text-center text-xs font-bold text-slate-500 uppercase tracking-wider w-20"></th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="(f, index) in form.filles" :key="index" class="group hover:bg-slate-50/50 transition-colors">
                    <td class="px-8 py-4">
                      <input 
                        v-model="f.idIngredients" 
                        list="ingredients-fab-list" 
                        @change="handleIngredientChange(index)"
                        class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-green-500 text-sm"
                        placeholder="Produit"
                      >
                    </td>
                    <td class="px-4 py-4">
                      <input v-model="f.idMachine" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-green-500 text-sm" placeholder="Machine">
                    </td>
                    <td class="px-4 py-4">
                      <input v-model.number="f.qte" type="number" step="0.01" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-green-500 text-sm font-medium text-right">
                    </td>
                    <td class="px-4 py-4">
                      <input v-model="f.idunite" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-green-500 text-sm">
                    </td>
                    <td class="px-8 py-4 text-center">
                      <button @click="removeFille(index)" type="button" class="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all opacity-0 group-hover:opacity-100">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="flex items-center justify-end gap-4">
            <button type="button" @click="router.push('/fabrications')" class="px-6 py-3 text-sm font-bold text-slate-600 hover:text-slate-900 transition-colors">
              Annuler
            </button>
            <button 
              type="submit" 
              :disabled="loading" 
              class="px-10 py-3 bg-green-600 text-white text-sm font-bold rounded-2xl shadow-lg shadow-green-200 hover:bg-green-700 transform hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center gap-2 disabled:opacity-50"
            >
              <svg v-if="loading" class="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ loading ? 'Lancement...' : 'Confirmer le lancement' }}
            </button>
          </div>
        </div>
      </div>
    </form>

    <datalist id="ingredients-fab-list">
      <option v-for="ing in ingredients" :key="ing.id" :value="ing.id">{{ ing.libelle }}</option>
    </datalist>
    <datalist id="of-filles-list">
      <option v-for="off in ofFilles" :key="off.id" :value="off.id">{{ off.libelle }}</option>
    </datalist>
  </div>
</template>
