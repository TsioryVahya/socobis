<template>
  <div class="fabrication-view">
    <!-- Header Section -->
    <div class="header-section">
      <div class="breadcrumb-container">
        <button @click="router.back()" class="back-button">
          <svg class="back-icon" viewBox="0 0 24 24">
            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
          </svg>
          Retour
        </button>
        <div class="breadcrumb-separator">/</div>
        <span class="breadcrumb-current">Fiche Fabrication</span>
      </div>
      
      <div class="header-main">
        <h1 class="page-title">Fiche Fabrication</h1>
        <div v-if="fabrication" class="status-badge" :class="getStatusClass(fabrication.etat)">
          {{ getStatusLabel(fabrication.etat) }}
        </div>
      </div>
      
      <div v-if="fabrication" class="fabrication-id">
        Référence: <strong>{{ fabrication.id }}</strong>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Chargement en cours...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <svg class="error-icon" viewBox="0 0 24 24">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
      </svg>
      <div class="error-content">
        <h3>Erreur de chargement</h3>
        <p>{{ error }}</p>
      </div>
    </div>

    <!-- Main Content -->
    <div v-else-if="fabrication" class="main-content">
      <!-- Information Cards Row -->
      <div class="info-cards-grid">
        <!-- Fabrication Info Card -->
        <div class="info-card">
          <div class="card-header">
            <svg class="card-icon" viewBox="0 0 24 24">
              <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
            <h3>Informations</h3>
          </div>
          <div class="card-content">
            <div class="info-item">
              <span class="info-label">Désignation</span>
              <span class="info-value">{{ fabrication.libelle || 'Non spécifié' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Date création</span>
              <span class="info-value">{{ formatDate(fabrication.daty) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Magasin cible</span>
              <span class="info-value">{{ fabrication.cible || '-' }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">Remarques</span>
              <div class="remarks-box">{{ fabrication.remarque || 'Aucune remarque' }}</div>
            </div>
          </div>
        </div>

        <!-- Financial Summary Card -->
        <div class="financial-card">
          <div class="card-header">
            <svg class="card-icon" viewBox="0 0 24 24">
              <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
            </svg>
            <h3>Synthèse financière</h3>
          </div>
          <div class="card-content">
            <div class="financial-total">
              <div class="total-label">Coût total estimé</div>
              <div class="total-amount">{{ formatCurrency(totalPrixRevient) }}</div>
            </div>
            <div class="financial-breakdown">
              <div class="breakdown-item">
                <span class="breakdown-label">Coûts directs</span>
                <span class="breakdown-amount">{{ formatCurrency(totalCharges) }}</span>
              </div>
              <div class="breakdown-item">
                <span class="breakdown-label">Coûts indirects</span>
                <span class="breakdown-amount">{{ formatCurrency(totalBudgetsFab) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Actions Card -->
        <div class="actions-card">
          <div class="card-header">
            <svg class="card-icon" viewBox="0 0 24 24">
              <path d="M13 3h-2v10h2V3zm4.83 2.17l-1.42 1.42C17.99 7.86 19 9.81 19 12c0 3.87-3.13 7-7 7s-7-3.13-7-7c0-2.19 1.01-4.14 2.58-5.42L6.17 5.17C4.23 6.82 3 9.26 3 12c0 4.97 4.03 9 9 9s9-4.03 9-9c0-2.74-1.23-5.18-3.17-6.83z"/>
            </svg>
            <h3>Actions</h3>
          </div>
          <div class="card-content">
            <button 
              v-if="fabrication.etat === 1 || fabrication.etat === '1'"
              @click="handleValidate"
              class="primary-action-button"
            >
              <svg class="button-icon" viewBox="0 0 24 24">
                <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
              </svg>
              Valider la fabrication
            </button>
            
            <div class="secondary-actions">
              <button 
                @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'residu' } })"
                class="secondary-button"
              >
                Gestion des résidus
              </button>
              
              <button 
                v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
                @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'entree' } })"
                class="secondary-button"
              >
                Entrées stock
              </button>
              
              <button 
                v-if="fabrication.etat >= 11 || fabrication.etat === '11'"
                @click="router.push({ name: 'FabricationMvtStock', params: { id: fabrication.id, type: 'sortie' } })"
                class="secondary-button"
              >
                Sorties stock
              </button>
              
              <button 
                @click="router.push({ name: 'ChargeSaisie', params: { id: fabrication.id } })"
                class="secondary-button"
              >
                Gestion charges
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs Navigation -->
      <div class="tabs-container">
        <div class="tabs-header">
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            @click="selectTab(tab.id)"
            class="tab-button"
            :class="{ 'active': activeTab === tab.id }"
          >
            {{ tab.label }}
            <div class="tab-indicator" :class="{ 'active': activeTab === tab.id }"></div>
          </button>
        </div>

        <!-- Tab Content -->
        <div class="tab-content">
          <!-- Details Tab -->
          <div v-if="activeTab === 'details'" class="tab-pane">
            <div class="table-container">
              <div class="table-header">
                <div class="table-title">Composants</div>
                <div class="table-subtitle">{{ details.length }} composant(s)</div>
              </div>
              
              <div class="table">
                <div class="table-row header">
                  <div class="table-cell">Ingrédient</div>
                  <div class="table-cell text-right">Quantité</div>
                  <div class="table-cell">Unité</div>
                  <div class="table-cell text-right">Prix unitaire</div>
                  <div class="table-cell text-right">Total</div>
                </div>
                
                <div v-if="details.length === 0" class="empty-state">
                  <svg class="empty-icon" viewBox="0 0 24 24">
                    <path d="M19 5v14H5V5h14m0-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"/>
                  </svg>
                  <p>Aucun composant enregistré</p>
                </div>
                
                <div 
                  v-for="d in details" 
                  :key="d.id"
                  class="table-row"
                >
                  <div class="table-cell">
                    <div class="ingredient-cell">
                      <span class="ingredient-id">{{ d.idIngredients }}</span>
                    </div>
                  </div>
                  <div class="table-cell text-right">
                    <span class="quantity-value">{{ d.qte }}</span>
                  </div>
                  <div class="table-cell">
                    <span class="unit-badge">{{ d.idunitelib }}</span>
                  </div>
                  <div class="table-cell text-right">
                    {{ formatCurrency(d.pu) }}
                  </div>
                  <div class="table-cell text-right">
                    <div class="amount-cell">{{ formatCurrency(d.montant) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Movements Tab -->
          <div v-if="activeTab === 'mouvements'" class="tab-pane">
            <div class="table-container">
              <div class="table-header">
                <div class="table-title">Mouvements de stock</div>
                <div class="table-subtitle">Total mouvements: {{ mouvements.length }}</div>
              </div>
              
              <div class="table">
                <div class="table-row header">
                  <div class="table-cell">Référence</div>
                  <div class="table-cell">Type</div>
                  <div class="table-cell">Magasin</div>
                  <div class="table-cell text-right">Montant</div>
                  <div class="table-cell">Statut</div>
                </div>
                
                <div v-if="mouvements.length === 0" class="empty-state">
                  <svg class="empty-icon" viewBox="0 0 24 24">
                    <path d="M19 5v14H5V5h14m0-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"/>
                  </svg>
                  <p>Aucun mouvement enregistré</p>
                </div>
                
                <div 
                  v-for="mvt in mouvements" 
                  :key="mvt.id"
                  class="table-row"
                >
                  <div class="table-cell">
                    <div class="reference-cell">
                      <span class="ref-id">{{ mvt.id }}</span>
                      <span class="ref-date">{{ formatDate(mvt.daty) }}</span>
                    </div>
                  </div>
                  <div class="table-cell">
                    <span class="type-badge" :class="getMovementTypeClass(mvt.typeMouvement)">
                      {{ mvt.typeMouvement }}
                    </span>
                  </div>
                  <div class="table-cell">
                    {{ mvt.magasin }}
                  </div>
                  <div class="table-cell text-right">
                    <div class="amount-cell">{{ formatCurrency(mvt.montant) }}</div>
                  </div>
                  <div class="table-cell">
                    <span class="status-badge-small" :class="getStatusClass(mvt.etat)">
                      {{ getMvtStatusLabel(mvt.etat) }}
                    </span>
                  </div>
                </div>
                
                <div v-if="mouvements.length > 0" class="table-summary">
                  <div class="summary-label">Total mouvements (sorties/résidus)</div>
                  <div class="summary-amount">{{ formatCurrency(totalBudgetsFab) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Charges Tab -->
          <div v-if="activeTab === 'charges'" class="tab-pane">
            <div class="table-container">
              <div class="table-header">
                <div class="table-title">Charges rattachées</div>
                <div class="table-subtitle">
                  <span class="total-label">Total: {{ formatCurrency(totalCharges) }}</span>
                </div>
              </div>
              
              <div class="table">
                <div class="table-row header">
                  <div class="table-cell">Date</div>
                  <div class="table-cell">Ingrédient</div>
                  <div class="table-cell">Description</div>
                  <div class="table-cell text-right">Quantité</div>
                  <div class="table-cell text-right">PU</div>
                  <div class="table-cell text-right">Total</div>
                </div>
                
                <div v-if="charges.length === 0" class="empty-state">
                  <svg class="empty-icon" viewBox="0 0 24 24">
                    <path d="M19 5v14H5V5h14m0-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"/>
                  </svg>
                  <p>Aucune charge enregistrée</p>
                </div>
                
                <div 
                  v-for="c in charges" 
                  :key="c.id"
                  class="table-row"
                >
                  <div class="table-cell">{{ formatDate(c.daty) }}</div>
                  <div class="table-cell">
                    <strong>{{ c.idingredients }}</strong>
                  </div>
                  <div class="table-cell description-cell">
                    {{ c.libelle || '-' }}
                  </div>
                  <div class="table-cell text-right">{{ c.qte }}</div>
                  <div class="table-cell text-right">{{ formatCurrency(c.pu) }}</div>
                  <div class="table-cell text-right">
                    <div class="amount-cell">{{ formatCurrency(c.montant) }}</div>
                  </div>
                </div>
                
                <div v-if="charges.length > 0" class="table-summary">
                  <div class="summary-label">Total des charges</div>
                  <div class="summary-amount">{{ formatCurrency(totalCharges) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref<string | null>(null)
const fabrication = ref<any | null>(null)
const details = ref<any[]>([])
const mouvements = ref<any[]>([])
const charges = ref<any[]>([])
const activeTab = ref('details')

const tabs = [
  { id: 'details', label: 'Composants' },
  { id: 'mouvements', label: 'Mouvements' },
  { id: 'charges', label: 'Charges' },
]

const totalCharges = computed(() => {
  return charges.value.reduce((sum, item) => sum + (Number(item.montant) || 0), 0)
})

const totalBudgetsFab = computed(() => {
  return mouvements.value
    .filter(m => {
       const type = (m.typeMouvement || '').toUpperCase();
       return !type.includes('ENTREE');
    })
    .reduce((sum, item) => sum + (Number(item.montant) || 0), 0)
})

const totalPrixRevient = computed(() => {
  return Number(totalCharges.value) + Number(totalBudgetsFab.value)
})

const getStatusLabel = (status: any) => {
  if (status === 1 || status === '1') return 'Créé'
  if (status === 11 || status === '11') return 'Validé'
  if (status === 10 || status === '10') return 'Validé'
  return status || 'Inconnu'
}

const getStatusClass = (status: any) => {
  if (status === 1 || status === '1') return 'status-created'
  if (status === 10 || status === '10' || status === 11 || status === '11') return 'status-validated'
  return 'status-unknown'
}

const getMovementTypeClass = (type: string) => {
  const t = (type || '').toLowerCase()
  if (t.includes('entree')) return 'type-entry'
  if (t.includes('sortie')) return 'type-exit'
  if (t.includes('residu')) return 'type-residue'
  return ''
}

const getMvtStatusLabel = (status: any) => {
  if (status === 1 || status === '1') return 'Créé'
  if (status === 10 || status === '10') return 'Validé'
  if (status === 20 || status === '20') return 'Terminé'
  return status || 'Inconnu'
}

const formatCurrency = (value: number) => {
  if (typeof value !== 'number') return '-'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MGA' }).format(value)
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  try {
    return format(new Date(dateString), 'dd/MM/yyyy')
  } catch (e) {
    return dateString
  }
}

const handleValidate = async () => {
  if (!fabrication.value) return
  try {
    loading.value = true
    error.value = null
    const id = fabrication.value.id
    const resp = await axios.get('/FabricationServlet', {
      params: { action: 'valider', id }
    })

    let payload: any = resp.data
    if (typeof resp.data === 'string') {
      try { payload = JSON.parse(resp.data) } catch (e) {
        const raw = resp.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) { 
          payload = JSON.parse(raw.substring(start, end + 1)) 
        } else { 
          throw e 
        }
      }
    }

    if (payload.status === 'success') {
      await fetchFabrication()
    } else {
      error.value = payload.message || "Erreur lors de la validation"
    }
  } catch (err: any) {
    console.error(err)
    error.value = "Erreur lors de la validation"
  } finally {
    loading.value = false
  }
}

const fetchFabrication = async () => {
  const id = route.params.id as string
  try {
    const response = await axios.get('/FabricationServlet', { params: { action: 'list', id } })
    let payload: any = response.data
    if (typeof response.data === 'string') {
      try { payload = JSON.parse(response.data) } catch (e) {
        const raw = response.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) { 
          payload = JSON.parse(raw.substring(start, end + 1)) 
        } else { 
          throw e 
        }
      }
    }
    if (payload.status === 'success') {
      const list = payload.data || []
      fabrication.value = Array.isArray(list) ? list.find((f: any) => f.id === id) || list[0] : list
      if (!fabrication.value) {
        error.value = `Aucune fabrication trouvée pour l'ID ${id}`
        return
      }
    } else {
      error.value = payload.message || "Erreur de chargement"
      return
    }
  } catch (err: any) {
    console.error(err)
    error.value = "Erreur de chargement"
  }
}

const fetchDetails = async () => {
  const id = route.params.id as string
  try {
    const detailsResp = await axios.get('/FabricationServlet', { params: { action: 'details', id } })
    let detailsPayload: any = detailsResp.data
    if (typeof detailsResp.data === 'string') {
      try { 
        detailsPayload = JSON.parse(detailsResp.data) 
      } catch (e) {
        const raw = detailsResp.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) { 
          detailsPayload = JSON.parse(raw.substring(start, end + 1)) 
        } else { 
          throw e 
        }
      }
    }
    if (detailsPayload.status === 'success') {
      details.value = detailsPayload.data || []
    }
  } catch (err) {
    console.error("Erreur détails:", err)
  }
}

const fetchMouvements = async () => {
  const id = route.params.id as string
  try {
    const mvtResp = await axios.get('/FabricationServlet', { params: { action: 'mouvements', id } })
    let mvtPayload: any = mvtResp.data
    if (typeof mvtResp.data === 'string') {
      try { 
        mvtPayload = JSON.parse(mvtResp.data) 
      } catch (e) {
        const raw = mvtResp.data as string, start = raw.indexOf('{'), end = raw.lastIndexOf('}')
        if (start !== -1 && end !== -1 && end > start) { 
          mvtPayload = JSON.parse(raw.substring(start, end + 1)) 
        } else { 
          throw e 
        }
      }
    }
    if (mvtPayload.status === 'success') {
      mouvements.value = mvtPayload.data || []
    }
  } catch (err) {
    console.error("Erreur mouvements:", err)
  }
}

const fetchCharges = async () => {
  const id = route.params.id as string
  try {
    const resp = await axios.get('/ChargeServlet', { params: { action: 'list', idFabrication: id } })
    if (resp.data.status === 'success') {
      charges.value = resp.data.data || []
    }
  } catch (err) {
    console.error("Erreur charges:", err)
  }
}

const selectTab = (tabId: string) => {
  activeTab.value = tabId
}

watch(activeTab, (newTab) => {
  if (newTab === 'details' && details.value.length === 0) {
    fetchDetails()
  } else if (newTab === 'mouvements') {
    fetchMouvements()
  } else if (newTab === 'charges') {
    fetchCharges()
  }
})

onMounted(async () => {
  loading.value = true
  error.value = null
  await fetchFabrication()
  if (fabrication.value) {
    await Promise.all([
      fetchDetails(),
      fetchMouvements(),
      fetchCharges()
    ])
  }
  loading.value = false
})
</script>

<style scoped>
.fabrication-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Header Section */
.header-section {
  margin-bottom: 32px;
}

.breadcrumb-container {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #64748b;
  font-size: 14px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #475569;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.back-button:hover {
  background-color: #f1f5f9;
  color: #1e293b;
}

.back-icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.breadcrumb-separator {
  color: #cbd5e1;
}

.breadcrumb-current {
  color: #1e293b;
  font-weight: 500;
}

.header-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-created {
  background-color: #dbeafe;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.status-validated {
  background-color: #d1fae5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.status-unknown {
  background-color: #f3f4f6;
  color: #6b7280;
  border: 1px solid #e5e7eb;
}

.fabrication-id {
  font-size: 14px;
  color: #64748b;
}

/* Loading State */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #64748b;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error State */
.error-container {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 24px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 12px;
  margin: 20px 0;
}

.error-icon {
  width: 24px;
  height: 24px;
  fill: #dc2626;
  flex-shrink: 0;
  margin-top: 2px;
}

.error-content h3 {
  margin: 0 0 8px 0;
  color: #991b1b;
  font-size: 16px;
  font-weight: 600;
}

.error-content p {
  margin: 0;
  color: #7f1d1d;
  font-size: 14px;
  line-height: 1.5;
}

/* Information Cards */
.info-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.info-card,
.financial-card,
.actions-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}

.info-card:hover,
.financial-card:hover,
.actions-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.card-icon {
  width: 20px;
  height: 20px;
  fill: #3b82f6;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.info-item.full-width {
  flex-direction: column;
  gap: 8px;
  align-items: stretch;
}

.info-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  text-align: right;
  max-width: 60%;
}

.remarks-box {
  padding: 12px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}

/* Financial Card */
.financial-total {
  margin-bottom: 20px;
}

.total-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.total-amount {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.financial-breakdown {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 8px;
}

.breakdown-label {
  font-size: 13px;
  color: #475569;
}

.breakdown-amount {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

/* Actions Card */
.primary-action-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.2s ease;
  margin-bottom: 16px;
}

.primary-action-button:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.button-icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.secondary-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 10px;
}

.secondary-button {
  padding: 10px 14px;
  background-color: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
}

.secondary-button:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
  transform: translateY(-1px);
}

/* Tabs */
.tabs-container {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tabs-header {
  display: flex;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.tab-button {
  position: relative;
  padding: 18px 24px;
  background: none;
  border: none;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-button:hover {
  color: #475569;
  background-color: rgba(241, 245, 249, 0.5);
}

.tab-button.active {
  color: #3b82f6;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background-color: transparent;
  transition: background-color 0.2s ease;
}

.tab-indicator.active {
  background-color: #3b82f6;
}

.tab-content {
  padding: 0;
}

.tab-pane {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Tables */
.table-container {
  padding: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.table-subtitle {
  font-size: 14px;
  color: #64748b;
}

.total-label {
  font-size: 14px;
  font-weight: 600;
  color: #3b82f6;
}

.table {
  display: table;
  width: 100%;
  border-collapse: collapse;
}

.table-row {
  display: table-row;
  transition: background-color 0.2s ease;
}

.table-row.header {
  background-color: #f8fafc;
  border-bottom: 2px solid #e2e8f0;
}

.table-row:not(.header):hover {
  background-color: #f8fafc;
}

.table-cell {
  display: table-cell;
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.table-row.header .table-cell {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #64748b;
  padding: 12px 16px;
}

.text-right {
  text-align: right;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  grid-column: 1 / -1;
}

.empty-icon {
  width: 48px;
  height: 48px;
  fill: #cbd5e1;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0;
  color: #94a3b8;
  font-size: 14px;
}

/* Specific Cells */
.ingredient-cell {
  display: flex;
  flex-direction: column;
}

.ingredient-id {
  font-weight: 600;
  color: #1e293b;
}

.quantity-value {
  font-weight: 600;
  color: #1e293b;
}

.unit-badge {
  display: inline-block;
  padding: 4px 8px;
  background-color: #e2e8f0;
  color: #475569;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.amount-cell {
  font-weight: 600;
  color: #1e293b;
}

.reference-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ref-id {
  font-weight: 600;
  color: #1e293b;
}

.ref-date {
  font-size: 12px;
  color: #94a3b8;
}

.type-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.type-entry {
  background-color: #d1fae5;
  color: #047857;
}

.type-exit {
  background-color: #fee2e2;
  color: #991b1b;
}

.type-residue {
  background-color: #fef3c7;
  color: #92400e;
}

.status-badge-small {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.description-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Table Summary */
.table-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 16px;
  background-color: #f8fafc;
  border-top: 2px solid #e2e8f0;
}

.summary-label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.summary-amount {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

/* Responsive */
@media (max-width: 768px) {
  .fabrication-view {
    padding: 16px;
  }
  
  .info-cards-grid {
    grid-template-columns: 1fr;
  }
  
  .tabs-header {
    overflow-x: auto;
    flex-wrap: nowrap;
  }
  
  .tab-button {
    padding: 16px;
    white-space: nowrap;
  }
  
  .table-container {
    padding: 16px;
    overflow-x: auto;
  }
  
  .table {
    min-width: 800px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 24px;
  }
  
  .secondary-actions {
    grid-template-columns: 1fr;
  }
}
</style>