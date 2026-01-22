<template>
  <div class="space-y-8">
    <div class="bg-white p-8 rounded-3xl shadow-sm border border-slate-200">
      <h1 class="text-3xl font-bold text-slate-800">Bienvenue sur le portail SOCOBIS</h1>
      <p class="mt-2 text-slate-500 max-w-2xl">
        Votre centre de contrôle pour la gestion de la production, des compteurs et des ordres de fabrication.
      </p>
    </div>

    <div class="grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
      <!-- Compteurs Card -->
      <div class="group bg-white p-6 rounded-3xl shadow-sm border border-slate-200 hover:shadow-xl hover:-translate-y-1 transition-all duration-300">
        <div class="w-12 h-12 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-600 mb-4 group-hover:bg-indigo-600 group-hover:text-white transition-colors duration-300">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
        </div>
        <h3 class="text-xl font-bold text-slate-800 mb-2">Compteurs</h3>
        <p class="text-slate-500 text-sm mb-6 leading-relaxed">
          Suivez les relevés de production en temps réel et analysez les performances des machines.
        </p>
        <div class="flex flex-col space-y-3">
          <router-link to="/compteurs" class="flex items-center justify-center px-4 py-3 bg-indigo-600 text-white rounded-xl font-semibold shadow-lg shadow-indigo-600/20 hover:bg-indigo-700 transition-colors">
            Voir la liste
          </router-link>
          <router-link to="/compteurs/nouveau" class="flex items-center justify-center px-4 py-3 bg-slate-50 text-slate-700 rounded-xl font-semibold hover:bg-slate-100 transition-colors border border-slate-200">
            Nouveau relevé
          </router-link>
        </div>
      </div>

      <!-- Fabrications Card -->
      <div class="group bg-white p-6 rounded-3xl shadow-sm border border-slate-200 hover:shadow-xl hover:-translate-y-1 transition-all duration-300">
        <div class="w-12 h-12 bg-emerald-50 rounded-2xl flex items-center justify-center text-emerald-600 mb-4 group-hover:bg-emerald-600 group-hover:text-white transition-colors duration-300">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        </div>
        <h3 class="text-xl font-bold text-slate-800 mb-2">Fabrications</h3>
        <p class="text-slate-500 text-sm mb-6 leading-relaxed">
          Gérez le cycle de vie de vos fabrications, du lancement à la clôture finale.
        </p>
        <div class="flex flex-col space-y-3">
          <router-link to="/fabrications" class="flex items-center justify-center px-4 py-3 bg-emerald-600 text-white rounded-xl font-semibold shadow-lg shadow-emerald-600/20 hover:bg-emerald-700 transition-colors">
            Voir la liste
          </router-link>
          <router-link to="/fabrications/nouveau" class="flex items-center justify-center px-4 py-3 bg-slate-50 text-slate-700 rounded-xl font-semibold hover:bg-slate-100 transition-colors border border-slate-200">
            Lancer une fab
          </router-link>
        </div>
      </div>

      <!-- OF Card -->
      <div class="group bg-white p-6 rounded-3xl shadow-sm border border-slate-200 hover:shadow-xl hover:-translate-y-1 transition-all duration-300">
        <div class="w-12 h-12 bg-amber-50 rounded-2xl flex items-center justify-center text-amber-600 mb-4 group-hover:bg-amber-600 group-hover:text-white transition-colors duration-300">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012-2" />
          </svg>
        </div>
        <h3 class="text-xl font-bold text-slate-800 mb-2">Ordres de Fab</h3>
        <p class="text-slate-500 text-sm mb-6 leading-relaxed">
          Organisez vos ordres de fabrication maîtres et suivez les dépendances.
        </p>
        <div class="flex flex-col space-y-3">
          <router-link to="/ofs" class="flex items-center justify-center px-4 py-3 bg-amber-600 text-white rounded-xl font-semibold shadow-lg shadow-amber-600/20 hover:bg-amber-700 transition-colors">
            Voir la liste
          </router-link>
          <router-link to="/ofs/nouveau" class="flex items-center justify-center px-4 py-3 bg-slate-50 text-slate-700 rounded-xl font-semibold hover:bg-slate-100 transition-colors border border-slate-200">
            Créer un OF
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
