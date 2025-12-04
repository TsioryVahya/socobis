<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="caisse.Caisse"%>
<%@page import="vente.InsertionVente"%>
<%@page import="vente.VenteDetails"%>
<%@page import="bean.TypeObjet"%>
<%@page import="user.*"%>
<%@ page import="bean.*" %>
<%@page import="affichage.*"%>
<%@page import="utilitaire.*"%>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="caisse.Devise" %>
<%@ page import="annexe.Point" %>
<%@ page import="faturefournisseur.*" %>
<%
    try {
        UserEJB u = null;
        u = (UserEJB) session.getValue("u");
        FactureFournisseur mere = new FactureFournisseur();
        FactureFournisseurDetails fille = new FactureFournisseurDetails();
        fille.setNomTable("FactureFournisseurFille");

        FactureFournisseur fournisseur = null;
        FactureFournisseurDetails[] details = null;
        As_BonDeLivraison bonDeLivraison = new As_BonDeLivraison();
        As_BonDeCommande bonDeCommande = new As_BonDeCommande();
        if(request.getParameter("id")!=null){
            bonDeLivraison.setId(request.getParameter("id"));
            details = bonDeLivraison.getDetailsFacture(request.getParameter("id"),null);
            bonDeLivraison = (As_BonDeLivraison)bonDeLivraison.getById(request.getParameter("id"),"AS_BONDELIVRAISON",null);
            fournisseur = bonDeLivraison.genererFacture();
        }else if(request.getParameter("idbc")!=null){
            bonDeCommande.setId(request.getParameter("idbc"));
            details = bonDeCommande.getDetailsFacture(request.getParameter("idbc"),null);
            bonDeCommande = (As_BonDeCommande)bonDeCommande.getById(request.getParameter("idbc"),"AS_BONDECOMMANDE",null);
            fournisseur = bonDeCommande.genererFacture();
        }
        int nombreLigne = 10;
        PageInsertMultiple pi = new PageInsertMultiple(mere, fille, request, nombreLigne, u);
        pi.setLien((String) session.getValue("lien"));

        Liste[] liste = new Liste[4];
        Point mag = new Point();
        mag.setNomTable("point");
        liste[0] = new Liste("idMagasin",mag,"val","id");
        Devise d = new Devise();
        liste[1] = new Liste("idDevise",d,"val","id");

        liste[1].setDefaut("AR");
        ModePaiement mp = new ModePaiement();
        liste[2] = new Liste("idModePaiement",mp,"val","id");

        liste[3] = new Liste("estPrevu");
        liste[3].makeListeOuiNon();

        pi.getFormu().changerEnChamp(liste);
        pi.getFormu().getChamp("etat").setVisible(false);
        pi.getFormu().getChamp("idMagasin").setLibelle("Magasin");
        pi.getFormu().getChamp("designation").setLibelle("D&eacute;signation");
        pi.getFormu().getChamp("idModePaiement").setLibelle("Mode de paiement");

        pi.getFormu().getChamp("daty").setLibelle("Date");
        pi.getFormu().getChamp("idFournisseur").setLibelle("Fournisseur");
        pi.getFormu().getChamp("idFournisseur").setPageAppelComplete("faturefournisseur.Fournisseur","id","fournisseur");
        pi.getFormu().getChamp("idFournisseur").setPageAppelInsert("fournisseur/fournisseur-saisie.jsp","idFournisseur;idFournisseurlibelle","id;nom");

        String[] ordre={"daty"};
        pi.getFormu().setOrdre(ordre);
        String ac_affiche_val = "null";
        String ac_valeur_val = "id";
        String ac_colFiltre_val = "null";
        String ac_nomTable_val = "ST_INGREDIENTSAUTOVENTE_CPL";
        String ac_classe_val = "produits.IngredientsLib";
        String ac_useMotcle_val = "true";
        String ac_champRetour_val = "pv;compte_achat;compte_achat;libelleComposant";
        String dependentFieldsToMap_str_val = "pu;compte;comptelibelle;designation";

        String onChangeParam = "dynamicAutocompleteDependant(this, " +
                "\"IDFOURNISSEUR\", " +
                "\"LIKE\", " +
                "\"idProduit\", " +
                "\"" + nombreLigne + "\", " +
                "\"" + ac_affiche_val + "\", " +
                "\"" + ac_valeur_val + "\", " +
                "\"" + ac_colFiltre_val + "\", " +
                "\"" + ac_nomTable_val + "\", " +
                "\"" + ac_classe_val + "\", " +
                "\"" + ac_useMotcle_val + "\", " +
                "\"" + ac_champRetour_val + "\", " +
                "\"" + dependentFieldsToMap_str_val + "\"" + // Last parameter
                ")";

        //pi.getFormu().getChamp("idFournisseur").setAutre("onChange='" + onChangeParam + "'");
        //pi.getFormu().getChamp("idFournisseur").setAutre("onchange=\"updateFille(event, 'formId')\"");
        //pi.getFormu().getChamp("idDevise").setAutre("onchange=\"updateFille(event, 'formId')\"");

        pi.getFormu().getChamp("idDevise").setLibelle("Devise");
        pi.getFormu().getChamp("dateEcheancePaiement").setVisible(false);
        pi.getFormu().getChamp("idDevise").setAutre("onChange='deviseModification()'");
        pi.getFormu().getChamp("idBc").setVisible(false);
        pi.getFormu().getChamp("devise").setVisible(false);
        pi.getFormu().getChamp("taux").setVisible(false);
        pi.getFormu().getChamp("reference").setLibelle("R&eacute;f&eacute;rence");

        pi.getFormu().getChamp("estPrevu").setLibelle("Est pr&eacute;vu");
        pi.getFormu().getChamp("datyPrevu").setLibelle("Date pr&eacute;visionnelle de paiement");
        affichage.Champ.setPageAppelComplete(pi.getFormufle().getChampFille("idProduit"),"produits.IngredientsLib","id","ST_INGREDIENTSAUTOACHAT_CPL","pu;taux;compte_achat;compte_achat","pu;tauxDeChange;compte;comptelibelle");
        affichage.Champ.setPageAppelInsert(pi.getFormufle().getChampFille("idProduit"),"produits/as-ingredients-saisie.jsp","id;libelle");
        affichage.Champ.setPageAppelComplete(pi.getFormufle().getChampFille("compte"),"mg.cnaps.compta.ComptaCompte","compte","compte6","","");

        pi.getFormufle().getChamp("idProduit_0").setLibelle("Produit");
        pi.getFormufle().getChamp("tva_0").setLibelle("TVA");
        pi.getFormufle().getChamp("remises_0").setLibelle("remise");
        pi.getFormufle().getChamp("qte_0").setLibelle("Quantit&eacute;");
        pi.getFormufle().getChamp("pu_0").setLibelle("Prix unitaire");
        pi.getFormufle().getChamp("compte_0").setLibelle("Compte");
        //pi.getFormufle().getChamp("idDevise_0").setVisible(false);
        pi.getFormufle().getChamp("idDevise_0").setLibelle("Devise");
        pi.getFormufle().getChamp("tauxDeChange_0").setLibelle("Taux de change");

        pi.getFormufle().getChamp("mois_0").setLibelle("Mois");
        pi.getFormufle().getChamp("annee_0").setLibelle("Ann&eacute;e");

        pi.getFormufle().getChampMulitple("idFactureFournisseur").setVisible(false);
        pi.getFormufle().getChampMulitple("id").setVisible(false);
        pi.getFormufle().getChampMulitple("idbcDetail").setVisible(false);
        pi.getFormufle().getChampMulitple("idbcDevise").setVisible(false);
        pi.getFormufle().getChampMulitple("mois").setVisible(false);
        pi.getFormufle().getChampMulitple("annee").setVisible(false);

        pi.preparerDataFormu();
        for(int i=0;i<nombreLigne;i++){
            pi.getFormufle().getChamp("qte_"+i).setAutre("onChange='calculerMontant("+i+")'");
            pi.getFormufle().getChamp("qte_"+i).setDefaut("0");
            pi.getFormufle().getChamp("tva_"+i).setDefaut("0");
            //pi.getFormufle().getChamp("tva_"+i).setVisible(false);
            pi.getFormufle().getChamp("idDevise_"+i).setAutre("readonly");
            //pi.getFormufle().getChamp("tauxDeChange_"+i).setVisible(false);
            pi.getFormufle().getChamp("idDevise_"+i).setDefaut("AR");
            //pi.getFormufle().getChamp("compte_"+i).setAutre("readonly");
        }

        FactureFournisseur ocr = (FactureFournisseur) session.getAttribute("ocr");
        if(ocr!=null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = ocr.getDaty().toLocalDate();
            String formattedDate = localDate.format(formatter);
            pi.getFormu().getChamp("designation").setDefaut(ocr.getDesignation());
            pi.getFormu().getChamp("daty").setDefaut(formattedDate);
            if(ocr.getIdFournisseur()!=null){
                pi.getFormu().getChamp("idFournisseur").setDefaut(ocr.getIdFournisseur());
            }
            if(ocr.getIdMagasin()!=null){
                pi.getFormu().getChamp("idMagasin").setDefaut(ocr.getIdMagasin());
            }
            pi.setDefautFille(ocr.getFille());
        }

        if(details!=null && details.length > 0){
            if(request.getParameter("idbc")!=null){
                fournisseur.setDesignation("Facturation de la commande num "+request.getParameter("idbc"));
            }
            pi.getFormu().setDefaut(fournisseur);
            pi.setDefautFille(details);
            pi.getFormu().getChamp("daty").setDefaut(Utilitaire.dateDuJour());
            //pi.getFormu().getChamp("designation").setDefaut("Facturation de la commande num "+request.getParameter("idbc"));
        }

        session.removeAttribute("ocr");

        String iddmdachat = request.getParameter("iddmdachat");
        if (iddmdachat!=null && !iddmdachat.isEmpty()) {
            DmdAchat dmdAchat = (DmdAchat) new DmdAchat().getById(iddmdachat,"DMDACHAT",null);
            if (dmdAchat!=null){
                pi.getFormu().getChamp("daty").setDefaut(Utilitaire.datetostring(dmdAchat.getDaty()));
                pi.getFormu().getChamp("idFournisseur").setDefaut(dmdAchat.getFournisseur());
                pi.getFormu().getChamp("designation").setDefaut("Facture de la demande d'achat "+iddmdachat);
                FactureFournisseurDetails[] factureFournisseurDetails = dmdAchat.getFactureFournisseurDetails(null);
                pi.setDefautFille(factureFournisseurDetails);
            }
        }

        //Variables de navigation
        String classeMere = "faturefournisseur.FactureFournisseur";
        String classeFille = "faturefournisseur.FactureFournisseurDetails";
        String butApresPost = "facturefournisseur/facturefournisseur-fiche.jsp";
        String colonneMere = "idFactureFournisseur";
        //Preparer les affichages
        String[] colOrdre = {"id","idProduit","compte","qte","pu","tva","idDevise","tauxDeChange","mois","annee"};
        pi.getFormufle().setColOrdre(colOrdre);
        pi.getFormu().makeHtmlInsertTabIndex();
        pi.getFormufle().makeHtmlInsertTableauIndex();

%>
<div class="content-wrapper">
    <%if (request.getParameter("acte") != null && request.getParameter("acte").equalsIgnoreCase("update")) {%>
    <h1>Modification D&eacute;pense  / Facture Fournisseur</h1>
    <% }else{ %>
    <h1>D&eacute;pense / Facture Fournisseur</h1>
    <% } %>
    <div class="box-body">
        <form id="formId" class='container' action="<%=pi.getLien()%>?but=apresMultiple.jsp" method="post" >
            <%

                out.println(pi.getFormu().getHtmlInsert());
            %>
            <div class="col-md-12" >
                <h3 class="fontinter" style="background: white;padding: 16px;margin-top: 10px;border-radius: 16px;" >Total  : <span id="montanttotal">0</span><span id="deviseLibelle">Ar</span></h3>
            </div>
            <div id="butfillejsp">
                <%
                    out.println(pi.getFormufle().getHtmlTableauInsert());
                %>
            </div>

            <input name="acte" type="hidden" id="nature" value="insert">
            <input name="bute" type="hidden" id="bute" value="<%= butApresPost %>">
            <input name="classe" type="hidden" id="classe" value="<%= classeMere %>">
            <input name="classefille" type="hidden" id="classefille" value="<%= classeFille %>">
            <input name="nombreLigne" type="hidden" id="nombreLigne" value="<%= nombreLigne %>">
            <input name="colonneMere" type="hidden" id="colonneMere" value="<%= colonneMere %>">
        </form>
        <script>
            // Appel automatique de calculerMontant pour chaque ligne au chargement
            document.addEventListener("DOMContentLoaded", function() {
                var nombreLigne = <%=nombreLigne%>;
                for(var i=0;i<nombreLigne;i++){
                    calculerMontant(i);
                }
            });
        </script>
    </div>
</div>
<script>
    function calculerMontant(indice,source) {
        var val = 0;
        $('input[id^="qte_"]').each(function() {
            var quantite =  parseFloat($("#"+$(this).attr('id').replace("qte","pu")).val());
            var montant = parseFloat($(this).val());
            if(!isNaN(quantite) && !isNaN(montant)){
                var value =quantite * montant;
                val += value;
            }
        });
        $("#montanttotal").html(Intl.NumberFormat('fr-FR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(val));
    }
    function deviseModification() {
        var nombreLigne = parseInt($("#nombreLigne").val());
        var idDevise = $('#idDevise').val();
        var daty = $('input[name="daty"]').val() || '';
        $("#deviseLibelle").html(idDevise);
        // Récupérer le taux pour la devise sélectionnée
        var url = '<%= request.getContextPath() %>/DeviseServlet?idDevise=' + encodeURIComponent(idDevise) + '&daty=' + encodeURIComponent(daty);
        fetch(url, { credentials: 'same-origin' })
            .then(function(r){ return r.json(); })
            .then(function(data){
                var newTaux = (data && typeof data.taux !== 'undefined') ? parseFloat(data.taux) : 1;
                if (isNaN(newTaux) || newTaux<=0) newTaux = 1;
                for(let iL=0;iL<nombreLigne;iL++){
                    // Mettre à jour la devise de la ligne
                    $("#idDevise_"+iL).val(idDevise);
                    // Convertir le PU existant selon ancien et nouveau taux
                    var puStr = $("#pu_"+iL).val();
                    var oldTaux = parseFloat($("#tauxDeChange_"+iL).val());
                    if (isNaN(oldTaux) || oldTaux<=0) oldTaux = 1;
                    if (puStr && puStr.trim().length>0){
                        var pu = parseFloat(puStr.replace(',', '.'));
                        if(!isNaN(pu)){
                            // Convertir PU courant -> AR, puis AR -> nouvelle devise
                            var puAR = pu * oldTaux;
                            var puNew = (idDevise === 'AR') ? puAR : (puAR / newTaux);
                            $("#pu_"+iL).val(puNew.toFixed(2));
                        }
                    }
                    // Affecter le nouveau taux
                    $("#tauxDeChange_"+iL).val(idDevise === 'AR' ? '1' : (''+newTaux));
                }
            })
            .catch(function(err){ console.error('Erreur taux AJAX', err); });
    }
</script>
<%
} catch (Exception e) {
    e.printStackTrace();
%>
<script language="JavaScript">
    alert('<%=e.getMessage()%>');
    history.back();
</script>
<% }%>
