<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const form = ref({
  daty: new Date().toISOString().split('T')[0],
  lancePar: 'PHARM002',
  cible: 'PHARM002',
  libelle: 'Test Creation Neuve',
  idBonDeCommande: '',
  ordreDeFab: 'OF001059',
  filles: [
    {
      idIngredients: 'ING000T0129',
      qte: 75600,
      idunite: 'UNT001',
      idMachine: 'MACHN000004',
      libelle: 'Test Creation Neuve'
    }
  ]
})

const addFille = () => {
  form.value.filles.push({
    idIngredients: '',
    qte: 0,
    idunite: 'UNT001',
    idMachine: 'MACHN000004',
    libelle: form.value.libelle
  })
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
</script>

<template>
  <div class="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
    <div class="md:grid md:grid-cols-3 md:gap-6">
      <div class="md:col-span-1">
        <div class="px-4 sm:px-0">
          <h3 class="text-lg font-medium leading-6 text-gray-900">Nouvelle Fabrication</h3>
          <p class="mt-1 text-sm text-gray-600">
            Lancer un nouvel ordre de fabrication (OF).
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

              <div class="grid grid-cols-6 gap-6">
                <!-- Informations Générales -->
                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Date</label>
                  <input v-model="form.daty" type="date" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Lancé par</label>
                  <input v-model="form.lancePar" type="text" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Cible</label>
                  <input v-model="form.cible" type="text" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Machine</label>
                  <input v-model="form.idMachine" type="text" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6">
                  <label class="block text-sm font-medium text-gray-700">Libellé</label>
                  <input v-model="form.libelle" type="text" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <!-- Références -->
                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">ID Bon de Commande</label>
                  <input v-model="form.idBonDeCommande" type="text" class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Ordre de Fab</label>
                  <input v-model="form.ordreDeFab" type="text" class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <!-- Produits / Ingrédients Filles -->
                <div class="col-span-6">
                  <div class="flex items-center justify-between mb-4">
                    <h4 class="text-md font-medium text-gray-900">Produits / Ingrédients</h4>
                    <button type="button" @click="addFille" class="inline-flex items-center px-3 py-1 border border-transparent text-sm font-medium rounded-md text-green-700 bg-green-100 hover:bg-green-200">
                      + Ajouter un produit
                    </button>
                  </div>
                  
                  <div v-for="(fille, index) in form.filles" :key="index" class="p-4 border border-gray-200 rounded-md mb-4 bg-gray-50">
                    <div class="grid grid-cols-6 gap-4">
                      <div class="col-span-6 sm:col-span-3">
                        <label class="block text-sm font-medium text-gray-700">Ingrédient / Produit</label>
                        <input v-model="fille.idIngredients" type="text" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                      </div>

                      <div class="col-span-6 sm:col-span-2">
                        <label class="block text-sm font-medium text-gray-700">Quantité</label>
                        <input v-model.number="fille.qte" type="number" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                      </div>

                      <div class="col-span-6 sm:col-span-1">
                        <label class="block text-sm font-medium text-gray-700">Unité</label>
                        <input v-model="fille.idunite" type="text" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                      </div>

                      <div class="col-span-6 sm:col-span-3">
                        <label class="block text-sm font-medium text-gray-700">Machine</label>
                        <input v-model="fille.idMachine" type="text" required class="mt-1 focus:ring-green-500 focus:border-green-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                      </div>

                      <div class="col-span-6 sm:col-span-3 flex items-end justify-end">
                        <button v-if="form.filles.length > 1" type="button" @click="removeFille(index)" class="text-red-600 hover:text-red-800 text-sm font-medium">
                          Supprimer cette ligne
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="px-4 py-3 bg-gray-50 text-right sm:px-6 space-x-3">
              <button type="button" @click="router.back()" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                Annuler
              </button>
              <button type="submit" :disabled="loading" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500">
                {{ loading ? 'Lancement...' : 'Lancer la fabrication' }}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
