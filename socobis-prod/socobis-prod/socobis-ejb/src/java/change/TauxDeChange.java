package change;

import java.sql.Connection;
import java.sql.Timestamp;

import bean.CGenUtil;
import bean.ClassMAPTable;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;
import vente.VenteLib;
import faturefournisseur.FactureFournisseurCpl;
import prevision.PrevisionComplet;

public class TauxDeChange extends ClassMAPTable {
    String id;
    String idDevise;
    double taux;
    java.sql.Date daty;

    public TauxDeChange(){
        super.setNomTable("tauxdechange");
    }

    // Récupère le taux à utiliser selon la Prévision liée à l'origine (vente VNT... ou facture fournisseur FCF...)
    // Stratégie: chercher la prévision (PrevisionComplet) dont IDFACTURE = idOrigine.
    // - Si aucune prévision, retourner 1
    // - Si la prévision est en AR, retourner 1
    // - Sinon retourner le dernier taux connu à la date de la prévision pour la devise de la prévision
    public static double getTauxPrevision(Connection c, String idOrigine) throws Exception {
        boolean estOuvert = false;
        if (c == null) {
            c = new UtilDB().GetConn();
            estOuvert = true;
        }
        try{
            if(idOrigine==null || idOrigine.trim().isEmpty()) return 1;
            PrevisionComplet crit = new PrevisionComplet();
            // Rechercher les prévisions liées à l'origine (dépense pour FCF, recette pour VNT)
            PrevisionComplet[] pcs = (PrevisionComplet[]) CGenUtil.rechercher(crit, null, null, c,
                    " and IDFACTURE='"+idOrigine+"' order by daty desc");
            if(pcs==null || pcs.length==0) return 1;
            String idDev = pcs[0].getIdDevise();
            String datyPrev = Utilitaire.formatterDaty(pcs[0].getDaty());
            if(idDev==null || idDev.trim().isEmpty() || idDev.equalsIgnoreCase("AR")) return 1;
            return getLastTaux(c, datyPrev, idDev);
        } catch (Exception e){
            e.printStackTrace();
            throw e;
            
        } finally {
            if(estOuvert && c!=null) c.close();
        }
    }

    // Récupère le taux applicable à une facture fournisseur: dernier taux <= date de la facture, pour la devise de la facture
    public static double getTauxForFF(Connection c, String idFF) throws Exception{
        boolean estOuvert = false;
        if(c == null) {
            c = new UtilDB().GetConn();
            estOuvert = true;
        }
        try {
            if(idFF==null || idFF.trim().isEmpty()) return 1;
            FactureFournisseurCpl crit = new FactureFournisseurCpl();
            crit.setId(idFF);
            FactureFournisseurCpl[] ffs = (FactureFournisseurCpl[]) CGenUtil.rechercher(crit, null, null, c, " and id='"+idFF+"'");
            if(ffs==null || ffs.length==0) return 1;
            String idDev = ffs[0].getIdDevise();
            if(idDev==null || idDev.trim().isEmpty() || idDev.equalsIgnoreCase("AR")) return 1;
            String daty = Utilitaire.formatterDaty(ffs[0].getDaty());
            return getLastTaux(c, daty, idDev);
        } catch (Exception e) {
            e.printStackTrace();
            if(estOuvert && c!=null) c.close();
            throw e;
        } finally {
            if(estOuvert && c!=null) c.close();
        }
    }

    // Récupère le taux applicable à une vente: dernier taux <= date de la vente, pour la devise de la vente
    public static double getTauxForVente(Connection c, String idVente) throws Exception{
        boolean estOuvert = false;
        if(c == null) {
            c = new UtilDB().GetConn();
            estOuvert = true;
        }
        try {
            if(idVente==null || idVente.trim().isEmpty()) return 1;
            VenteLib crit = new VenteLib();
            crit.setId(idVente);
            VenteLib[] ventes = (VenteLib[]) CGenUtil.rechercher(crit, null, null, c, " and id='"+idVente+"'");
            if(ventes==null || ventes.length==0) return 1;
            String idDev = ventes[0].getIdDevise();
            if(idDev==null || idDev.trim().isEmpty() || idDev.equalsIgnoreCase("AR")) return 1;
            String daty = Utilitaire.formatterDaty(ventes[0].getDaty());
            return getLastTaux(c, daty, idDev);
        } catch (Exception e) {
            e.printStackTrace();
            if(estOuvert && c!=null) c.close();
            throw e;
        } finally {
            if(estOuvert && c!=null) c.close();
        }
    }

    @Override
    public void construirePK(Connection c) throws Exception {
        this.preparePk("TX", "GETSEQTAUXDECHANGE");
        this.setId(makePK(c));
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIdDevise() {
        return idDevise;
    }
    public void setIdDevise(String idDevise) {
        this.idDevise = idDevise;
    }
    public double getTaux() {
        
        return taux;
    }
    public void setTaux(double taux) throws Exception {
        if(this.getMode().compareTo("modif")==0)
        {
            if(taux<=0)
                throw new Exception("taux non valide");
        }
        this.taux = taux;
    }
    public java.sql.Date getDaty() {
        return daty;
    }
    public void setDaty(java.sql.Date daty) throws Exception {
        if(this.getMode().compareTo("modif")==0)
        {
            if(daty==null)
                throw new Exception("Date obligatoire");
        }
        this.daty = daty;
    }
    @Override
    public String getAttributIDName() {
        // TODO Auto-generated method stub
        return "id";
    }
    @Override
    public String getTuppleID() {
        // TODO Auto-generated method stub
        return this.id;
    }

    @Override
    public void controler(Connection c) throws Exception {
        // Raha efa misy meme date aminy de meme devise de mthrows Exception
        TauxDeChange tc = new TauxDeChange();
        tc.setIdDevise(this.getIdDevise());
        TauxDeChange[] tableTaux = (TauxDeChange[])CGenUtil.rechercher(tc, null, null, c, " and daty ='" + utilitaire.Utilitaire.formatterDaty(this.getDaty()) + "'");
        if( tableTaux.length > 0 ){
            throw new Exception("Taux de change déja existant pour cette date et devise");
        }
    }

    public static double getLastTaux(Connection c, String daty, String iddevise) throws Exception{
        boolean estOuvert = false;
        if(c == null) {
            c = new UtilDB().GetConn();
            estOuvert = true;
        }
        try {
            if(daty==null||daty.isEmpty()) daty = Utilitaire.dateDuJour();
            String req = "select *\n" +
            "from TAUXDECHANGE t1\n" +
            "where t1.DATY = (select max(t2.daty) from TAUXDECHANGE t2 where t2.IDDEVISE = t1.IDDEVISE and t2.daty <= '"+daty+"')\n" +
            "  and t1.IDDEVISE = '"+iddevise+"' ";
            TauxDeChange[] taux = (TauxDeChange[]) CGenUtil.rechercher(new TauxDeChange(), req, c);
            return taux.length > 0 ? taux[0].getTaux() : 1; 
        } catch (Exception e) {
            e.printStackTrace();
            c.close();
            throw e;
        } finally {
            c.close();
        }
    }


}
