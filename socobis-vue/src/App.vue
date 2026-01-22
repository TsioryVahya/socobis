<template>
  <div class="min-h-screen bg-slate-50 font-sans text-slate-900 flex overflow-hidden">
    <!-- Sidebar -->
    <aside 
      v-if="route.path !== '/login'"
      :class="[
        'fixed inset-y-0 left-0 z-50 w-72 bg-gradient-to-b from-white to-slate-50 border-r border-slate-200/80 shadow-xl transition-all duration-300 transform lg:relative lg:translate-x-0',
        isSidebarOpen ? 'translate-x-0 shadow-2xl' : '-translate-x-full'
      ]"
    >
      <div class="flex flex-col h-full">
        <!-- Logo avec animation -->
        <div class="h-20 flex items-center px-6 border-b border-slate-100 bg-white/50 backdrop-blur-sm">
          <router-link 
            to="/" 
            class="group flex items-center space-x-3 transition-all duration-300 hover:scale-[1.02]"
          >
            <div class="relative">
              <div class="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center text-white font-bold shadow-lg shadow-indigo-300/50 group-hover:shadow-indigo-400/70 transition-all duration-300">
                S
              </div>
              <div class="absolute -inset-1 bg-gradient-to-r from-indigo-400 to-purple-400 rounded-xl blur opacity-20 group-hover:opacity-30 transition-opacity duration-300"></div>
            </div>
            <div class="flex flex-col">
              <span class="text-xl font-bold tracking-tight text-slate-900 group-hover:text-indigo-700 transition-colors">
                SOCOBIS
              </span>
              <span class="text-xs text-slate-500 font-medium mt-0.5">
                Gestion Production
              </span>
            </div>
          </router-link>
        </div>

        <!-- User Info Quick View (Desktop) -->
        <div class="hidden lg:block px-6 py-4 border-b border-slate-100 bg-gradient-to-r from-indigo-50/50 to-white">
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 bg-gradient-to-br from-slate-600 to-slate-800 rounded-lg flex items-center justify-center text-white text-sm font-semibold shadow-sm">
              {{ getUserInitials() }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-slate-900 truncate">{{ getUserName() }}</p>
              <p class="text-xs text-slate-500 truncate">{{ getUserRole() }}</p>
            </div>
          </div>
        </div>

        <!-- Navigation Links -->
        <nav class="flex-1 px-4 py-6 space-y-1 overflow-y-auto custom-scrollbar">
          <div class="px-3 mb-4">
            <h3 class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Navigation Principale</h3>
          </div>
          
          <!-- Dashboard -->
          <router-link 
            to="/" 
            class="sidebar-nav-item group"
            :class="{ 'sidebar-nav-item-active': $route.path === '/' }"
          >
            <div class="sidebar-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
            <span>Tableau de bord</span>
            <div class="ml-auto w-2 h-2 rounded-full bg-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
          </router-link>

          <!-- Compteurs -->
          <router-link 
            to="/compteurs" 
            class="sidebar-nav-item group"
            :class="{ 'sidebar-nav-item-active': $route.path.startsWith('/compteurs') }"
          >
            <div class="sidebar-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
            </div>
            <span>Compteurs</span>
            <div class="ml-auto flex items-center space-x-1">
              <span class="text-xs font-semibold px-1.5 py-0.5 rounded-full bg-indigo-100 text-indigo-700">3</span>
              <div class="w-2 h-2 rounded-full bg-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
          </router-link>

          <!-- Fabrications -->
          <router-link 
            to="/fabrications" 
            class="sidebar-nav-item group"
            :class="{ 'sidebar-nav-item-active': $route.path.startsWith('/fabrications') }"
          >
            <div class="sidebar-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
            </div>
            <span>Fabrications</span>
            <div class="ml-auto w-2 h-2 rounded-full bg-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
          </router-link>

          <!-- Ordres de Fabrication -->
          <router-link 
            to="/ofs" 
            class="sidebar-nav-item group"
            :class="{ 'sidebar-nav-item-active': $route.path.startsWith('/ofs') }"
          >
            <div class="sidebar-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
              </svg>
            </div>
            <span>Ordres de Fabrication</span>
            <div class="ml-auto flex items-center space-x-1">
              <span class="text-xs font-semibold px-1.5 py-0.5 rounded-full bg-green-100 text-green-700">12</span>
              <div class="w-2 h-2 rounded-full bg-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
          </router-link>

          <!-- Separator -->
          <div class="px-3 my-6">
            <h3 class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Outils</h3>
          </div>

          <!-- Rapports -->
          <router-link 
            to="/rapports" 
            class="sidebar-nav-item group"
            :class="{ 'sidebar-nav-item-active': $route.path.startsWith('/rapports') }"
          >
            <div class="sidebar-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <span>Rapports</span>
            <div class="ml-auto w-2 h-2 rounded-full bg-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
          </router-link>

          <!-- Paramètres -->
          <router-link 
            to="/parametres" 
            class="sidebar-nav-item group"
            :class="{ 'sidebar-nav-item-active': $route.path.startsWith('/parametres') }"
          >
            <div class="sidebar-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <span>Paramètres</span>
            <div class="ml-auto w-2 h-2 rounded-full bg-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
          </router-link>
        </nav>

        <!-- User Profile / Logout -->
        <div class="p-4 border-t border-slate-100/50 bg-white/30 backdrop-blur-sm">
          <!-- User Info Mobile -->
          <div class="lg:hidden mb-4 px-2">
            <div class="flex items-center space-x-3 p-2 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer">
              <div class="w-8 h-8 bg-gradient-to-br from-slate-600 to-slate-800 rounded-lg flex items-center justify-center text-white text-sm font-semibold">
                {{ getUserInitials() }}
              </div>
              <div class="flex-1">
                <p class="text-sm font-semibold text-slate-900">{{ getUserName() }}</p>
                <p class="text-xs text-slate-500">{{ getUserRole() }}</p>
              </div>
            </div>
          </div>

          <button 
            @click="handleLogout" 
            class="sidebar-logout-btn group"
          >
            <div class="sidebar-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </div>
            <span>Déconnexion</span>
            <div class="ml-auto opacity-0 group-hover:opacity-100 transition-opacity">
              <svg class="h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
              </svg>
            </div>
          </button>

          <!-- Version info -->
          <div class="mt-4 pt-4 border-t border-slate-100/50">
            <p class="text-xs text-slate-400 text-center">
              v2.1.0 • © {{ new Date().getFullYear() }} SOCOBIS
            </p>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Top Mobile Header -->
      <header v-if="route.path !== '/login'" class="lg:hidden h-16 bg-white/80 backdrop-blur-sm border-b border-slate-200/80 flex items-center justify-between px-4 shrink-0 sticky top-0 z-40">
        <button @click="isSidebarOpen = !isSidebarOpen" class="p-2 rounded-lg text-slate-600 hover:bg-slate-100 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        
        <div class="flex items-center space-x-2">
          <div class="w-8 h-8 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center text-white font-bold text-sm shadow-sm">
            S
          </div>
          <span class="font-bold text-slate-900">SOCOBIS</span>
        </div>
        
        <!-- Quick Actions -->
        <div class="flex items-center space-x-2">
          <button class="p-2 rounded-lg text-slate-600 hover:bg-slate-100 transition-colors relative">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            <div class="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full border border-white"></div>
          </button>
        </div>
      </header>

      <!-- Main Content Scroll Area -->
      <main class="flex-1 overflow-y-auto bg-gradient-to-br from-slate-50 to-white relative">
        <!-- Background Pattern -->
        <div class="absolute inset-0 opacity-5 pattern-bg"></div>
        
        <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <RouterView v-slot="{ Component }">
            <transition 
              enter-active-class="transition duration-300 ease-out"
              enter-from-class="transform opacity-0"
              enter-to-class="transform opacity-100"
              leave-active-class="transition duration-200 ease-in"
              leave-from-class="transform opacity-100"
              leave-to-class="transform opacity-0"
              mode="out-in"
            >
              <component :is="Component" />
            </transition>
          </RouterView>
        </div>

        <!-- Sticky Footer -->
        <footer v-if="route.path !== '/login'" class="py-6 px-8 border-t border-slate-200/50 bg-white/50 backdrop-blur-sm mt-12 sticky bottom-0">
          <div class="max-w-7xl mx-auto flex flex-col lg:flex-row items-center justify-between gap-4">
            <p class="text-sm text-slate-500">
              &copy; {{ new Date().getFullYear() }} SOCOBIS - Système de Gestion de Production
            </p>
            <div class="flex items-center space-x-6 text-sm text-slate-500">
              <a href="#" class="hover:text-indigo-600 transition-colors">Confidentialité</a>
              <a href="#" class="hover:text-indigo-600 transition-colors">Conditions</a>
              <a href="#" class="hover:text-indigo-600 transition-colors">Support</a>
            </div>
          </div>
        </footer>
      </main>
    </div>

    <!-- Mobile Overlay -->
    <div 
      v-if="isSidebarOpen && route.path !== '/login'" 
      @click="isSidebarOpen = false"
      class="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-40 lg:hidden transition-all duration-300"
      :class="isSidebarOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterView, useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const isSidebarOpen = ref(true)

const getUserInitials = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const name = user.nom || 'Utilisateur'
  return name.charAt(0).toUpperCase()
}

const getUserName = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return user.nom || 'Utilisateur'
}

const getUserRole = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return user.role || 'Administrateur'
}

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

<style scoped>
/* Sidebar Navigation Items */
.sidebar-nav-item {
  display: flex;
  align-items: center;
  padding-left: 1rem;
  padding-right: 1rem;
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
  border-radius: 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  transition-property: all;
  transition-duration: 200ms;
  color: #475569;
}

.sidebar-nav-item:hover {
  color: #0f172a;
  background-color: white;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.sidebar-nav-item-active {
  background: linear-gradient(to right, #eef2ff, #f5f3ff);
  color: #4338ca;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #e0e7ff;
}

.sidebar-nav-item-active .sidebar-icon-wrapper {
  background-color: #e0e7ff;
  color: #4f46e5;
}

/* Sidebar Icon Wrapper */
.sidebar-icon-wrapper {
  width: 2.25rem;
  height: 2.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  transition-property: all;
  transition-duration: 200ms;
  background-color: #f1f5f9;
  color: #475569;
}

.group:hover .sidebar-icon-wrapper {
  background-color: #eef2ff;
  color: #4f46e5;
}

/* Logout Button */
.sidebar-logout-btn {
  display: flex;
  align-items: center;
  width: 100%;
  padding-left: 1rem;
  padding-right: 1rem;
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
  border-radius: 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  transition-property: all;
  transition-duration: 200ms;
  color: #475569;
  border: 1px solid transparent;
}

.sidebar-logout-btn:hover {
  color: #dc2626;
  background-color: #fef2f2;
  border-color: #fee2e2;
}

/* Custom Scrollbar for Sidebar */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 9999px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #94a3b8;
}

/* Pattern background */
.pattern-bg {
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239C92AC' fill-opacity='0.4'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

/* Smooth transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

<style>
/* Global Styles */
:root {
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
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

/* Smooth page transitions */
.page-transition-enter-active,
.page-transition-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-transition-enter-from,
.page-transition-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Utility classes for spacing */
.space-x-2 > * + * {
  margin-left: 0.5rem;
}

.space-x-3 > * + * {
  margin-left: 0.75rem;
}

.space-x-4 > * + * {
  margin-left: 1rem;
}

.space-x-6 > * + * {
  margin-left: 1.5rem;
}

.space-y-1 > * + * {
  margin-top: 0.25rem;
}

.space-y-2 > * + * {
  margin-top: 0.5rem;
}

.space-y-4 > * + * {
  margin-top: 1rem;
}

.space-y-6 > * + * {
  margin-top: 1.5rem;
}
</style>