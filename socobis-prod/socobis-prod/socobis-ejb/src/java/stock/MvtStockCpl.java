package stock;

import bean.CGenUtil;
import java.sql.Connection;

public class MvtStockCpl extends MvtStock {

    private String libelleTypeMvtStock;
    private String libelleMagasin;
    private double montant;

    public MvtStockCpl() throws Exception {
        super();
        this.setNomTable("V_MVTSTOCK_CPL_TEMP"); // Utiliser une vue si possible, sinon on calcule à la volée
    }

    public String getLibelleTypeMvtStock() {
        return libelleTypeMvtStock;
    }

    public void setLibelleTypeMvtStock(String libelleTypeMvtStock) {
        this.libelleTypeMvtStock = libelleTypeMvtStock;
    }

    public String getLibelleMagasin() {
        return libelleMagasin;
    }

    public void setLibelleMagasin(String libelleMagasin) {
        this.libelleMagasin = libelleMagasin;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public static MvtStockCpl[] getMouvementsForFabrication(String idFabrication, Connection c) throws Exception {
        MvtStock mvtSearch = new MvtStock();
        mvtSearch.setNomTable("MVTSTOCK");
        mvtSearch.setIdobjet(idFabrication);

        MvtStock[] mouvements = (MvtStock[]) CGenUtil.rechercher(mvtSearch, null, null, c, " ORDER BY daty DESC");
        MvtStockCpl[] result = new MvtStockCpl[mouvements.length];

        for (int i = 0; i < mouvements.length; i++) {
            MvtStock mvt = mouvements[i];
            MvtStockCpl cpl = new MvtStockCpl();
            
            // Copier les propriétés de base
            cpl.setId(mvt.getId());
            cpl.setDaty(mvt.getDaty());
            cpl.setDesignation(mvt.getDesignation());
            cpl.setIdTypeMvStock(mvt.getIdTypeMvStock());
            cpl.setIdMagasin(mvt.getIdMagasin());
            cpl.setIdVente(mvt.getIdVente());
            cpl.setEtat(mvt.getEtat());

            // Enrichir avec les libellés
            TypeMvtStock type = new TypeMvtStock();
            type = (TypeMvtStock) type.getById(mvt.getIdTypeMvStock(), type.getNomTable(), c);
            if (type != null) {
                cpl.setLibelleTypeMvtStock(type.getVal());
            }

            magasin.Magasin mag = new magasin.Magasin();
            mag = (magasin.Magasin) mag.getById(mvt.getIdMagasin(), mag.getNomTable(), c);
            if (mag != null) {
                cpl.setLibelleMagasin(mag.getValColLibelle());
            }

            // Calculer le montant total
            MvtStockFille[] filles = mvt.getMvtStockFilles(c);
            double total = 0;
            if (filles != null) {
                for (MvtStockFille fille : filles) {
                    total += (fille.getEntree() + fille.getSortie()) * fille.getPu();
                }
            }
            cpl.setMontant(total);

            result[i] = cpl;
        }

        return result;
    }

    public static MvtStockCpl getSingleMouvement(String idMouvement, Connection c) throws Exception {
        MvtStock mvt = (MvtStock) new MvtStock().getById(idMouvement, "MVTSTOCK", c);
        if (mvt == null) {
            return null;
        }

        MvtStockCpl cpl = new MvtStockCpl();
        // Copier les propriétés de base
        cpl.setId(mvt.getId());
        cpl.setDaty(mvt.getDaty());
        cpl.setDesignation(mvt.getDesignation());
        cpl.setIdTypeMvStock(mvt.getIdTypeMvStock());
        cpl.setIdMagasin(mvt.getIdMagasin());
        cpl.setIdVente(mvt.getIdVente());
        cpl.setIdobjet(mvt.getIdobjet());
        cpl.setEtat(mvt.getEtat());

        // Enrichir avec les libellés
        TypeMvtStock type = new TypeMvtStock();
        type = (TypeMvtStock) type.getById(mvt.getIdTypeMvStock(), type.getNomTable(), c);
        if (type != null) {
            cpl.setLibelleTypeMvtStock(type.getVal());
        }

        magasin.Magasin mag = new magasin.Magasin();
        mag = (magasin.Magasin) mag.getById(mvt.getIdMagasin(), mag.getNomTable(), c);
        if (mag != null) {
            cpl.setLibelleMagasin(mag.getValColLibelle());
        }

        // Récupérer et attacher les filles
        MvtStockFille[] filles = mvt.getMvtStockFilles(c);
        cpl.setFille(filles);

        // Calculer le montant total
        double total = 0;
        if (filles != null) {
            for (MvtStockFille fille : filles) {
                total += (fille.getEntree() + fille.getSortie()) * fille.getPu();
            }
        }
        cpl.setMontant(total);

        return cpl;
    }
}
