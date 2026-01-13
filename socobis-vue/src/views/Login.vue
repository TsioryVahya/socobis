<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const error = ref<string | null>(null)

const form = ref({
  identifiant: 'admin',
  passe: 'test',
  interim: '0',
  service: ''
})

const handleLogin = async () => {
  loading.value = true
  error.value = null
  try {
    // On envoie les données en tant que paramètres d'URL pour correspondre au backend Java
    const params = new URLSearchParams()
    params.append('identifiant', form.value.identifiant)
    params.append('passe', form.value.passe)
    params.append('interim', form.value.interim)
    params.append('service', form.value.service)

    const response = await axios.post('/LoginServlet', params)
    
    if (response.data.status === 'success') {
      localStorage.setItem('user', JSON.stringify(response.data.user))
      router.push('/')
    } else {
      error.value = response.data.message
    }
  } catch (err: any) {
    console.error(err)
    error.value = err.response?.data?.message || "Erreur d'authentification. Vérifiez vos identifiants."
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        Connexion à SOCOBIS
      </h2>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <form class="space-y-6" @submit.prevent="handleLogin">
          <div v-if="error" class="bg-red-50 p-4 rounded-md">
            <p class="text-sm text-red-700">{{ error }}</p>
          </div>

          <div>
            <label for="identifiant" class="block text-sm font-medium text-gray-700">
              Identifiant
            </label>
            <div class="mt-1">
              <input
                v-model="form.identifiant"
                id="identifiant"
                name="identifiant"
                type="text"
                required
                readonly
                class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm bg-gray-100 text-gray-700 cursor-not-allowed sm:text-sm"
              >
            </div>
          </div>

          <div>
            <label for="passe" class="block text-sm font-medium text-gray-700">
              Mot de passe
            </label>
            <div class="mt-1">
              <input
                v-model="form.passe"
                id="passe"
                name="passe"
                type="password"
                required
                readonly
                class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm bg-gray-100 text-gray-700 cursor-not-allowed sm:text-sm"
              >
            </div>
          </div>

          <div>
            <button type="submit" :disabled="loading" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              {{ loading ? 'Connexion en cours...' : 'Se connecter' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
