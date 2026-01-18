<script setup lang="ts">
import { ref } from 'vue'
import { RouterView, useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const isSidebarOpen = ref(true)

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
  <div class="min-h-screen bg-slate-50 font-sans text-slate-900 flex overflow-hidden">
    <!-- Sidebar -->
    <aside 
      v-if="route.path !== '/login'"
      :class="[
        'fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-slate-200 transition-transform duration-300 transform lg:relative lg:translate-x-0',
        isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
    >
      <div class="flex flex-col h-full">
        <!-- Logo -->
        <div class="h-16 flex items-center px-6 border-b border-slate-100">
          <router-link to="/" class="group flex items-center gap-3">
            <div class="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center text-white font-bold shadow-lg shadow-indigo-200 group-hover:scale-105 transition-transform">
              S
            </div>
            <span class="text-xl font-bold tracking-tight text-slate-900">
              SOCOBIS
            </span>
          </router-link>
        </div>

        <!-- Navigation Links -->
        <nav class="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
          <router-link 
            to="/" 
            class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all duration-200 group"
            :class="[$route.path === '/' ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            Tableau de bord
          </router-link>

          <router-link 
            to="/compteurs" 
            class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all duration-200 group"
            :class="[$route.path.startsWith('/compteurs') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
            </svg>
            Compteurs
          </router-link>

          <router-link 
            to="/fabrications" 
            class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all duration-200 group"
            :class="[$route.path.startsWith('/fabrications') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
            Fabrications
          </router-link>

          <router-link 
            to="/ofs" 
            class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all duration-200 group"
            :class="[$route.path.startsWith('/ofs') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
            Ordres de Fabrication
          </router-link>
        </nav>

        <!-- User Profile / Logout -->
        <div class="p-4 border-t border-slate-100">
          <button 
            @click="handleLogout" 
            class="flex items-center gap-3 w-full px-4 py-3 rounded-xl text-sm font-semibold text-slate-600 hover:bg-red-50 hover:text-red-600 transition-all group"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            Déconnexion
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Top Mobile Header -->
      <header v-if="route.path !== '/login'" class="lg:hidden h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 shrink-0">
        <button @click="isSidebarOpen = !isSidebarOpen" class="p-2 text-slate-600">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16m-7 6h7" />
          </svg>
        </button>
        <span class="font-bold text-slate-900">SOCOBIS</span>
        <div class="w-10"></div> <!-- Spacer -->
      </header>

      <!-- Main Content Scroll Area -->
      <main class="flex-1 overflow-y-auto bg-slate-50/50 relative">
        <div class="max-w-7xl mx-auto">
          <RouterView v-slot="{ Component }">
            <transition 
              enter-active-class="transition duration-300 ease-out"
              enter-from-class="transform translate-y-4 opacity-0"
              enter-to-class="transform translate-y-0 opacity-100"
              leave-active-class="transition duration-200 ease-in"
              leave-from-class="transform translate-y-0 opacity-100"
              leave-to-class="transform translate-y-4 opacity-0"
              mode="out-in"
            >
              <component :is="Component" />
            </transition>
          </RouterView>
        </div>

        <!-- Sticky Footer -->
        <footer v-if="route.path !== '/login'" class="py-8 px-8 border-t border-slate-200 bg-white/50 mt-12">
          <p class="text-sm text-slate-500 text-center lg:text-left">
            &copy; {{ new Date().getFullYear() }} SOCOBIS - Système de Gestion Intégré
          </p>
        </footer>
      </main>
    </div>

    <!-- Mobile Overlay -->
    <div 
      v-if="isSidebarOpen && route.path !== '/login'" 
      @click="isSidebarOpen = false"
      class="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-40 lg:hidden"
    ></div>
  </div>
</template>

<style>
@import "tailwindcss";

:root {
  --font-sans: 'Inter', system-ui, -apple-system, sans-serif;
}

body {
  margin: 0;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Custom Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
