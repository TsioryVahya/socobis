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
  <div class="min-h-screen bg-[#0f172a] flex items-center justify-center p-6">
    <div class="w-full max-w-md">
      <!-- Logo / Title -->
      <div class="text-center mb-10">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-indigo-500 rounded-2xl shadow-xl shadow-indigo-500/20 mb-4">
          <span class="text-2xl font-bold text-white">S</span>
        </div>
        <h2 class="text-3xl font-bold text-white tracking-tight">SOCOBIS ERP</h2>
        <p class="text-slate-400 mt-2">Connectez-vous pour accéder à votre espace de gestion.</p>
      </div>

      <!-- Login Card -->
      <div class="bg-white/5 backdrop-blur-xl border border-white/10 p-8 rounded-3xl shadow-2xl">
        <form class="space-y-6" @submit.prevent="handleLogin">
          <div v-if="error" class="bg-red-500/10 border border-red-500/20 p-4 rounded-2xl">
            <p class="text-sm text-red-400 text-center font-medium">{{ error }}</p>
          </div>

          <div class="space-y-2">
            <label for="identifiant" class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">
              Identifiant
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <input
                v-model="form.identifiant"
                id="identifiant"
                type="text"
                required
                readonly
                class="w-full pl-11 pr-4 py-3 bg-white/5 border border-white/10 rounded-2xl text-white placeholder-slate-500 focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 outline-none transition-all cursor-not-allowed"
                placeholder="Votre identifiant"
              >
            </div>
          </div>

          <div class="space-y-2">
            <label for="passe" class="block text-xs font-semibold text-slate-400 uppercase tracking-wider ml-1">
              Mot de passe
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 00-2 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input
                v-model="form.passe"
                id="passe"
                type="password"
                required
                readonly
                class="w-full pl-11 pr-4 py-3 bg-white/5 border border-white/10 rounded-2xl text-white placeholder-slate-500 focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 outline-none transition-all cursor-not-allowed"
                placeholder="••••••••"
              >
            </div>
          </div>

          <button 
            type="submit" 
            :disabled="loading" 
            class="w-full py-4 px-6 bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl font-bold shadow-lg shadow-indigo-600/30 transition-all duration-200 transform active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading" class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Chargement...
            </span>
            <span v-else>Se connecter</span>
          </button>
        </form>
      </div>
      
      <p class="text-center text-slate-500 text-sm mt-8">
        © 2026 SOCOBIS. Tous droits réservés.
      </p>
    </div>
  </div>
</template>
