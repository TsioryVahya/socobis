<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

// Partie "mere" (OF) préremplie
const mere = ref({
  daty: new Date().toISOString().split('T')[0],
  lancePar: 'ADMIN',
  cible: 'PHARM001',
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

const submitForm = async () => {
  loading.value = true
  error.value = null
  success.value = null
  try {
    const response = await axios.post('/OfServlet', {
      mere: mere.value,
      filles: filles.value
    })
    
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
</script>

<template>
  <div class="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
    <div class="md:grid md:grid-cols-3 md:gap-6">
      <div class="md:col-span-1">
        <div class="px-4 sm:px-0">
          <h3 class="text-lg font-medium leading-6 text-gray-900">Nouvel Ordre de Fabrication</h3>
          <p class="mt-1 text-sm text-gray-600">
            Créer un nouvel Ordre de Fabrication (OF).
          </p>
        </div>
      </div>
      <div class="mt-5 md:mt-0 md:col-span-2">
        <form @submit.prevent="submitForm">
          <div class="shadow sm:rounded-md sm:overflow-hidden">
            <div class="px-4 py-5 bg-white space-y-6 sm:p-6">
              <div v-if="error" class="bg-red-50 p-4 rounded-md">
                <p class="text-sm text-red-700">{{ error }}</p>
              </div>
              <div v-if="success" class="bg-green-50 p-4 rounded-md">
                <p class="text-sm text-green-700">{{ success }}</p>
              </div>

              <!-- Partie Mère (OF) -->
              <div class="grid grid-cols-6 gap-6">
                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Date</label>
                  <input v-model="mere.daty" type="date" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Lancé par</label>
                  <input v-model="mere.lancePar" type="text" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Cible</label>
                  <input v-model="mere.cible" type="text" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6">
                  <label class="block text-sm font-medium text-gray-700">Libellé</label>
                  <input v-model="mere.libelle" type="text" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6">
                  <label class="block text-sm font-medium text-gray-700">Remarque</label>
                  <textarea v-model="mere.remarque" rows="2" class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md" />
                </div>
              </div>

              <!-- Partie Filles (lignes ingrédients) -->
              <div class="mt-8">
                <div class="flex items-center justify-between mb-2">
                  <h4 class="text-sm font-medium text-gray-900">Lignes de fabrication (filles)</h4>
                  <button type="button" @click="addFille" class="inline-flex items-center px-3 py-1 border border-indigo-500 text-xs font-medium rounded-md text-indigo-700 bg-white hover:bg-indigo-50">
                    + Ajouter une ligne
                  </button>
                </div>

                <div v-for="(f, index) in filles" :key="index" class="grid grid-cols-6 gap-4 mb-4 border rounded-md p-3 bg-gray-50">
                  <div class="col-span-6 sm:col-span-3">
                    <label class="block text-xs font-medium text-gray-700">Ingrédient / Produit</label>
                    <input v-model="f.idIngredients" type="text" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-xs border-gray-300 rounded-md">
                  </div>

                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-xs font-medium text-gray-700">Quantité</label>
                    <input v-model.number="f.qte" type="number" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-xs border-gray-300 rounded-md">
                  </div>

                  <div class="col-span-6 sm:col-span-1">
                    <label class="block text-xs font-medium text-gray-700">Unité</label>
                    <input v-model="f.idunite" type="text" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-xs border-gray-300 rounded-md">
                  </div>

                  <div class="col-span-6">
                    <label class="block text-xs font-medium text-gray-700">Libellé fille</label>
                    <input v-model="f.libelle" type="text" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-xs border-gray-300 rounded-md">
                  </div>
                </div>
              </div>
            </div>
            <div class="px-4 py-3 bg-gray-50 text-right sm:px-6 space-x-3">
              <button type="button" @click="router.back()" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                Annuler
              </button>
              <button type="submit" :disabled="loading" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                {{ loading ? 'Enregistrement...' : 'Créer l\'OF' }}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
