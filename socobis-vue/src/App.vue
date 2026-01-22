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
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <!-- Navbar -->
    <nav v-if="route.path !== '/login'" class="bg-white border-b border-slate-200 sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <!-- Logo -->
            <router-link to="/" class="flex-shrink-0 flex items-center gap-2">
              <div class="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clip-rule="evenodd" />
                </svg>
              </div>
              <span class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-600 to-violet-600">
                SOCOBIS
              </span>
            </router-link>

            <!-- Desktop Links -->
            <div class="hidden md:ml-8 md:flex md:space-x-4">
              <router-link
                to="/"
                class="px-3 py-2 rounded-lg text-sm font-medium transition-all"
                :class="[route.path === '/' ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
              >
                Tableau de bord
              </router-link>
              <router-link
                to="/compteurs"
                class="px-3 py-2 rounded-lg text-sm font-medium transition-all"
                :class="[route.path.startsWith('/compteurs') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
              >
                Compteurs
              </router-link>
              <router-link
                to="/fabrications"
                class="px-3 py-2 rounded-lg text-sm font-medium transition-all"
                :class="[route.path.startsWith('/fabrications') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
              >
                Fabrications
              </router-link>
              <router-link
                to="/ofs"
                class="px-3 py-2 rounded-lg text-sm font-medium transition-all"
                :class="[route.path.startsWith('/ofs') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
              >
                Ordres de Fab
              </router-link>
            </div>
          </div>

          <!-- Right Side -->
          <div class="hidden md:flex items-center">
            <button
              @click="handleLogout"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-slate-600 hover:bg-red-50 hover:text-red-600 transition-all gap-2"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              Déconnexion
            </button>
          </div>

          <!-- Mobile menu button -->
          <div class="flex items-center md:hidden">
            <button
              @click="isMobileMenuOpen = !isMobileMenuOpen"
              class="inline-flex items-center justify-center p-2 rounded-lg text-slate-400 hover:text-slate-500 hover:bg-slate-100 focus:outline-none transition-all"
            >
              <svg :class="[isMobileMenuOpen ? 'hidden' : 'block', 'h-6 w-6']" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg :class="[isMobileMenuOpen ? 'block' : 'hidden', 'h-6 w-6']" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Mobile menu -->
      <div v-show="isMobileMenuOpen" class="md:hidden border-t border-slate-200 bg-white">
        <div class="pt-2 pb-3 space-y-1 px-4">
          <router-link
            to="/"
            @click="isMobileMenuOpen = false"
            class="block px-3 py-2 rounded-lg text-base font-medium transition-all"
            :class="[route.path === '/' ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            Tableau de bord
          </router-link>
          <router-link
            to="/compteurs"
            @click="isMobileMenuOpen = false"
            class="block px-3 py-2 rounded-lg text-base font-medium transition-all"
            :class="[route.path.startsWith('/compteurs') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            Compteurs
          </router-link>
          <router-link
            to="/fabrications"
            @click="isMobileMenuOpen = false"
            class="block px-3 py-2 rounded-lg text-base font-medium transition-all"
            :class="[route.path.startsWith('/fabrications') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            Fabrications
          </router-link>
          <router-link
            to="/ofs"
            @click="isMobileMenuOpen = false"
            class="block px-3 py-2 rounded-lg text-base font-medium transition-all"
            :class="[route.path.startsWith('/ofs') ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900']"
          >
            Ordres de Fab
          </router-link>
          <button
            @click="handleLogout"
            class="w-full text-left px-3 py-2 rounded-lg text-base font-medium text-slate-600 hover:bg-red-50 hover:text-red-600 transition-all"
          >
            Déconnexion
          </button>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main>
      <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <RouterView v-slot="{ Component }">
          <transition
            enter-active-class="transition ease-out duration-200"
            enter-from-class="opacity-0 translate-y-4"
            enter-to-class="opacity-100 translate-y-0"
            leave-active-class="transition ease-in duration-150"
            leave-from-class="opacity-100 translate-y-0"
            leave-to-class="opacity-0 translate-y-4"
            mode="out-in"
          >
            <component :is="Component" />
          </transition>
        </RouterView>
      </div>
    </main>
  </div>
</template>

<style>
@reference "tailwindcss";

body {
  margin: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  @apply bg-slate-50 text-slate-900;
}

.router-link-active {
  @apply font-semibold;
}
</style>
