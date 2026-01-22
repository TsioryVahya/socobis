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
  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
    <!-- Navbar Horizontale -->
    <nav v-if="route.path !== '/login'" class="bg-gradient-to-r from-emerald-600 via-teal-600 to-cyan-600 shadow-2xl sticky top-0 z-50">
      <div class="max-w-full px-6">
        <div class="flex items-center justify-between h-20">
          <!-- Logo et Titre -->
          <div class="flex items-center space-x-4">
            <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center shadow-xl transform hover:scale-110 transition-transform duration-300">
              <span class="font-black text-2xl bg-gradient-to-r from-emerald-600 to-teal-600 bg-clip-text text-transparent">S</span>
            </div>
            <div class="flex flex-col">
              <span class="text-2xl font-black tracking-tight text-white drop-shadow-lg">SOCOBIS</span>
              <span class="text-xs text-emerald-100 font-medium tracking-wide">Gestion de Production</span>
            </div>
          </div>

          <!-- Navigation Links -->
          <div class="hidden md:flex items-center space-x-2">
            <router-link 
              to="/" 
              class="flex items-center space-x-2 px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:scale-105"
              :class="[$route.path === '/' ? 'bg-white text-emerald-700 shadow-xl' : 'text-white hover:bg-white/20']"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
              <span>Dashboard</span>
            </router-link>

            <router-link 
              to="/compteurs" 
              class="flex items-center space-x-2 px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:scale-105"
              :class="[$route.path.startsWith('/compteurs') ? 'bg-white text-emerald-700 shadow-xl' : 'text-white hover:bg-white/20']"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              <span>Compteurs</span>
            </router-link>

            <router-link 
              to="/fabrications" 
              class="flex items-center space-x-2 px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:scale-105"
              :class="[$route.path.startsWith('/fabrications') ? 'bg-white text-emerald-700 shadow-xl' : 'text-white hover:bg-white/20']"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              <span>Fabrications</span>
            </router-link>

            <router-link 
              to="/ofs" 
              class="flex items-center space-x-2 px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:scale-105"
              :class="[$route.path.startsWith('/ofs') ? 'bg-white text-emerald-700 shadow-xl' : 'text-white hover:bg-white/20']"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012-2" />
              </svg>
              <span>Ordres de Fab</span>
            </router-link>
          </div>

          <!-- User Avatar & Déconnexion -->
          <div class="flex items-center space-x-4">
            <div class="w-10 h-10 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center text-white font-bold shadow-lg">
              <span>U</span>
            </div>
            <button 
              @click="handleLogout" 
              class="flex items-center space-x-2 px-5 py-3 rounded-xl font-semibold text-white hover:bg-red-500 transition-all duration-300 transform hover:scale-105 shadow-lg"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              <span>Déconnexion</span>
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="min-h-[calc(100vh-5rem)]">
      <div v-if="route.path !== '/login'" class="container mx-auto px-6 py-8">
        <RouterView />
      </div>
      <div v-else>
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}
</style>
