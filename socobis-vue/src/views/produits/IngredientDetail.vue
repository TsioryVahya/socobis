<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const error = ref<string | null>(null);
const ingredient = ref<any | null>(null);

const fetchIngredientDetail = async () => {
  const id = route.params.id as string;
  if (!id) return;

  loading.value = true;
  error.value = null;
  ingredient.value = null;

  try {
    const response = await axios.get('/IngredientServlet', {
      params: { action: 'getDetail', id }
    });

    let payload: any = response.data;
    if (typeof response.data === 'string') {
      try { payload = JSON.parse(response.data); } catch (e) {
        const raw = response.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start !== -1 && end !== -1 && end > start) { payload = JSON.parse(raw.substring(start, end + 1)); } else { throw e; }
      }
    }

    if (payload.status === 'success') {
      ingredient.value = payload.data;
    } else {
      error.value = payload.message || 'Erreur lors du chargement de l\'ingrédient.';
    }
  } catch (err: any) {
    error.value = 'Erreur technique lors du chargement de l\'ingrédient.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const formatCurrency = (value: number) => {
  if (typeof value !== 'number') return '-';
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MGA' }).format(value);
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(date);
  } catch (e) {
    return dateString;
  }
};

onMounted(fetchIngredientDetail);

</script>

<template>
  <div class="min-h-screen bg-slate-50/50 pb-12">
    <!-- Header -->
    <div class="bg-white border-b border-slate-200 sticky top-0 z-10 backdrop-blur-md bg-white/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center gap-4">
            <button 
              @click="router.back()" 
              class="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500 hover:text-slate-700"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </button>
            <h1 class="text-xl font-bold text-slate-900">Consultation composant</h1>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        <p class="mt-4 text-slate-500 font-medium">Chargement des données...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 border border-red-100 rounded-2xl p-4 flex items-start gap-3 text-red-700">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
        </svg>
        <p class="font-medium">{{ error }}</p>
      </div>

      <div v-else-if="ingredient" class="space-y-8">
        <!-- Informations Principales -->
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
            <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Fiche Technique</h2>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Libellé</p>
                <p class="text-lg font-bold text-slate-900">{{ ingredient.libelle }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Catégorie</p>
                <p class="text-base font-medium text-indigo-600">{{ ingredient.categorieIngredientLib }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Unité</p>
                <p class="text-base font-medium text-slate-700">{{ ingredient.unite }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Prix de revient</p>
                <p class="text-lg font-bold text-emerald-600">{{ formatCurrency(ingredient.revient) }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Prix de vente</p>
                <p class="text-lg font-bold text-indigo-600">{{ formatCurrency(ingredient.pv) }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold text-slate-500 uppercase">Type de stock</p>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-100 text-slate-700 border border-slate-200 uppercase">
                  {{ ingredient.typeStock }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Sections Tableaux -->
        <div class="grid grid-cols-1 gap-8">
          <!-- Composition -->
          <div v-if="ingredient.composition && ingredient.composition.length > 0" class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Composition</h2>
              <span class="px-2 py-1 text-xs font-bold bg-slate-100 text-slate-600 rounded-lg">{{ ingredient.composition.length }} éléments</span>
            </div>
            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-slate-200">
                <thead>
                  <tr class="bg-slate-50">
                    <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Ingrédient</th>
                    <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Quantité</th>
                    <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Unité</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-slate-100">
                  <tr v-for="item in ingredient.composition" :key="item.id" class="hover:bg-slate-50 transition-colors">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-slate-900">{{ item.libingredients }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-mono font-bold text-indigo-600">{{ item.quantite }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">{{ item.unite }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Autres composants concernés -->
          <div v-if="ingredient.autresComposants && ingredient.autresComposants.length > 0" class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div class="px-6 py-4 border-b border-slate-100">
              <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Utilisé dans les produits</h2>
            </div>
            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-slate-200">
                <thead>
                  <tr class="bg-slate-50">
                    <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Produit</th>
                    <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Quantité</th>
                    <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Unité</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-slate-100">
                  <tr v-for="item in ingredient.autresComposants" :key="item.id" class="hover:bg-slate-50 transition-colors">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-slate-900">{{ item.libproduit }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-mono font-bold text-amber-600">{{ item.quantite }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">{{ item.unite }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Décomposition finale -->
          <div v-if="ingredient.decompositionFinale && ingredient.decompositionFinale.length > 0" class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div class="px-6 py-4 border-b border-slate-100">
              <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Décomposition finale</h2>
            </div>
            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-slate-200">
                <thead>
                  <tr class="bg-slate-50">
                    <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Ingrédient</th>
                    <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Quantité</th>
                    <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Unité</th>
                    <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">P.U.</th>
                    <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Montant</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-slate-100">
                  <tr v-for="item in ingredient.decompositionFinale" :key="item.id" class="hover:bg-slate-50 transition-colors">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-slate-900">{{ item.libIngredients }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-mono font-bold text-indigo-600">{{ item.quantite.toFixed(6) }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">{{ item.unite }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-right text-slate-600">{{ formatCurrency(item.qteAv) }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-bold text-emerald-600">{{ formatCurrency(item.revient) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Historiques -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <!-- Historique prix de vente -->
            <div v-if="ingredient.historiquePv && ingredient.historiquePv.length > 0" class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100">
                <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Historique Prix de Vente</h2>
              </div>
              <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-slate-200">
                  <thead>
                    <tr class="bg-slate-50">
                      <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Date</th>
                      <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">P.U.</th>
                      <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Remarque</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-slate-100">
                    <tr v-for="item in ingredient.historiquePv" :key="item.id">
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-600">{{ formatDate(item.daty) }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-bold text-indigo-600">{{ formatCurrency(item.pu) }}</td>
                      <td class="px-6 py-4 text-sm text-slate-500 italic">{{ item.remarque || '-' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Historique Tarif -->
            <div v-if="ingredient.historiqueTarif && ingredient.historiqueTarif.length > 0" class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100">
                <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Historique Tarif Client</h2>
              </div>
              <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-slate-200">
                  <thead>
                    <tr class="bg-slate-50">
                      <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Date</th>
                      <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Type Client</th>
                      <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">P.U.</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-slate-100">
                    <tr v-for="item in ingredient.historiqueTarif" :key="item.id">
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-600">{{ formatDate(item.daty) }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-slate-900">{{ item.idtypeclientlib }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-bold text-indigo-600">{{ formatCurrency(item.pu) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer Actions -->
        <div class="flex justify-end pt-4">
          <button
            @click="router.back()"
            class="inline-flex items-center px-8 py-3 text-sm font-bold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-50 transition-all active:scale-95 shadow-sm"
          >
            Retour
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

