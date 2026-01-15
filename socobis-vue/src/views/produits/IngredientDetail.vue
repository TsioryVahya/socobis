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
  <div class="content-wrapper py-4 px-2 sm:px-4">
    <h1 class="box-title text-xl font-semibold mb-4 flex items-center gap-2">
      <button type="button" @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &#8592;
      </button>
      <span>Consultation composant</span>
    </h1>

    <div v-if="loading" class="text-center py-10 text-gray-500">Chargement...</div>
    <div v-else-if="error" class="max-w-4xl mx-auto bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
      {{ error }}
    </div>
    <div v-else-if="ingredient" class="max-w-4xl mx-auto bg-white p-4 sm:p-6 rounded-md shadow">
      <!-- Détails de l'ingrédient -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-4 mb-6 text-sm">
        <div>
          <div class="text-xs font-semibold text-gray-500 uppercase">Libellé</div>
          <div class="mt-1 font-medium text-gray-900">{{ ingredient.libelle }}</div>
        </div>
         <div>
          <div class="text-xs font-semibold text-gray-500 uppercase">Catégorie</div>
          <div class="mt-1 text-gray-900">{{ ingredient.categorieIngredientLib }}</div>
        </div>
        <div>
          <div class="text-xs font-semibold text-gray-500 uppercase">Prix de revient</div>
          <div class="mt-1 text-gray-900">{{ formatCurrency(ingredient.revient) }}</div>
        </div>
        <div>
          <div class="text-xs font-semibold text-gray-500 uppercase">Prix de vente</div>
          <div class="mt-1 text-gray-900">{{ formatCurrency(ingredient.pv) }}</div>
        </div>
        <div>
          <div class="text-xs font-semibold text-gray-500 uppercase">Unité</div>
          <div class="mt-1 text-gray-900">{{ ingredient.unite }}</div>
        </div>
        <div>
          <div class="text-xs font-semibold text-gray-500 uppercase">Type de stock</div>
          <div class="mt-1 text-gray-900">{{ ingredient.typeStock }}</div>
        </div>
      </div>

      <!-- Composition -->
      <div v-if="ingredient.composition && ingredient.composition.length > 0" class="mt-8">
        <h2 class="text-sm font-semibold text-gray-800 mb-2">Composition</h2>
        <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
          <table class="min-w-full divide-y divide-gray-200 text-xs">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Ingrédient</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Quantité</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Unité</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr v-for="item in ingredient.composition" :key="item.id">
                <td class="px-3 py-2">{{ item.libingredients }}</td>
                <td class="px-3 py-2 text-right">{{ item.quantite }}</td>
                <td class="px-3 py-2">{{ item.unite }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Autres composants concernés -->
      <div v-if="ingredient.autresComposants && ingredient.autresComposants.length > 0" class="mt-8">
        <h2 class="text-sm font-semibold text-gray-800 mb-2">Autres Composants concernés</h2>
        <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
          <table class="min-w-full divide-y divide-gray-200 text-xs">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Produit</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Quantité</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Unité</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr v-for="item in ingredient.autresComposants" :key="item.id">
                <td class="px-3 py-2">{{ item.libproduit }}</td>
                <td class="px-3 py-2 text-right">{{ item.quantite }}</td>
                <td class="px-3 py-2">{{ item.unite }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Décomposition finale -->
      <div v-if="ingredient.decompositionFinale && ingredient.decompositionFinale.length > 0" class="mt-8">
        <h2 class="text-sm font-semibold text-gray-800 mb-2">Décomposition finale</h2>
        <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
          <table class="min-w-full divide-y divide-gray-200 text-xs">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Ingrédient</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Quantité</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Unité</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">PU</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Montant</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr v-for="item in ingredient.decompositionFinale" :key="item.id">
                <td class="px-3 py-2">{{ item.libIngredients }}</td>
                <td class="px-3 py-2 text-right">{{ item.quantite.toFixed(10) }}</td>
                <td class="px-3 py-2">{{ item.unite }}</td>
                <td class="px-3 py-2 text-right">{{ formatCurrency(item.qteAv) }}</td>
                <td class="px-3 py-2 text-right">{{ formatCurrency(item.revient) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Historique prix de vente -->
      <div v-if="ingredient.historiquePv && ingredient.historiquePv.length > 0" class="mt-8">
        <h2 class="text-sm font-semibold text-gray-800 mb-2">Historique prix de vente</h2>
        <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
          <table class="min-w-full divide-y divide-gray-200 text-xs">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Date</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Prix unitaire</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Remarque</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr v-for="item in ingredient.historiquePv" :key="item.id">
                <td class="px-3 py-2">{{ formatDate(item.daty) }}</td>
                <td class="px-3 py-2 text-right">{{ formatCurrency(item.pu) }}</td>
                <td class="px-3 py-2">{{ item.remarque }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Historique Tarif -->
      <div v-if="ingredient.historiqueTarif && ingredient.historiqueTarif.length > 0" class="mt-8">
        <h2 class="text-sm font-semibold text-gray-800 mb-2">Historique Tarif</h2>
        <div class="shadow overflow-hidden border border-gray-200 sm:rounded-lg">
          <table class="min-w-full divide-y divide-gray-200 text-xs">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Date</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Type client</th>
                <th class="px-3 py-2 text-left font-medium text-gray-500">Prix unitaire</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr v-for="item in ingredient.historiqueTarif" :key="item.id">
                <td class="px-3 py-2">{{ formatDate(item.daty) }}</td>
                <td class="px-3 py-2">{{ item.idtypeclientlib }}</td>
                <td class="px-3 py-2 text-right">{{ formatCurrency(item.pu) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="mt-6 flex justify-end">
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
