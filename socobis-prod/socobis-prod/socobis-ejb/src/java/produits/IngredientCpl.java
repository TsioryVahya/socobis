package produits;

import bean.CGenUtil;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

public class IngredientCpl extends Ingredients {

    private List<RecetteLib> composition;
    private List<Recette> decompositionFinale;
    private String categorieIngredientLib;
    private List<RecetteLib> autresComposants;
    private List<HistoriquePrixIng> historiquePu;
    private List<HistoriquePrixIng> historiquePv;
    private List<TarifIngredientsLib> historiqueTarif;

    public IngredientCpl() throws Exception {
        super();
    }

    public List<RecetteLib> getComposition() {
        return composition;
    }

    public void setComposition(List<RecetteLib> composition) {
        this.composition = composition;
    }

    public List<Recette> getDecompositionFinale() {
        return decompositionFinale;
    }

    public void setDecompositionFinale(List<Recette> decompositionFinale) {
        this.decompositionFinale = decompositionFinale;
    }

    public String getCategorieIngredientLib() {
        return categorieIngredientLib;
    }

    public void setCategorieIngredientLib(String categorieIngredientLib) {
        this.categorieIngredientLib = categorieIngredientLib;
    }

    // Getters and Setters for new fields
    public List<RecetteLib> getAutresComposants() { return autresComposants; }
    public void setAutresComposants(List<RecetteLib> autresComposants) { this.autresComposants = autresComposants; }
    public List<HistoriquePrixIng> getHistoriquePu() { return historiquePu; }
    public void setHistoriquePu(List<HistoriquePrixIng> historiquePu) { this.historiquePu = historiquePu; }
    public List<HistoriquePrixIng> getHistoriquePv() { return historiquePv; }
    public void setHistoriquePv(List<HistoriquePrixIng> historiquePv) { this.historiquePv = historiquePv; }
    public List<TarifIngredientsLib> getHistoriqueTarif() { return historiqueTarif; }
    public void setHistoriqueTarif(List<TarifIngredientsLib> historiqueTarif) { this.historiqueTarif = historiqueTarif; }

    public static IngredientCpl getDetailIngredient(String idIngredient, Connection c) throws Exception {
        Ingredients ingredientBase = (Ingredients) new Ingredients().getById(idIngredient, "AS_INGREDIENTS_LIB", c);
        if (ingredientBase == null) {
            throw new Exception("Ingrédient non trouvé: " + idIngredient);
        }

        IngredientCpl cpl = new IngredientCpl();

        // Copier les propriétés de base
        cpl.setId(ingredientBase.getId());
        cpl.setLibelle(ingredientBase.getLibelle());
        cpl.setParfums(ingredientBase.getParfums());
        cpl.setSeuil(ingredientBase.getSeuil());
        cpl.setUnite(ingredientBase.getUnite());
        cpl.setQuantiteParPack(ingredientBase.getQuantiteParPack());
        cpl.setRevient(ingredientBase.getRevient());
        cpl.setActif(ingredientBase.getActif());
        cpl.setCompose(ingredientBase.getCompose());
        cpl.setCategorieIngredient(ingredientBase.getCategorieIngredient());
        cpl.setCompte_vente(ingredientBase.getCompte_vente());
        cpl.setCompte_achat(ingredientBase.getCompte_achat());
        cpl.setPv(ingredientBase.getPv());
        cpl.setTva(ingredientBase.getTva());
        cpl.setTypeStock(ingredientBase.getTypeStock());
        cpl.setIdFamille(ingredientBase.getIdFamille());

        // Enrichir avec les libellés
        if (ingredientBase.getCategorieIngredient() != null) {
            CategorieIngredient cat = new CategorieIngredient();
            cat = (CategorieIngredient) cat.getById(ingredientBase.getCategorieIngredient(), cat.getNomTable(), c);
            if(cat != null) cpl.setCategorieIngredientLib(cat.getVal());
        }

        // Récupérer la composition (recette directe)
        RecetteLib[] compositionArray = ingredientBase.getRecette("recettelib", c);
        if (compositionArray != null) {
            cpl.setComposition(Arrays.asList(compositionArray));
        }

        // Récupérer la décomposition finale
        Recette[] decompositionArray = ingredientBase.decomposerBase(c);
        if (decompositionArray != null) {
            cpl.setDecompositionFinale(Arrays.asList(decompositionArray));
        }

        // Récupérer les autres composants concernés
        RecetteLib[] autresComposantsArray = ingredientBase.getRecetteIngredient("recettelib", c);
        if (autresComposantsArray != null) {
            cpl.setAutresComposants(Arrays.asList(autresComposantsArray));
        }

        // Récupérer les historiques
        HistoriquePrixIng[] puHistory = ingredientBase.getHistoriquePu(c, "pu", null);
        if (puHistory != null) {
            cpl.setHistoriquePu(Arrays.asList(puHistory));
        }

        HistoriquePrixIng[] pvHistory = ingredientBase.getHistoriquePu(c, "pv", null);
        if (pvHistory != null) {
            cpl.setHistoriquePv(Arrays.asList(pvHistory));
        }

        TarifIngredientsLib[] tarifHistory = ingredientBase.getHistoriqueTarif(c, null);
        if (tarifHistory != null) {
            cpl.setHistoriqueTarif(Arrays.asList(tarifHistory));
        }

        return cpl;
    }
}
