<%@page import="prevision.Prevision" %>
<%@page import="caisse.Caisse" %>
<%@page import="affichage.*" %>
<%@page import="user.*" %>
<%@page import="utils.*" %>

<%
    try{

        UserEJB user = (UserEJB) session.getValue("u");
        String lien = (String) session.getValue("lien");
        Prevision prevision = new Prevision();
        PageInsert pageInsert = new PageInsert(prevision, request, user);
        pageInsert.setLien(lien);
                affichage.Champ[] liste = new affichage.Champ[1];
            liste[0] = new Liste("idDevise",new caisse.Devise(),"val","id");
            Caisse c = new Caisse();
            c.setIdPoint(ConstanteStation.getFichierCentre());
                
            pageInsert.getFormu().changerEnChamp(liste);
            pageInsert.getFormu().getChamp("designation").setDefaut("Prevision du "+utilitaire.Utilitaire.dateDuJour());
            pageInsert.getFormu().getChamp("designation").setLibelle("D&eacute;signation");
            pageInsert.getFormu().getChamp("idDevise").setLibelle("Devise");
            pageInsert.getFormu().getChamp("debit").setLibelle("d&eacute;pense");
            pageInsert.getFormu().getChamp("credit").setLibelle("recette");
            pageInsert.getFormu().getChamp("idDevise").setDefaut("AR");
            pageInsert.getFormu().getChamp("taux").setDefaut("1");
            pageInsert.getFormu().getChamp("compte").setLibelle("Compte de regroupement");
            pageInsert.getFormu().getChamp("debit").setVisible(true);
            pageInsert.getFormu().getChamp("idCaisse").setVisible(false);
            pageInsert.getFormu().getChamp("idVirement").setVisible(false);
            pageInsert.getFormu().getChamp("idVenteDetail").setVisible(false);
            pageInsert.getFormu().getChamp("idOp").setVisible(false);
            pageInsert.getFormu().getChamp("etat").setVisible(false);
            pageInsert.getFormu().getChamp("idOrigine").setVisible(false);
            pageInsert.getFormu().getChamp("daty").setLibelle("Date");
            pageInsert.getFormu().getChamp("idTiers").setPageAppelComplete("pertegain.Tiers","id","tiers");
            pageInsert.getFormu().getChamp("idTiers").setLibelle("Tiers");
            pageInsert.getFormu().getChamp("idFacture").setVisible(false);

            String classe = "prevision.Prevision";
            String nomTable = "PREVISION";
            String butApresPost = "prevision/prevision-fiche.jsp";
            String[] champOrdre={"daty","designation","debit","credit","idDevise","taux","idTiers","compte","idCaisse","idVirement","idVenteDetail","idOp","idOrigine","idFacture","etat"};
            pageInsert.getFormu().setOrdre(champOrdre);
            pageInsert.preparerDataFormu();
            pageInsert.getFormu().makeHtmlInsertTabIndex();


%>



    <div class="content-wrapper">
        <h1 align="center">Saisie d'une pr&eacute;vision </h1>
        <form action="<%=pageInsert.getLien()%>?but=apresTarif.jsp" method="post"  data-parsley-validate>
            <%
                out.println(pageInsert.getFormu().getHtmlInsert());
            %>
            <input name="acte" type="hidden" id="nature" value="insert">
            <input name="bute" type="hidden" id="bute" value="<%= butApresPost %>">
            <input name="classe" type="hidden" id="classe" value="<%= classe %>">
            <input name="nomtable" type="hidden" id="nomtable" value="<%= nomTable %>">
        </form>
    </div>

    <script>
      (function() {
        function qs(name){ return document.querySelector('[name="'+name+'"]') || document.getElementById(name); }
        const deviseEl = qs('idDevise');
        const dateEl   = qs('daty');
        const tauxEl   = qs('taux');

        async function refreshTaux(){
          if(!deviseEl || !tauxEl) return;
          const idDevise = deviseEl.value;
          // Si AR => 1
          if(!idDevise || idDevise.toUpperCase()==='AR'){
            tauxEl.value = '1';
            return;
          }
          const daty = dateEl && dateEl.value ? dateEl.value : '';
          try{
            const url = '<%=request.getContextPath()%>/DeviseServlet?idDevise=' + encodeURIComponent(idDevise) + (daty ? ('&daty=' + encodeURIComponent(daty)) : '');
            const res = await fetch(url, { headers: { 'Accept':'application/json' } });
            const data = await res.json();
            if(data && typeof data.taux !== 'undefined'){
              tauxEl.value = data.taux;
            }
          }catch(e){
            console.error('Erreur chargement taux', e);
          }
        }

        if(deviseEl){ deviseEl.addEventListener('change', refreshTaux); }
        if(dateEl){ dateEl.addEventListener('change', refreshTaux); }

        // Premier chargement (au cas où devise par défaut != AR)
        refreshTaux();
      })();
    </script>

<%
    }catch(Exception e){
        e.printStackTrace();
    }

%>