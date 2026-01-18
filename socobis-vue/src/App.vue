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
      <aside class="w-64 bg-white border-r border-gray-200 shadow-sm flex flex-col">
        <div class="h-16 flex items-center px-6 border-b border-gray-200">
          <router-link to="/" class="text-xl font-bold text-blue-600 tracking-tight">
            SOCOBIS
          </router-link>
        </div>

        <nav class="flex-1 px-3 py-4 space-y-1">
          <router-link
            to="/"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path === '/' ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50']"
          >
            Tableau de bord
          </router-link>
          <router-link
            to="/compteurs"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path.startsWith('/compteurs') ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50']"
          >
            Compteurs
          </router-link>
          <router-link
            to="/fabrications"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path.startsWith('/fabrications') ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50']"
          >
            Fabrications
          </router-link>
          <router-link
            to="/ofs"
            class="block rounded-md px-3 py-2 text-sm font-medium"
            :class="[$route.path.startsWith('/ofs') ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50']"
          >
            Ordres de Fab
          </router-link>
        </nav>

        <div class="p-3 border-t border-gray-200">
          <button
            @click="handleLogout"
            class="w-full text-left rounded-md px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
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
