<%@page import="utils.ConstanteStation"%>
<%@page import="affichage.*"%>
<%@page import="caisse.MvtCaisse"%>
<%@page import="caisse.Caisse"%>
<%@page import="user.*"%>
<%@page import="change.TauxDeChange" %>
<%@page import="faturefournisseur.FactureFournisseurCpl" %>
<%@page import="bean.CGenUtil" %>

<%


    try{

        String lien = (String) session.getValue("lien");

        UserEJB user = (UserEJB) session.getValue("u");
        MvtCaisse mouvement = new MvtCaisse();
        PageInsert pageInsert = new PageInsert( mouvement, request, user );
        pageInsert.setLien(lien);
        String  montant=request.getParameter("montant");
        String idOrigine=request.getParameter("idOrigine");
        String devise=request.getParameter("devise");
        String tiers=request.getParameter("tiers");
        String prev=request.getParameter("idPrevision");
        String deviseDef = devise;
        String tauxDef = "1";
        String datyFact = null;
        // Pré-remplir devise/taux à partir de la facture fournisseur
        if(idOrigine!=null && idOrigine.startsWith("FCF")){
            FactureFournisseurCpl f = new FactureFournisseurCpl();
            f.setId(idOrigine);
            FactureFournisseurCpl[] ffs = (FactureFournisseurCpl[]) CGenUtil.rechercher(f, null, null, null, " and id='"+idOrigine+"'");
            if(ffs!=null && ffs.length>0){
                if(ffs[0].getIdDevise()!=null && ffs[0].getIdDevise().trim().length()>0){
                    deviseDef = ffs[0].getIdDevise();
                }
                datyFact = utilitaire.Utilitaire.formatterDaty(ffs[0].getDaty());
                try{
                    double tx = TauxDeChange.getTauxForFF(null, idOrigine);
                    if(tx>0){ tauxDef = String.valueOf(tx); }
                }catch(Exception ignore){}
            }
        } else if(deviseDef!=null && deviseDef.trim().length()>0){
            try{
                double last = TauxDeChange.getLastTaux(null, utilitaire.Utilitaire.dateDuJour(), deviseDef);
                if(last>0){ tauxDef = String.valueOf(last);} 
            }catch(Exception ignore){}
        }

        affichage.Champ[] liste = new affichage.Champ[2];
        // Rendre la devise sélectionnable
        liste[0] = new Liste("idDevise",new caisse.Devise(),"val","id");
        Caisse c = new Caisse();
        //c.setIdPoint(ConstanteStation.getFichierCentre());
        liste[1] = new Liste("idCaisse",c,"val","id");
			
        pageInsert.getFormu().changerEnChamp(liste);
				pageInsert.getFormu().getChamp("designation").setDefaut("Paiement de la facture : "+idOrigine);
        pageInsert.getFormu().getChamp("idCaisse").setLibelle("Caisse");
        pageInsert.getFormu().getChamp("idDevise").setLibelle("Devise");
        pageInsert.getFormu().getChamp("designation").setLibelle("D&eacute;signation");
        pageInsert.getFormu().getChamp("debit").setLibelle("D&eacute;bit");
        pageInsert.getFormu().getChamp("daty").setLibelle("Date");
        // Champ Fournisseur (Tiers)
        pageInsert.getFormu().getChamp("idTiers").setLibelle("Fournisseur");
        pageInsert.getFormu().getChamp("idTiers").setPageAppelComplete("pertegain.Tiers","id","tiers");
        pageInsert.getFormu().getChamp("idTiers").setVisible(true);
        pageInsert.getFormu().getChamp("idDevise").setDefaut(deviseDef);
        pageInsert.getFormu().getChamp("taux").setDefaut(tauxDef);
        pageInsert.getFormu().getChamp("idVirement").setVisible(false);
        pageInsert.getFormu().getChamp("idVenteDetail").setVisible(false);
        pageInsert.getFormu().getChamp("idOp").setVisible(false);
        pageInsert.getFormu().getChamp("idOrigine").setVisible(false);
				pageInsert.getFormu().getChamp("credit").setVisible(false);
        pageInsert.getFormu().getChamp("etat").setVisible(false);
				pageInsert.getFormu().getChamp("idOrigine").setDefaut(idOrigine);
				pageInsert.getFormu().getChamp("idOrigine").setVisible(false);
        pageInsert.getFormu().getChamp("debit").setDefaut(montant);
				pageInsert.getFormu().getChamp("idTiers").setDefaut(tiers);
        pageInsert.getFormu().getChamp("idTiers").setVisible(true);
        pageInsert.getFormu().getChamp("idPrevision").setLibelle("Pr&eacute;vision");
        pageInsert.getFormu().getChamp("idPrevision").setDefaut(prev);
        pageInsert.getFormu().getChamp("idPrevision").setPageAppelComplete("prevision.Prevision", "id", "PREVISION");
        pageInsert.getFormu().getChamp("compte").setLibelle("Compte de regroupement");
        String[] ordre={"daty"};
        pageInsert.getFormu().setOrdre(ordre);

        String classe = "caisse.MvtCaisse";
        String nomTable = "MOUVEMENTCAISSE";
        String butApresPost = "caisse/mvt/mvtCaisse-fiche.jsp";

        pageInsert.preparerDataFormu();
        pageInsert.getFormu().makeHtmlInsertTabIndex();

%>

    <div class="content-wrapper">
        <h1 align="center">D&eacute;caissement</h1>
        <form action="<%=pageInsert.getLien()%>?but=apresTarif.jsp" method="post"  data-parsley-validate>
            <%
                out.println(pageInsert.getFormu().getHtmlInsert());
            %>
            <input name="acte" type="hidden" id="nature" value="insert">
            <input name="bute" type="hidden" id="bute" value="<%= butApresPost %>">
            <input name="classe" type="hidden" id="classe" value="<%= classe %>">
            <input name="nomtable" type="hidden" id="nomtable" value="<%= nomTable %>">
        </form>
        <% if(datyFact!=null){ %>
            <input type="hidden" id="datyFactureHidden" value="<%= datyFact %>" />
        <% } %>
    </div>
    <script>
        (function(){
            var deviseSelect   = document.querySelector('[name="idDevise"]');
            var tauxInput      = document.querySelector('[name="taux"]');
            var datyFact       = document.getElementById('datyFactureHidden');
            var previsionInput = document.querySelector('[name="idPrevision"]');
            var origineInput   = document.querySelector('[name="idOrigine"]');
            function majTaux(){
                var idDev      = deviseSelect.value;
                var daty       = datyFact ? datyFact.value : '';
                var idPrev     = previsionInput ? previsionInput.value : '';
                var idOrig     = origineInput ? origineInput.value : '';
                var url = '<%= request.getContextPath() %>/DeviseServlet'
                        + '?idDevise=' + encodeURIComponent(idDev)
                        + '&daty='     + encodeURIComponent(daty)
                        + (idPrev ? ('&idPrevision=' + encodeURIComponent(idPrev)) : '')
                        + (idOrig ? ('&idOrigine=' + encodeURIComponent(idOrig)) : '');
                console.log('URL DeviseServlet =', url);
                fetch(url, { credentials: 'same-origin' })
                    .then(function(r){ return r.json(); })
                    .then(function(data){ if(data && typeof data.taux!== 'undefined'){ tauxInput.value = data.taux; console.log('DeviseServlet retour =', data);} })
                    .catch(function(err){ console.error('Erreur taux AJAX', err); });
            }
            window.addEventListener('load', majTaux);
            deviseSelect.addEventListener('change', majTaux);
            if (previsionInput) previsionInput.addEventListener('change', majTaux);
        })();
    </script>
<%
    }catch(Exception e){
        e.printStackTrace();
    }

%>