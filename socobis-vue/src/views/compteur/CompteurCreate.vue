<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const error = ref<string | null>(null)

const form = ref({
  idMachine: 'MACHN000001',
  nombre: 1501,
  daty: '2026-01-11',
  heure: '10:30',
  idFabrication: 'FAB002944',
  idOrigine: 'PHARM002'
})

const submitForm = async () => {
  loading.value = true
  error.value = null
  try {
    // Utilisation de l'action 'create' (par défaut dans notre servlet modifiée)
    const response = await axios.post('/CompteurServlet', form.value)
    if (response.status === 200) {
      router.push('/compteurs')
    }
  } catch (err: any) {
    console.error(err)
    error.value = err.response?.data || "Erreur lors de la création du compteur. Vérifiez que la valeur est supérieure au dernier compteur."
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto py-6 sm:px-6 lg:px-8">
    <div class="md:grid md:grid-cols-3 md:gap-6">
      <div class="md:col-span-1">
        <div class="px-4 sm:px-0">
          <h3 class="text-lg font-medium leading-6 text-gray-900">Nouveau Compteur</h3>
          <p class="mt-1 text-sm text-gray-600">
            Saisissez les informations du relevé de compteur.
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

              <div class="grid grid-cols-6 gap-6">
                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Machine</label>
                  <input v-model="form.idMachine" type="text" required class="mt-1 focus:ring-blue-500 focus:border-blue-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Nombre</label>
                  <input v-model.number="form.nombre" type="number" required class="mt-1 focus:ring-blue-500 focus:border-blue-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Date</label>
                  <input v-model="form.daty" type="date" required class="mt-1 focus:ring-blue-500 focus:border-blue-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">Heure</label>
                  <input v-model="form.heure" type="text" placeholder="HH:mm" required class="mt-1 focus:ring-blue-500 focus:border-blue-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">ID Fabrication</label>
                  <input v-model="form.idFabrication" type="text" class="mt-1 focus:ring-blue-500 focus:border-blue-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label class="block text-sm font-medium text-gray-700">ID Origine</label>
                  <input v-model="form.idOrigine" type="text" class="mt-1 focus:ring-blue-500 focus:border-blue-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                </div>
              </div>
            </div>
            <div class="px-4 py-3 bg-gray-50 text-right sm:px-6 space-x-3">
              <button type="button" @click="router.back()" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                Annuler
              </button>
              <button type="submit" :disabled="loading" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                {{ loading ? 'Enregistrement...' : 'Enregistrer' }}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
