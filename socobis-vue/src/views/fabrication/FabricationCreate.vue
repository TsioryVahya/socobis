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
  <div class="content-wrapper py-4 px-2 sm:px-4">
    <h1 class="box-title text-xl font-semibold mb-4">
      Nouvelle fabrication
    </h1>

    <div class="row m-0">
      <div class="col-md-3"></div>
      <div class="col-md-6">
        <div class="box box-fiche shadow-sm border border-gray-200 rounded-md bg-white">
          <div class="box-body px-4 py-4 space-y-4">
            <p class="text-sm text-gray-600 mb-2">
              Lancer un nouvel ordre de fabrication (OF).
            </p>

            <form @submit.prevent="submitForm">
              <div v-if="error" class="bg-red-50 p-4 rounded-md">
                <p class="text-sm text-red-700">{{ error }}</p>
              </div>
              <div v-if="success" class="bg-green-50 p-4 rounded-md">
                <p class="text-sm text-green-700">{{ success }}</p>
              </div>

              <div class="grid grid-cols-6 gap-4">
                <!-- Informations Générales -->
                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Date</label>
                  <input
                    v-model="form.daty"
                    type="date"
                    required
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Lancé par</label>
                  <select
                    v-model="form.lancePar"
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white pl-3 pr-10 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    required
                  >
                    <option disabled value="">Sélectionner un magasin</option>
                    <option
                      v-for="magasin in magasins"
                      :key="magasin.id"
                      :value="magasin.id"
                    >
                      {{ magasin.libelle }}
                    </option>
                  </select>
                </div>

                <div class="col-span-6">
                  <label class="block text-sm font-medium text-gray-700">Remarque</label>
                  <input
                    v-model="form.remarque"
                    type="text"
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Cible</label>
                  <select
                    v-model="form.cible"
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white pl-3 pr-10 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    required
                  >
                    <option disabled value="">Sélectionner un magasin</option>
                    <option
                      v-for="magasin in magasins"
                      :key="'cible-' + magasin.id"
                      :value="magasin.id"
                    >
                      {{ magasin.libelle }}
                    </option>
                  </select>
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Équipe</label>
                  <input
                    v-model="form.equipe"
                    type="text"
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                </div>

                <div class="col-span-6">
                  <label class="block text-sm font-medium text-gray-700">Désignation</label>
                  <input
                    v-model="form.libelle"
                    type="text"
                    required
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                </div>

                <!-- Références -->
                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Bon de commande associé</label>
                  <input
                    v-model="form.idBonDeCommande"
                    list="bons-de-commande-list"
                    type="text"
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                  <datalist id="bons-de-commande-list">
                    <option
                      v-for="bc in bonsDeCommande"
                      :key="bc.id"
                      :value="bc.id"
                    >
                      {{ bc.id }} - {{ bc.designation }}<span v-if="bc.client"> ({{ bc.client }})</span>
                    </option>
                  </datalist>
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Ordre de fabrication associé</label>
                  <input
                    v-model="form.ordreDeFab"
                    list="of-filles-list"
                    type="text"
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                  <datalist id="of-filles-list">
                    <option
                      v-for="of in ofFilles"
                      :key="of.id"
                      :value="of.id"
                    >
                      {{ of.id }} - {{ of.libelle }}
                    </option>
                  </datalist>
                </div>

                <!-- Produits / Ingrédients Filles -->
                <div class="col-span-6">
                  <div class="flex items-center justify-between mb-4">
                    <h4 class="text-md font-medium text-gray-900">Composants</h4>
                    <button type="button" @click="addFille" class="inline-flex items-center px-3 py-1 border border-transparent text-sm font-medium rounded-md text-green-700 bg-green-100 hover:bg-green-200">
                      + Ajouter un produit
                    </button>
                  </div>

                  <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Entrez le chemin de votre fichier Excel</label>
                    <input
                      type="file"
                      class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  
                  <datalist id="ingredients-list">
                    <option
                      v-for="ing in ingredients"
                      :key="ing.id"
                      :value="ing.id"
                    >
                      {{ ing.id }} - {{ ing.libelle }}<span v-if="ing.unite"> ({{ ing.unite }})</span>
                    </option>
                  </datalist>

                  <div v-for="(fille, index) in form.filles" :key="index" class="p-4 border border-gray-200 rounded-md mb-4 bg-gray-50">
                    <div class="grid grid-cols-6 gap-4">
                      <div class="col-span-6 sm:col-span-3">
                        <label class="block text-sm font-medium text-gray-700">Composants</label>
                        <input
                          v-model="fille.idIngredients"
                          list="ingredients-list"
                          type="text"
                          required
                          @change="handleIngredientChange(index)"
                          class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                      </div>

                      <div class="col-span-6 sm:col-span-3">
                        <label class="block text-sm font-medium text-gray-700">Remarque</label>
                        <input
                          v-model="fille.remarque"
                          type="text"
                          class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                      </div>

                      <div class="col-span-6 sm:col-span-2">
                        <label class="block text-sm font-medium text-gray-700">Unité</label>
                        <input
                          v-model="fille.idunite"
                          type="text"
                          required
                          class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                      </div>

                      <div class="col-span-6 sm:col-span-2">
                        <label class="block text-sm font-medium text-gray-700">Bon de commande fille</label>
                        <input
                          v-model="fille.idBcFille"
                          type="text"
                          class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                      </div>

                      <div class="col-span-6 sm:col-span-2">
                        <label class="block text-sm font-medium text-gray-700">Machine</label>
                        <input
                          v-model="fille.idMachine"
                          type="text"
                          required
                          class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                      </div>

                      <div class="col-span-6 sm:col-span-2">
                        <label class="block text-sm font-medium text-gray-700">Quantité</label>
                        <input
                          v-model.number="fille.qte"
                          type="number"
                          required
                          class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                      </div>

                      <div class="col-span-6 sm:col-span-2 flex items-end justify-end">
                        <button v-if="form.filles.length > 1" type="button" @click="removeFille(index)" class="text-red-600 hover:text-red-800 text-sm font-medium">
                          Supprimer cette ligne
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </form>
          </div>
          <div class="box-footer px-4 py-3 bg-gray-50 text-right space-x-3">
            <button
              type="button"
              @click="router.back()"
              class="btn btn-secondary inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
            >
              Annuler
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="btn btn-primary inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
            >
              {{ loading ? 'Lancement...' : 'Lancer la fabrication' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
