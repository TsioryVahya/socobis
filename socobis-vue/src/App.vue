<script setup lang="ts">
import { RouterView, useRouter, useRoute } from 'vue-router'

import axios from 'axios'

const router = useRouter()
const route = useRoute()

const handleLogout = async () => {
  try {
    await axios.delete('/LoginServlet')
  } catch (err) {
    console.error("Erreur lors de la déconnexion côté serveur", err)
  }
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-[#f8fafc] flex">
    <!-- Sidebar -->
    <aside v-if="route.path !== '/login'" class="w-64 bg-[#1e293b] text-white flex-shrink-0 flex flex-col transition-all duration-300 ease-in-out shadow-xl">
      <div class="p-6 flex items-center space-x-3">
        <div class="w-8 h-8 bg-indigo-500 rounded-lg flex items-center justify-center shadow-lg shadow-indigo-500/50">
          <span class="font-bold text-white">S</span>
        </div>
        <span class="text-xl font-bold tracking-wider text-indigo-100">SOCOBIS</span>
      </div>

      <nav class="flex-1 mt-6 px-4 space-y-2">
        <router-link 
          to="/" 
          class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group"
          :class="[$route.path === '/' ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30' : 'text-slate-400 hover:bg-slate-800 hover:text-white']"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
          <span class="font-medium">Dashboard</span>
        </router-link>

        <router-link 
          to="/compteurs" 
          class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group"
          :class="[$route.path.startsWith('/compteurs') ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30' : 'text-slate-400 hover:bg-slate-800 hover:text-white']"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <span class="font-medium">Compteurs</span>
        </router-link>

        <router-link 
          to="/fabrications" 
          class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group"
          :class="[$route.path.startsWith('/fabrications') ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30' : 'text-slate-400 hover:bg-slate-800 hover:text-white']"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          <span class="font-medium">Fabrications</span>
        </router-link>

        <router-link 
          to="/ofs" 
          class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group"
          :class="[$route.path.startsWith('/ofs') ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30' : 'text-slate-400 hover:bg-slate-800 hover:text-white']"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012-2" />
          </svg>
          <span class="font-medium">Ordres de Fab</span>
        </router-link>
      </nav>

      <div class="p-4 mt-auto">
        <button 
          @click="handleLogout" 
          class="w-full flex items-center space-x-3 px-4 py-3 rounded-xl text-slate-400 hover:bg-red-500/10 hover:text-red-400 transition-all duration-200"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          <span class="font-medium">Déconnexion</span>
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Top header for mobile/content info -->
      <header v-if="route.path !== '/login'" class="bg-white border-b border-slate-200 h-16 flex items-center px-8 flex-shrink-0">
        <h2 class="text-lg font-semibold text-slate-800 capitalize">{{ route.name || route.path.split('/')[1] || 'Dashboard' }}</h2>
        <div class="ml-auto flex items-center space-x-4">
          <div class="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 font-bold">
            U
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}
</style>
