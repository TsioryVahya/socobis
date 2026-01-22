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
  <div class="min-h-screen bg-gray-100">
    <div v-if="route.path !== '/login'" class="min-h-screen flex flex-col">
      <!-- Navbar horizontale -->
      <nav class="bg-blue-600 shadow-lg">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex justify-between h-16">
            <div class="flex items-center space-x-8">
              <!-- Logo -->
              <router-link to="/" class="text-2xl font-bold text-white tracking-tight hover:text-blue-100 transition">
                SOCOBIS
              </router-link>

              <!-- Navigation links -->
              <div class="hidden md:flex space-x-1">
                <router-link
                  to="/"
                  class="rounded-md px-3 py-2 text-sm font-medium transition"
                  :class="[$route.path === '/' ? 'bg-blue-700 text-white' : 'text-blue-100 hover:bg-blue-500 hover:text-white']"
                >
                  Tableau de bord
                </router-link>
                <router-link
                  to="/compteurs"
                  class="rounded-md px-3 py-2 text-sm font-medium transition"
                  :class="[$route.path.startsWith('/compteurs') ? 'bg-blue-700 text-white' : 'text-blue-100 hover:bg-blue-500 hover:text-white']"
                >
                  Compteurs
                </router-link>
                <router-link
                  to="/fabrications"
                  class="rounded-md px-3 py-2 text-sm font-medium transition"
                  :class="[$route.path.startsWith('/fabrications') ? 'bg-blue-700 text-white' : 'text-blue-100 hover:bg-blue-500 hover:text-white']"
                >
                  Fabrications
                </router-link>
                <router-link
                  to="/ofs"
                  class="rounded-md px-3 py-2 text-sm font-medium transition"
                  :class="[$route.path.startsWith('/ofs') ? 'bg-blue-700 text-white' : 'text-blue-100 hover:bg-blue-500 hover:text-white']"
                >
                  Ordres de Fab
                </router-link>
              </div>
            </div>

            <!-- Bouton déconnexion -->
            <div class="flex items-center">
              <button
                @click="handleLogout"
                class="rounded-md px-4 py-2 text-sm font-medium text-white bg-blue-700 hover:bg-red-600 transition"
              >
                Déconnexion
              </button>
            </div>
          </div>
        </div>
      </nav>

      <main class="flex-1">
        <RouterView />
      </main>
    </div>

    <main v-else>
      <RouterView />
    </main>
  </div>
</template>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}
</style>
