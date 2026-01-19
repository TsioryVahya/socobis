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
  <div class="content-wrapper py-4 px-2 sm:px-4">
    <h1 class="box-title text-xl font-semibold mb-4 flex items-center gap-2">
      <button type="button" @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &#8592;
      </button>
      <span>Fiche du mouvement de stock</span>
    </h1>

    <div v-if="loading" class="text-center py-10 text-gray-500">Chargement...</div>
    <div v-else-if="error" class="max-w-3xl mx-auto bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
      {{ error }}
    </div>
    <div v-else-if="mouvement" class="max-w-3xl mx-auto bg-white p-4 sm:p-6 rounded-md shadow">
        <!-- Détails de l'en-tête -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-4 mb-6 text-sm">
            <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">ID</div>
                <div class="mt-1 font-medium text-gray-900">{{ mouvement.id }}</div>
            </div>
            <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Date</div>
                <div class="mt-1 text-gray-900">{{ formatDate(mouvement.daty) }}</div>
            </div>
            <div class="sm:col-span-2">
                <div class="text-xs font-semibold text-gray-500 uppercase">Désignation</div>
                <div class="mt-1 text-gray-900">{{ mouvement.designation }}</div>
            </div>
            <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Magasin</div>
                <div class="mt-1 text-gray-900">{{ mouvement.libelleMagasin }}</div>
            </div>
            <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Type</div>
                <div class="mt-1 text-gray-900">{{ mouvement.libelleTypeMvtStock }}</div>
            </div>
            <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">Fabrication Associée</div>
                <div class="mt-1 text-gray-900">{{ mouvement.idobjet }}</div>
            </div>
            <div>
                <div class="text-xs font-semibold text-gray-500 uppercase">État</div>
                <div class="mt-1 font-semibold text-gray-900">{{ getStatusLabel(mouvement.etat) }}</div>
            </div>
        </div>

        <!-- Lignes de détail -->
        <div class="mt-8">
            <h2 class="text-sm font-semibold text-gray-800 mb-2">Détails du mouvement</h2>
            <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
            <table class="min-w-full divide-y divide-gray-200 text-xs">
                <thead class="bg-gray-50">
                <tr>
                    <th class="px-3 py-2 text-left font-medium text-gray-500">Ingrédient</th>
                    <th class="px-3 py-2 text-left font-medium text-gray-500">Entrée</th>
                    <th class="px-3 py-2 text-left font-medium text-gray-500">Sortie</th>
                    <th class="px-3 py-2 text-left font-medium text-gray-500">PU</th>
                    <th class="px-3 py-2 text-left font-medium text-gray-500">Montant</th>
                </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-100">
                <tr v-for="fille in mouvement.filles" :key="fille.id">
                    <td class="px-3 py-2">{{ fille.designation || fille.idProduit }}</td>
                    <td class="px-3 py-2 text-right">{{ fille.entree }}</td>
                    <td class="px-3 py-2 text-right">{{ fille.sortie }}</td>
                    <td class="px-3 py-2 text-right">{{ formatCurrency(fille.pu) }}</td>
                    <td class="px-3 py-2 text-right">{{ formatCurrency((fille.entree + fille.sortie) * fille.pu) }}</td>
                </tr>
                </tbody>
            </table>
            </div>
            <div class="text-right font-bold mt-2 text-sm">Montant Total : {{ formatCurrency(mouvement.montant) }}</div>
        </div>

        <!-- Boutons d'action -->
        <div class="mt-6 flex justify-end space-x-3">
            <button
                v-if="mouvement && mouvement.etat === 1"
                @click="handleViser"
                class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
            >
                Viser le mouvement
            </button>
            <button
                type="button"
                @click="router.back()"
                class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
            >
                Retour
            </button>
        </div>
    </div>
  </div>
</template>
