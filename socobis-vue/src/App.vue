<script setup lang="ts">
import { RouterView, useRouter, useRoute } from 'vue-router'
import { ref } from 'vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const isMobileMenuOpen = ref(false)

const handleLogout = async () => {
  try {
    await axios.delete('/LoginServlet')
  } catch (err) {
    console.error("Erreur lors de la déconnexion côté serveur", err)
  }
  localStorage.removeItem('user')
  router.push('/login')
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <!-- Navbar -->
    <nav v-if="route.path !== '/login'" class="bg-white border-b border-slate-200 sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <!-- Logo -->
            <router-link to="/" class="flex-shrink-0 flex items-center gap-2 group">
              <div class="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-200 group-hover:scale-105 transition-transform">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-10V4m0 10V4m-4 10h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M15 14h.01M12 17h.01M15 17h.01" />
                </svg>
              </div>
              <span class="text-xl font-bold text-slate-900 tracking-tight">SOCOBIS</span>
            </router-link>

            <!-- Desktop Navigation -->
            <div class="hidden md:ml-8 md:flex md:space-x-4">
              <router-link
                to="/"
                class="inline-flex items-center px-3 py-2 text-sm font-semibold rounded-lg transition-all"
                :class="[$route.path === '/' ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
              >
                Tableau de bord
              </router-link>
              <router-link
                to="/compteurs"
                class="inline-flex items-center px-3 py-2 text-sm font-semibold rounded-lg transition-all"
                :class="[$route.path.startsWith('/compteurs') ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
              >
                Compteurs
              </router-link>
              <router-link
                to="/fabrications"
                class="inline-flex items-center px-3 py-2 text-sm font-semibold rounded-lg transition-all"
                :class="[$route.path.startsWith('/fabrications') ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
              >
                Fabrications
              </router-link>
              <router-link
                to="/ofs"
                class="inline-flex items-center px-3 py-2 text-sm font-semibold rounded-lg transition-all"
                :class="[$route.path.startsWith('/ofs') ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
              >
                Ordres de Fab
              </router-link>
            </div>
          </div>

          <!-- Desktop Right Side -->
          <div class="hidden md:flex md:items-center md:space-x-4">
            <button
              @click="handleLogout"
              class="inline-flex items-center px-4 py-2 text-sm font-semibold text-red-600 bg-red-50 hover:bg-red-100 rounded-xl transition-all"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              Déconnexion
            </button>
          </div>

          <!-- Mobile menu button -->
          <div class="flex items-center md:hidden">
            <button
              @click="toggleMobileMenu"
              class="inline-flex items-center justify-center p-2 rounded-xl text-slate-500 hover:text-indigo-600 hover:bg-slate-100 focus:outline-none transition-all"
            >
              <svg v-if="!isMobileMenuOpen" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg v-else class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Mobile Navigation -->
      <transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0 -translate-y-4"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100 translate-y-0"
        leave-to-class="opacity-0 -translate-y-4"
      >
        <div v-if="isMobileMenuOpen" class="md:hidden bg-white border-b border-slate-200 shadow-xl">
          <div class="px-4 pt-2 pb-3 space-y-1">
            <router-link
              @click="isMobileMenuOpen = false"
              to="/"
              class="block px-4 py-3 text-base font-semibold rounded-xl transition-all"
              :class="[$route.path === '/' ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
            >
              Tableau de bord
            </router-link>
            <router-link
              @click="isMobileMenuOpen = false"
              to="/compteurs"
              class="block px-4 py-3 text-base font-semibold rounded-xl transition-all"
              :class="[$route.path.startsWith('/compteurs') ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
            >
              Compteurs
            </router-link>
            <router-link
              @click="isMobileMenuOpen = false"
              to="/fabrications"
              class="block px-4 py-3 text-base font-semibold rounded-xl transition-all"
              :class="[$route.path.startsWith('/fabrications') ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
            >
              Fabrications
            </router-link>
            <router-link
              @click="isMobileMenuOpen = false"
              to="/ofs"
              class="block px-4 py-3 text-base font-semibold rounded-xl transition-all"
              :class="[$route.path.startsWith('/ofs') ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50']"
            >
              Ordres de Fab
            </router-link>
            <div class="pt-4 pb-2 border-t border-slate-100">
              <button
                @click="handleLogout"
                class="w-full flex items-center px-4 py-3 text-base font-semibold text-red-600 bg-red-50 hover:bg-red-100 rounded-xl transition-all"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
                Déconnexion
              </button>
            </div>
          </div>
        </div>
      </transition>
    </nav>

    <!-- Main Content -->
    <main>
      <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <RouterView v-slot="{ Component }">
          <transition
            name="fade"
            mode="out-in"
            enter-active-class="transition duration-200 ease-out"
            enter-from-class="opacity-0 translate-y-2"
            enter-to-class="opacity-100 translate-y-0"
            leave-active-class="transition duration-150 ease-in"
            leave-from-class="opacity-100 translate-y-0"
            leave-to-class="opacity-0 translate-y-2"
          >
            <component :is="Component" />
          </transition>
        </RouterView>
      </div>
    </main>
  </div>
</template>

<style>
body {
  margin: 0;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  -webkit-font-smoothing: antialiased;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
