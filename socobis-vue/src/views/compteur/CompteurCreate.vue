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
  <div class="min-h-screen bg-slate-50/50 pb-12">
    <!-- Header -->
    <div class="bg-white border-b border-slate-200 sticky top-0 z-10 backdrop-blur-md bg-white/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center gap-4">
            <button 
              @click="router.back()" 
              class="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500 hover:text-slate-700"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </button>
            <h1 class="text-xl font-bold text-slate-900">Nouveau relevé de compteur</h1>
          </div>
        </div>
      </div>
    </div>

    <main class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <form @submit.prevent="submitForm">
          <div class="p-6 sm:p-8 space-y-8">
            <!-- Alert Error -->
            <transition enter-active-class="transition duration-200 ease-out" enter-from-class="transform -translate-y-2 opacity-0" enter-to-class="transform translate-y-0 opacity-100">
              <div v-if="error" class="bg-red-50 border border-red-100 rounded-xl p-4 flex gap-3 items-start">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-red-500 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                </svg>
                <p class="text-sm text-red-700 font-medium">{{ error }}</p>
              </div>
            </transition>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
              <!-- Section Infos Machine -->
              <div class="space-y-6">
                <h3 class="text-sm font-semibold text-slate-400 uppercase tracking-wider">Informations Machine</h3>
                
                <div class="space-y-2">
                  <label class="block text-sm font-medium text-slate-700">Machine</label>
                  <input 
                    v-model="form.idMachine" 
                    type="text" 
                    required 
                    class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                  >
                </div>

                <div class="space-y-2">
                  <label class="block text-sm font-medium text-slate-700">Relevé (Nombre)</label>
                  <input 
                    v-model.number="form.nombre" 
                    type="number" 
                    required 
                    class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                  >
                </div>
              </div>

              <!-- Section Temps -->
              <div class="space-y-6">
                <h3 class="text-sm font-semibold text-slate-400 uppercase tracking-wider">Date & Heure</h3>
                
                <div class="grid grid-cols-2 gap-4">
                  <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700">Date</label>
                    <input 
                      v-model="form.daty" 
                      type="date" 
                      required 
                      class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                    >
                  </div>
                  <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700">Heure</label>
                    <input 
                      v-model="form.heure" 
                      type="text" 
                      placeholder="HH:mm" 
                      required 
                      class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                    >
                  </div>
                </div>
              </div>

              <!-- Section Liens -->
              <div class="md:col-span-2 space-y-6 pt-4 border-t border-slate-100">
                <h3 class="text-sm font-semibold text-slate-400 uppercase tracking-wider">Références</h3>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700">ID Fabrication</label>
                    <input 
                      v-model="form.idFabrication" 
                      type="text" 
                      class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                    >
                  </div>

                  <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700">ID Origine</label>
                    <input 
                      v-model="form.idOrigine" 
                      type="text" 
                      class="block w-full px-4 py-3 rounded-xl border-slate-200 bg-slate-50 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer Actions -->
          <div class="px-8 py-6 bg-slate-50 border-t border-slate-200 flex flex-col sm:flex-row justify-end gap-3">
            <button 
              type="button" 
              @click="router.back()" 
              class="px-6 py-2.5 rounded-xl border border-slate-200 bg-white text-sm font-semibold text-slate-600 hover:bg-slate-50 transition-colors"
            >
              Annuler
            </button>
            <button 
              type="submit" 
              :disabled="loading" 
              class="px-6 py-2.5 rounded-xl bg-indigo-600 text-sm font-semibold text-white hover:bg-indigo-700 focus:ring-4 focus:ring-indigo-500/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <svg v-if="loading" class="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ loading ? 'Enregistrement...' : 'Enregistrer le relevé' }}
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>
