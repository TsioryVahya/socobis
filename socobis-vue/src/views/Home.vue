<template>
  <div class="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
    <div class="text-center">
      <h1 class="text-4xl font-extrabold text-gray-900 sm:text-5xl sm:tracking-tight lg:text-6xl">
        Gestion Socobis
      </h1>
      <p class="mt-5 text-xl text-gray-500">
        Système de gestion des fabrications et compteurs.
      </p>
    </div>

    <div class="mt-12 grid gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
      <!-- Compteurs Card -->
      <div class="bg-white overflow-hidden shadow rounded-lg border border-gray-200 hover:shadow-lg transition-shadow">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900">Compteurs</h3>
          <div class="mt-2 max-w-xl text-sm text-gray-500">
            <p>Gérez vos compteurs de production, visualisez la liste et créez de nouveaux relevés.</p>
          </div>
          <div class="mt-5 space-y-3">
            <router-link to="/compteurs" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 w-full justify-center">
              Liste des compteurs
            </router-link>
            <router-link to="/compteurs/nouveau" class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 w-full justify-center">
              Nouveau compteur
            </router-link>
          </div>
        </div>
      </div>

      <!-- Fabrications Card -->
      <div class="bg-white overflow-hidden shadow rounded-lg border border-gray-200 hover:shadow-lg transition-shadow">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900">Fabrications</h3>
          <div class="mt-2 max-w-xl text-sm text-gray-500">
            <p>Lancez de nouvelles opérations de fabrication et suivez les opérations en cours.</p>
          </div>
          <div class="mt-5 space-y-3">
            <router-link to="/fabrications" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 w-full justify-center">
              Liste des fabrications
            </router-link>
            <router-link to="/fabrications/nouveau" class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 w-full justify-center">
              Nouvelle fabrication
            </router-link>
          </div>
        </div>
      </div>

      <!-- OF Card -->
      <div class="bg-white overflow-hidden shadow rounded-lg border border-gray-200 hover:shadow-lg transition-shadow">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900">Ordres de Fabrication</h3>
          <div class="mt-2 max-w-xl text-sm text-gray-500">
            <p>Gérez les Ordres de Fabrication (OF) maîtres et leurs composants.</p>
          </div>
          <div class="mt-5 space-y-3">
            <router-link to="/ofs" class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 w-full justify-center">
              Liste des OF
            </router-link>
            <router-link to="/ofs/nouveau" class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 w-full justify-center">
              Nouvel OF
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
