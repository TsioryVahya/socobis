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
    <div v-if="route.path !== '/login'" class="min-h-screen flex">
      <aside class="w-64 bg-gray-800 border-r border-gray-700 shadow-sm flex flex-col">
        <div class="h-16 flex items-center px-6 border-b border-gray-700">
          <router-link to="/" class="text-xl font-bold text-white tracking-tight">
            APP VUE
          </router-link>
        </div>

        <nav class="flex-1 px-3 py-4 space-y-1">
          <router-link
            to="/"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path === '/' ? 'bg-gray-900 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white']"
          >
            Tableau de bord
          </router-link>
          <router-link
            to="/compteurs"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path.startsWith('/compteurs') ? 'bg-gray-900 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white']"
          >
            Compteurs
          </router-link>
          <router-link
            to="/fabrications"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path.startsWith('/fabrications') ? 'bg-gray-900 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white']"
          >
            Fabrications
          </router-link>
          <router-link
            to="/ofs"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path.startsWith('/ofs') ? 'bg-gray-900 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white']"
          >
            Ordres de Fab
          </router-link>
        </nav>

        <div class="p-3 border-t border-gray-700">
          <button
            @click="handleLogout"
            class="w-full text-left rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-red-600 hover:text-white"
          >
            Déconnexion
          </button>
        </div>
      </aside>

      <main class="flex-1 min-w-0">
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
