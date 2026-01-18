<template>
  <div class="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
    <div class="relative overflow-hidden rounded-3xl bg-blue-600 px-8 py-12 shadow-2xl mb-12">
      <div class="absolute top-0 right-0 -mt-20 -mr-20 w-64 h-64 bg-blue-500 rounded-full opacity-20 blur-3xl"></div>
      <div class="absolute bottom-0 left-0 -mb-20 -ml-20 w-64 h-64 bg-indigo-500 rounded-full opacity-20 blur-3xl"></div>
      
      <div class="relative text-center">
        <h1 class="text-4xl font-black text-white sm:text-5xl lg:text-6xl tracking-tight">
          Gestion <span class="text-blue-200">SOCOBIS</span>
        </h1>
        <p class="mt-6 text-xl text-blue-100 max-w-2xl mx-auto font-medium">
          Plateforme intelligente de gestion des fabrications, des compteurs et du suivi de production en temps réel.
        </p>
      </div>
    </div>

    <div class="grid gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
      <!-- Compteurs Card -->
      <div class="group bg-white rounded-2xl p-8 shadow-sm border border-slate-200 hover:shadow-xl hover:border-blue-200 transition-all duration-300 transform hover:-translate-y-1">
        <div class="w-14 h-14 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 mb-6 group-hover:bg-blue-600 group-hover:text-white transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
        </div>
        <h3 class="text-xl font-bold text-slate-900 mb-3">Compteurs</h3>
        <p class="text-slate-500 text-sm leading-relaxed mb-8">
          Gérez vos compteurs de production, visualisez la liste détaillée et créez de nouveaux relevés précis.
        </p>
        <div class="space-y-3">
          <router-link to="/compteurs" class="flex items-center justify-center px-4 py-2.5 bg-slate-900 text-white rounded-xl text-sm font-bold hover:bg-slate-800 transition-colors">
            Liste des compteurs
          </router-link>
          <router-link to="/compteurs/nouveau" class="flex items-center justify-center px-4 py-2.5 bg-white text-slate-700 border border-slate-200 rounded-xl text-sm font-bold hover:bg-slate-50 transition-colors">
            Nouveau compteur
          </router-link>
        </div>
      </div>

      <!-- Fabrications Card -->
      <div class="group bg-white rounded-2xl p-8 shadow-sm border border-slate-200 hover:shadow-xl hover:border-green-200 transition-all duration-300 transform hover:-translate-y-1">
        <div class="w-14 h-14 bg-green-50 rounded-xl flex items-center justify-center text-green-600 mb-6 group-hover:bg-green-600 group-hover:text-white transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        </div>
        <h3 class="text-xl font-bold text-slate-900 mb-3">Fabrications</h3>
        <p class="text-slate-500 text-sm leading-relaxed mb-8">
          Lancez de nouvelles opérations de fabrication, planifiez vos ressources et suivez les ordres en cours.
        </p>
        <div class="space-y-3">
          <router-link to="/fabrications" class="flex items-center justify-center px-4 py-2.5 bg-green-600 text-white rounded-xl text-sm font-bold hover:bg-green-700 transition-colors">
            Liste des fabrications
          </router-link>
          <router-link to="/fabrications/nouveau" class="flex items-center justify-center px-4 py-2.5 bg-white text-slate-700 border border-slate-200 rounded-xl text-sm font-bold hover:bg-slate-50 transition-colors">
            Nouvelle fabrication
          </router-link>
        </div>
      </div>

      <!-- OF Card -->
      <div class="group bg-white rounded-2xl p-8 shadow-sm border border-slate-200 hover:shadow-xl hover:border-indigo-200 transition-all duration-300 transform hover:-translate-y-1">
        <div class="w-14 h-14 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 mb-6 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
          </svg>
        </div>
        <h3 class="text-xl font-bold text-slate-900 mb-3">Ordres de Fab</h3>
        <p class="text-slate-500 text-sm leading-relaxed mb-8">
          Gérez les Ordres de Fabrication (OF) maîtres, contrôlez les composants et validez les étapes de production.
        </p>
        <div class="space-y-3">
          <router-link to="/ofs" class="flex items-center justify-center px-4 py-2.5 bg-indigo-600 text-white rounded-xl text-sm font-bold hover:bg-indigo-700 transition-colors">
            Liste des OF
          </router-link>
          <router-link to="/ofs/nouveau" class="flex items-center justify-center px-4 py-2.5 bg-white text-slate-700 border border-slate-200 rounded-xl text-sm font-bold hover:bg-slate-50 transition-colors">
            Nouvel OF
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
