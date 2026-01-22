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
  <div class="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="flex justify-center">
        <div class="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center text-white text-3xl font-bold shadow-xl shadow-blue-200">
          S
        </div>
      </div>
      <h2 class="mt-6 text-center text-3xl font-extrabold text-slate-900 tracking-tight">
        Bienvenue chez SOCOBIS
      </h2>
      <p class="mt-2 text-center text-sm text-slate-600">
        Connectez-vous à votre espace de gestion
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow-2xl shadow-slate-200 sm:rounded-2xl sm:px-10 border border-slate-100">
        <form class="space-y-6" @submit.prevent="handleLogin">
          <div>
            <label for="identifiant" class="block text-sm font-semibold text-slate-700">
              Identifiant
            </label>
            <div class="mt-1 relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-slate-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                </svg>
              </div>
              <input 
                id="identifiant" 
                v-model="form.identifiant" 
                name="identifiant" 
                type="text" 
                required 
                class="appearance-none block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-xl shadow-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent sm:text-sm transition-all"
                placeholder="votre-identifiant"
              />
            </div>
          </div>

          <div>
            <label for="password" class="block text-sm font-semibold text-slate-700">
              Mot de passe
            </label>
            <div class="mt-1 relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-slate-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
                </svg>
              </div>
              <input 
                id="password" 
                v-model="form.passe" 
                name="password" 
                type="password" 
                required 
                class="appearance-none block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-xl shadow-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent sm:text-sm transition-all"
                placeholder="••••••••"
              />
            </div>
          </div>

          <div v-if="error" class="rounded-xl bg-red-50 p-4 border border-red-100 animate-pulse">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-red-800">
                  {{ error }}
                </p>
              </div>
            </div>
          </div>

          <div>
            <button 
              type="submit" 
              :disabled="loading"
              class="w-full flex justify-center py-3 px-4 border border-transparent rounded-xl shadow-lg text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ loading ? 'Connexion en cours...' : 'Se connecter' }}
            </button>
          </div>
        </form>
      </div>
      
      <p class="mt-8 text-center text-xs text-slate-500">
        &copy; {{ new Date().getFullYear() }} SOCOBIS. Tous droits réservés.
      </p>
    </div>
  </div>
</template>
