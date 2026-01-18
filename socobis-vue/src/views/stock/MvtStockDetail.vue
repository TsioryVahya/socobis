<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { format } from 'date-fns';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const error = ref<string | null>(null);
const mouvement = ref<any | null>(null);

const getStatusLabel = (status: any) => {
  if (status === 1 || status === '1') return 'CRÉÉ';
  if (status === 10 || status === '10') return 'VALIDÉ';
  if (status === 20 || status === '20') return 'TERMINÉ';
  return status || 'INCONNU';
};

const formatCurrency = (value: number) => {
  if (typeof value !== 'number') return '-';
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MGA' }).format(value);
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  try {
    return format(new Date(dateString), 'dd/MM/yyyy');
  } catch (e) {
    return dateString;
  }
};

const fetchMouvementDetail = async () => {
  const id = route.params.id as string;
  if (!id) return;

  loading.value = true;
  error.value = null;
  mouvement.value = null;

  try {
    const response = await axios.get('/MvtStockServlet', {
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
      mouvement.value = payload.data;
    } else {
      error.value = payload.message || 'Erreur lors du chargement du mouvement.';
    }
  } catch (err: any) {
    error.value = 'Erreur technique lors du chargement du mouvement.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleViser = async () => {
  if (!mouvement.value) return;
  loading.value = true;
  error.value = null;

  try {
    const response = await axios.get('/MvtStockServlet', {
      params: { action: 'viserMouvement', id: mouvement.value.id }
    });
    
    let payload: any = response.data;
    if (typeof response.data === 'string') {
      try { payload = JSON.parse(response.data); } catch (e) {
        const raw = response.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start !== -1 && end !== -1 && end > start) { payload = JSON.parse(raw.substring(start, end + 1)); } else { throw e; }
      }
    }

    if (payload.status === 'success') {
      // Recharger les données pour voir le nouvel état
      await fetchMouvementDetail();
    } else {
      error.value = payload.message || 'Erreur lors de la validation.';
    }
  } catch (err: any) {
    error.value = 'Erreur technique lors de la validation.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchMouvementDetail);

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
            <h1 class="text-xl font-bold text-slate-900">Fiche du mouvement de stock</h1>
          </div>
          
          <div v-if="mouvement" class="flex items-center gap-3">
            <span :class="{
              'px-3 py-1 text-xs font-bold rounded-full border': true,
              'bg-blue-50 text-blue-700 border-blue-100': mouvement.etat == 1,
              'bg-emerald-50 text-emerald-700 border-emerald-100': mouvement.etat >= 10,
              'bg-slate-50 text-slate-600 border-slate-200': mouvement.etat < 1 && mouvement.etat != null
            }">
              {{ getStatusLabel(mouvement.etat) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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

      <div v-else-if="mouvement" class="space-y-6">
        <!-- Informations Générales -->
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden transition-all hover:shadow-md">
          <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
            <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Informations du Mouvement</h2>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div class="space-y-4">
                <div class="space-y-1">
                  <p class="text-xs font-semibold text-slate-500 uppercase">Référence</p>
                  <p class="text-base font-bold text-indigo-600">#{{ mouvement.id }}</p>
                </div>
                <div class="space-y-1">
                  <p class="text-xs font-semibold text-slate-500 uppercase">Date</p>
                  <p class="text-base font-medium text-slate-900">{{ formatDate(mouvement.daty) }}</p>
                </div>
                <div class="space-y-1">
                  <p class="text-xs font-semibold text-slate-500 uppercase">Désignation</p>
                  <p class="text-base font-medium text-slate-900">{{ mouvement.designation }}</p>
                </div>
              </div>
              <div class="space-y-4">
                <div class="space-y-1">
                  <p class="text-xs font-semibold text-slate-500 uppercase">Magasin</p>
                  <p class="text-base font-medium text-slate-900 flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                    </svg>
                    {{ mouvement.libelleMagasin }}
                  </p>
                </div>
                <div class="space-y-1">
                  <p class="text-xs font-semibold text-slate-500 uppercase">Type de Mouvement</p>
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-100 text-slate-700 border border-slate-200">
                    {{ mouvement.libelleTypeMvtStock }}
                  </span>
                </div>
                <div v-if="mouvement.idobjet" class="space-y-1">
                  <p class="text-xs font-semibold text-slate-500 uppercase">Fabrication Associée</p>
                  <router-link :to="{ name: 'FabricationDetail', params: { id: mouvement.idobjet } }" class="text-sm font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1">
                    #{{ mouvement.idobjet }}
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Détails du mouvement -->
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
            <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">Détails du mouvement</h2>
            <span class="px-2 py-1 text-xs font-bold bg-white text-slate-600 rounded-lg border border-slate-200">{{ mouvement.filles?.length || 0 }} lignes</span>
          </div>
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200">
              <thead>
                <tr class="bg-slate-50/50">
                  <th class="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">Ingrédient</th>
                  <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Entrée</th>
                  <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Sortie</th>
                  <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">P.U.</th>
                  <th class="px-6 py-3 text-right text-xs font-bold text-slate-500 uppercase tracking-wider">Montant</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-slate-100">
                <tr v-for="fille in mouvement.filles" :key="fille.id" class="hover:bg-slate-50 transition-colors">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-slate-900">
                    {{ fille.designation || fille.idProduit }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-bold" :class="fille.entree > 0 ? 'text-emerald-600' : 'text-slate-400'">
                    {{ fille.entree || '-' }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-bold" :class="fille.sortie > 0 ? 'text-amber-600' : 'text-slate-400'">
                    {{ fille.sortie || '-' }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-right text-slate-600">
                    {{ formatCurrency(fille.pu) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-right font-bold text-slate-900">
                    {{ formatCurrency((fille.entree + fille.sortie) * fille.pu) }}
                  </td>
                </tr>
              </tbody>
              <tfoot>
                <tr class="bg-slate-50/50">
                  <td colspan="4" class="px-6 py-4 text-right text-sm font-bold text-slate-900 uppercase">Montant Total</td>
                  <td class="px-6 py-4 text-right text-lg font-black text-indigo-600">{{ formatCurrency(mouvement.montant) }}</td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4">
          <button
            v-if="mouvement && mouvement.etat === 1"
            @click="handleViser"
            class="inline-flex items-center px-6 py-2.5 text-sm font-bold text-white bg-emerald-600 rounded-xl hover:bg-emerald-700 transition-all active:scale-95 shadow-emerald-200 shadow-lg"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Viser le mouvement
          </button>
          <button
            @click="router.back()"
            class="inline-flex items-center px-8 py-2.5 text-sm font-bold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-50 transition-all active:scale-95 shadow-sm"
          >
            Retour
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

