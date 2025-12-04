<%@page import="utils.ConstanteStation"%>
<%@page import="affichage.*"%>
<%@page import="caisse.MvtCaisse"%>
<%@page import="caisse.Caisse"%>
<%@page import="user.*"%>
<%@ page import="ristourne.Ristourne" %>
<%@ page import="vente.Vente" %>
<%@ page import="utilitaire.Utilitaire" %>
<%@ page import="change.TauxDeChange" %>
<%@ page import="vente.VenteLib" %>
<%@ page import="bean.TypeObjet" %>
<%@ page import="caisse.Devise" %>
<%@ page import="bean.CGenUtil" %>

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
        String tau=request.getParameter("taux");
        String id = "";
        VenteLib v = null;
        System.err.println(request.getParameter("id"));
        if(request.getParameter("id")!=null && request.getParameter("id")!="" && request.getParameter("id").startsWith("RIS")){
            id = request.getParameter("id");
            Ristourne r = new Ristourne();
            r.setId(id);
            System.err.println(r);
            v = r.getVente(null);
            System.err.println(v);
        }

        affichage.Champ[] liste = new affichage.Champ[2];
        // Rendre la devise sélectionnable
        liste[0] = new Liste("idDevise",new caisse.Devise(),"val","id");
        Caisse c = new Caisse();
       // liste[1] = new Liste("idCaisse",c,"val","id");
        liste[1] = new Liste("idModePaiement",new TypeObjet("MODEPAIEMENT"),"val","id");
			
        pageInsert.getFormu().changerEnChamp(liste);
        pageInsert.getFormu().getChamp("designation").setDefaut("Paiement de la facture : "+idOrigine);
        pageInsert.getFormu().getChamp("idCaisse").setVisible(false);
        pageInsert.getFormu().getChamp("idDevise").setLibelle("Devise");
        pageInsert.getFormu().getChamp("daty").setLibelle("Date");
        // Par défaut, utiliser les paramètres d'URL
        String deviseDef = devise;
        String tauxDef = tau;
        String datyVente = null;
        // Si l'encaissement provient d'une vente, utiliser le taux basé sur la date de vente (dernier taux <= date vente)
        if(idOrigine!=null && idOrigine.startsWith("VNT")){
            VenteLib vntParam = new VenteLib();
            vntParam.setId(idOrigine);
            VenteLib[] ventesUi = (VenteLib[]) bean.CGenUtil.rechercher(vntParam, null, null, null, "");
            if(ventesUi!=null && ventesUi.length>0){
                if(ventesUi[0].getIdDevise()!=null && ventesUi[0].getIdDevise().trim().length()>0){
                    deviseDef = ventesUi[0].getIdDevise();
                }
                datyVente = utilitaire.Utilitaire.formatterDaty(ventesUi[0].getDaty());
                try{
                    double tx = TauxDeChange.getTauxForVente(null, idOrigine);
                    if(tx>0){ tauxDef = String.valueOf(tx); }
                }catch(Exception ignore){}
            }
        } else {
            // Sinon, appliquer le dernier taux connu pour la devise sélectionnée
            try{
                if(deviseDef!=null && deviseDef.trim().length()>0){
                    double last = TauxDeChange.getLastTaux(null, Utilitaire.dateDuJour(), deviseDef);
                    if(last > 0){ tauxDef = String.valueOf(last); }
                }
            }catch(Exception ignore){ }
        }
        pageInsert.getFormu().getChamp("idDevise").setDefaut(deviseDef);
        // Ne pas soumettre automatiquement; le taux sera mis à jour en AJAX
        pageInsert.getFormu().getChamp("taux").setDefaut(tauxDef);

        pageInsert.getFormu().getChamp("idVirement").setVisible(false);
        pageInsert.getFormu().getChamp("idVenteDetail").setVisible(false);
        pageInsert.getFormu().getChamp("idOp").setVisible(false);
        pageInsert.getFormu().getChamp("idOrigine").setVisible(false);
        pageInsert.getFormu().getChamp("credit").setDefaut(montant);
        pageInsert.getFormu().getChamp("credit").setLibelle("Cr&eacute;dit");
        pageInsert.getFormu().getChamp("designation").setLibelle("d&eacute;signation");
        //pageInsert.getFormu().getChamp("idtraite").setLibelle("ID Traite");

        pageInsert.getFormu().getChamp("idtraite").setVisible(false);
        pageInsert.getFormu().getChamp("etatversement").setVisible(false);

        pageInsert.getFormu().getChamp("etat").setVisible(false);
        pageInsert.getFormu().getChamp("idOrigine").setDefaut(idOrigine);
        pageInsert.getFormu().getChamp("idOrigine").setVisible(false);
        pageInsert.getFormu().getChamp("debit").setVisible(false);
        pageInsert.getFormu().getChamp("idTiers").setDefaut(tiers);
        pageInsert.getFormu().getChamp("idTiers").setVisible(false);
        pageInsert.getFormu().getChamp("idPrevision").setLibelle("Pr&eacute;vision");
        pageInsert.getFormu().getChamp("idModePaiement").setLibelle("Mode de paiement");
        pageInsert.getFormu().getChamp("idPrevision").setPageAppelComplete("prevision.Prevision", "id", "PREVISION");
        pageInsert.getFormu().getChamp("compte").setLibelle("Compte de regroupement");
        if(v!=null){
            pageInsert.getFormu().getChamp("designation").setDefaut("Paiement du ristourne "+id);
            pageInsert.getFormu().getChamp("idTiers").setDefaut(v.getTiers());
            pageInsert.getFormu().getChamp("credit").setDefaut(v.getMontantttc()+"");
            pageInsert.getFormu().getChamp("idOrigine").setDefaut(v.getId());
            pageInsert.getFormu().getChamp("idDevise").setDefaut("AR");
            pageInsert.getFormu().getChamp("taux").setDefaut(v.getTauxdechange()+"");
        }
        String classe = "caisse.MvtCaisse";
        String nomTable = "MOUVEMENTCAISSE";
        String butApresPost = "caisse/mvt/mvtCaisse-fiche.jsp";
        String[] order_form = {"daty","designation","idModePaiement","credit","idDevise","taux","compte","idPrevision","idVirement","idVenteDetail","idOp","idOrigine","debit","idTiers","etat"};
        pageInsert.getFormu().setOrdre(order_form);
        pageInsert.preparerDataFormu();
        pageInsert.getFormu().makeHtmlInsertTabIndex();

%>

    <div class="content-wrapper">
        <h1 align="center">Paiement</h1>
        <form action="<%=pageInsert.getLien()%>?but=apresTarif.jsp" method="post"  data-parsley-validate>
            <%
                out.println(pageInsert.getFormu().getHtmlInsert());
            %>
            <input name="acte" type="hidden" id="nature" value="insert">
            <input name="bute" type="hidden" id="bute" value="<%= butApresPost %>">
            <input name="classe" type="hidden" id="classe" value="<%= classe %>">
            <input name="nomtable" type="hidden" id="nomtable" value="<%= nomTable %>">
        </form>
        <% if(datyVente!=null){ %>
            <input type="hidden" id="datyVenteHidden" value="<%= datyVente %>" />
        <% } %>
    </div>

    <script>
        (function(){
            var deviseSelect    = document.querySelector('[name="idDevise"]');
            var tauxInput       = document.querySelector('[name="taux"]');
            var dateInput       = document.querySelector('[name="daty"]');
            var datyVenteHidden = document.getElementById('datyVenteHidden');
            var previsionInput  = document.querySelector('[name="idPrevision"]');
            var origineInput    = document.querySelector('[name="idOrigine"]');

            if(!deviseSelect || !tauxInput) return;

            function majTauxDepuisDevise(){
                var idDev      = deviseSelect.value;
                var daty       = datyVenteHidden ? datyVenteHidden.value : (dateInput ? dateInput.value : '');
                var idPrevision= previsionInput ? previsionInput.value : '';
                var idOrigine  = origineInput ? origineInput.value : '';

                var url = '<%= request.getContextPath() %>/DeviseServlet'
                        + '?idDevise=' + encodeURIComponent(idDev)
                        + '&daty='     + encodeURIComponent(daty)
                        + (idPrevision ? '&idPrevision=' + encodeURIComponent(idPrevision) : '')
                        + (idOrigine   ? '&idOrigine='   + encodeURIComponent(idOrigine)   : '');

                console.log('URL DeviseServlet =', url);
                fetch(url, { credentials: 'same-origin' })
                    .then(function(r){ return r.json(); })
                    .then(function(data){
                        console.log('DeviseServlet retour =', data);
                        if(data && typeof data.taux !== 'undefined'){
                            tauxInput.value = data.taux;
                        }
                    })
                    .catch(function(err){ console.error('Erreur taux AJAX', err); });
            }

            window.addEventListener('load', majTauxDepuisDevise);
            deviseSelect.addEventListener('change', majTauxDepuisDevise);
            if (dateInput) dateInput.addEventListener('change', majTauxDepuisDevise);
            if (previsionInput) previsionInput.addEventListener('change', majTauxDepuisDevise);
        })();
    </script>

<%

    }catch(Exception e){
    
        e.printStackTrace();
    }

%>