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
  <div class="content-wrapper py-4 px-2 sm:px-4">
    <h1 class="box-title text-xl font-semibold mb-4 flex items-center gap-2">
      <span>Saisie d'un ordre de fabrication</span>
    </h1>

    <div class="max-w-5xl mx-auto">
      <div class="box box-fiche">
        <div class="box-body">
          <form @submit.prevent="submitForm">
            <div class="space-y-6">
              <div v-if="error" class="bg-red-50 p-4 rounded-md">
                <p class="text-sm text-red-700">{{ error }}</p>
              </div>
              <div v-if="success" class="bg-green-50 p-4 rounded-md">
                <p class="text-sm text-green-700">{{ success }}</p>
              </div>

              <!-- Partie Mère (OF) -->
              <div class="grid grid-cols-6 gap-4">
                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Date</label>
                  <input v-model="mere.daty" type="date" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Cible</label>
                  <select
                    v-model="mere.cible"
                    required
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white py-2 px-3 shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  >
                    <option value="" disabled>-- Sélectionner un magasin --</option>
                    <option
                      v-for="mag in magasins"
                      :key="mag.id"
                      :value="mag.id"
                    >
                      {{ mag.id }} - {{ mag.libelle }}
                    </option>
                  </select>
                </div>

                <div class="col-span-6">
                  <label class="block text-sm font-medium text-gray-700">Désignation</label>
                  <input v-model="mere.libelle" type="text" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6">
                  <label class="block text-sm font-medium text-gray-700">Remarque</label>
                  <textarea v-model="mere.remarque" rows="2" class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md" />
                </div>
              </div>

              <!-- Partie Filles (lignes produits / ingrédients) -->
              <div class="mt-8">
                <h4 class="text-md font-medium text-gray-900 mb-3">Produits</h4>

                <div class="mb-4">
                  <label class="block text-sm font-medium text-gray-700">Entrez le chemin de votre fichier Excel</label>
                  <input
                    type="file"
                    class="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                  />
                </div>

                <datalist id="ingredients-of-list">
                  <option
                    v-for="ing in ingredients"
                    :key="ing.id"
                    :value="ing.id"
                  >
                    {{ ing.id }} - {{ ing.libelle }}<span v-if="ing.unite"> - {{ ing.unite }}</span>
                  </option>
                </datalist>

                <div class="overflow-x-auto border border-gray-200 rounded-md bg-white">
                  <table class="min-w-full divide-y divide-gray-200 text-xs">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="px-3 py-2 text-left font-medium text-gray-500">Produits</th>
                        <th class="px-3 py-2 text-left font-medium text-gray-500">Libellé fille</th>
                        <th class="px-3 py-2 text-left font-medium text-gray-500">Unité</th>
                        <th class="px-3 py-2 text-left font-medium text-gray-500">Quantité</th>
                        <th class="px-3 py-2 text-left font-medium text-gray-500">Remarque</th>
                      </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-100">
                      <tr v-for="(f, index) in filles" :key="index">
                        <td class="px-3 py-2 align-top">
                          <input
                            v-model="f.idIngredients"
                            list="ingredients-of-list"
                            type="text"
                            required
                            @change="handleIngredientChange(index)"
                            class="block w-full rounded-md border border-gray-300 px-2 py-1 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                          >
                        </td>
                        <td class="px-3 py-2 align-top">
                          <input
                            v-model="f.libelle"
                            type="text"
                            class="block w-full rounded-md border border-gray-300 px-2 py-1 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                          >
                        </td>
                        <td class="px-3 py-2 align-top">
                          <input
                            v-model="f.idunite"
                            type="text"
                            class="block w-full rounded-md border border-gray-300 px-2 py-1 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                          >
                        </td>
                        <td class="px-3 py-2 align-top">
                          <input
                            v-model.number="f.qte"
                            type="number"
                            class="block w-full rounded-md border border-gray-300 px-2 py-1 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                          >
                        </td>
                        <td class="px-3 py-2 align-top text-gray-400 text-[11px] italic">
                          <!-- Remarque colonne réservée si besoin -->
                          -
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div class="mt-3 flex justify-end">
                  <button
                    type="button"
                    @click="addFille"
                    class="inline-flex items-center px-3 py-1 border border-indigo-500 text-xs font-medium rounded-md text-indigo-700 bg-white hover:bg-indigo-50"
                  >
                    + Ajouter une ligne
                  </button>
                </div>
              </div>
            </div>

            <div class="mt-6 flex justify-end space-x-3">
              <button type="button" @click="router.back()" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                Annuler
              </button>
              <button type="submit" :disabled="loading" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                {{ loading ? 'Enregistrement...' : 'Créer l\'OF' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
