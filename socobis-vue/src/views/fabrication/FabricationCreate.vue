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
      idIngredients: 'ING000T0129',
      qte: 75600,
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
  <div class="space-y-6 max-w-5xl mx-auto">
    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Nouvelle Fabrication</h1>
        <p class="text-slate-500 text-sm mt-1">Créez un nouvel ordre de fabrication et gérez ses composants.</p>
      </div>
      <button @click="router.back()" class="inline-flex items-center px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-xl font-semibold transition-all">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        Retour
      </button>
    </div>

    <form @submit.prevent="submitForm" class="space-y-6">
      <!-- Status Messages -->
      <div v-if="error" class="bg-red-500/10 border border-red-500/20 p-4 rounded-2xl animate-shake">
        <p class="text-sm text-red-600 font-medium text-center">{{ error }}</p>
      </div>
      <div v-if="success" class="bg-emerald-500/10 border border-emerald-500/20 p-4 rounded-2xl">
        <p class="text-sm text-emerald-600 font-medium text-center">{{ success }}</p>
      </div>

      <!-- Main Form Card -->
      <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
        <div class="p-6 md:p-8">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Left Column: Basics -->
            <div class="space-y-6">
              <h3 class="text-lg font-bold text-slate-800 flex items-center">
                <span class="w-8 h-8 bg-indigo-100 text-indigo-600 rounded-lg flex items-center justify-center mr-3 text-sm">01</span>
                Informations Générales
              </h3>
              
              <div class="grid grid-cols-1 gap-4">
                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Date de fabrication</label>
                  <input v-model="form.daty" type="date" required class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
                </div>

                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Désignation / Libellé</label>
                  <input v-model="form.libelle" type="text" required placeholder="Nom de l'opération" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
                </div>

                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Remarque</label>
                  <textarea v-model="form.remarque" rows="2" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all resize-none"></textarea>
                </div>
              </div>
            </div>

            <!-- Right Column: Logistics -->
            <div class="space-y-6">
              <h3 class="text-lg font-bold text-slate-800 flex items-center">
                <span class="w-8 h-8 bg-indigo-100 text-indigo-600 rounded-lg flex items-center justify-center mr-3 text-sm">02</span>
                Logistique & Équipe
              </h3>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Lancé par</label>
                  <select v-model="form.lancePar" required class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all appearance-none">
                    <option disabled value="">Magasin source</option>
                    <option v-for="m in magasins" :key="m.id" :value="m.id">{{ m.libelle }}</option>
                  </select>
                </div>
                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Magasin Cible</label>
                  <select v-model="form.cible" required class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all appearance-none">
                    <option disabled value="">Destination</option>
                    <option v-for="m in magasins" :key="'c-'+m.id" :value="m.id">{{ m.libelle }}</option>
                  </select>
                </div>
                <div class="space-y-2 sm:col-span-2">
                  <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Équipe de production</label>
                  <input v-model="form.equipe" type="text" placeholder="Ex: Équipe A" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
                </div>
              </div>
            </div>
          </div>

          <!-- Section: Références -->
          <div class="mt-10 pt-10 border-t border-slate-100 grid grid-cols-1 md:grid-cols-2 gap-6">
             <div class="space-y-2">
                <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Bon de Commande Associé</label>
                <input v-model="form.idBonDeCommande" list="bc-list" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
                <datalist id="bc-list">
                  <option v-for="bc in bonsDeCommande" :key="bc.id" :value="bc.id">{{ bc.id }} - {{ bc.designation }}</option>
                </datalist>
             </div>
             <div class="space-y-2">
                <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">Ordre de Fabrication Associé</label>
                <input v-model="form.ordreDeFab" list="of-list" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
                <datalist id="of-list">
                  <option v-for="of in ofFilles" :key="of.id" :value="of.id">{{ of.id }} - {{ of.libelle }}</option>
                </datalist>
             </div>
          </div>
        </div>
      </div>

      <!-- Section: Composants -->
      <div class="space-y-4">
        <div class="flex items-center justify-between px-2">
          <h3 class="text-lg font-bold text-slate-800 flex items-center">
            <span class="w-8 h-8 bg-indigo-100 text-indigo-600 rounded-lg flex items-center justify-center mr-3 text-sm">03</span>
            Composants & Ingrédients
          </h3>
          <button type="button" @click="addFille" class="inline-flex items-center px-4 py-2 bg-emerald-500 text-white rounded-xl font-bold shadow-lg shadow-emerald-500/20 hover:bg-emerald-600 transition-all">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
            Ajouter une ligne
          </button>
        </div>

        <div class="space-y-4">
          <div v-for="(fille, index) in form.filles" :key="index" class="bg-white p-6 rounded-3xl shadow-sm border border-slate-200 group hover:border-indigo-200 transition-all relative overflow-hidden">
            <div class="absolute top-0 left-0 w-1 h-full bg-slate-100 group-hover:bg-indigo-400 transition-colors"></div>
            
            <div class="grid grid-cols-1 md:grid-cols-12 gap-6">
              <!-- Composant -->
              <div class="md:col-span-4 space-y-2">
                <label class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">Composant / Ingrédient</label>
                <input v-model="fille.idIngredients" list="ing-list" @change="handleIngredientChange(index)" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
              </div>

              <!-- Quantité & Unité -->
              <div class="md:col-span-3 grid grid-cols-2 gap-3">
                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">Quantité</label>
                  <input v-model.number="fille.qte" type="number" step="any" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
                </div>
                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">Unité</label>
                  <input v-model="fille.idunite" type="text" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
                </div>
              </div>

              <!-- Machine & BC Fille -->
              <div class="md:col-span-4 grid grid-cols-2 gap-3">
                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">Machine</label>
                  <input v-model="fille.idMachine" type="text" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
                </div>
                <div class="space-y-2">
                  <label class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">BC Fille</label>
                  <input v-model="fille.idBcFille" type="text" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
                </div>
              </div>

              <!-- Delete Button -->
              <div class="md:col-span-1 flex items-end justify-center">
                <button v-if="form.filles.length > 1" type="button" @click="removeFille(index)" class="p-3 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition-all">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>

              <!-- Bottom Row: Remarque Fille -->
              <div class="md:col-span-11 space-y-2">
                <label class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">Remarque spécifique</label>
                <input v-model="fille.remarque" type="text" placeholder="Note pour ce composant..." class="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-sm">
              </div>
            </div>
          </div>
        </div>

        <datalist id="ing-list">
          <option v-for="ing in ingredients" :key="ing.id" :value="ing.id">{{ ing.libelle }}</option>
        </datalist>
      </div>

      <!-- Action Buttons -->
      <div class="flex items-center justify-end space-x-4 pt-6">
        <button type="button" @click="router.back()" class="px-8 py-4 text-slate-600 font-bold hover:bg-slate-100 rounded-2xl transition-all">
          Annuler
        </button>
        <button 
          type="submit" 
          :disabled="loading" 
          class="px-10 py-4 bg-indigo-600 text-white rounded-2xl font-bold shadow-xl shadow-indigo-600/30 hover:bg-indigo-700 transition-all transform active:scale-[0.98] disabled:opacity-50"
        >
          <span v-if="loading" class="flex items-center">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Traitement...
          </span>
          <span v-else>Lancer la Fabrication</span>
        </button>
      </div>
    </form>
  </div>
</template>
