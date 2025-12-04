<%-- Attacher une liste de mouvements de caisse à une Prévision donnée --%>
<%@ page import="user.UserEJB" %>
<%@ page import="prevision.Prevision" %>
<%@ page import="caisse.MvtCaisse" %>
<%@ page import="bean.CGenUtil" %>
<%
    try{
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String lien = (String) session.getValue("lien");
        UserEJB u = (UserEJB) session.getValue("u");
        String idPrevision = request.getParameter("idPrevision");
        String[] idsMvt = request.getParameterValues("id"); // cocher dans la liste
        if(idPrevision==null || idPrevision.trim().length()==0){
            throw new Exception("Prévision manquante");
        }
        if(idsMvt==null || idsMvt.length==0){
            throw new Exception("Aucun mouvement sélectionné");
        }
        Prevision p = new Prevision();
        p.setId(idPrevision);
        // Utilise la méthode qui rattache une liste de mouvements à la prévision
        p.attacherFacture(idsMvt, u.getUser().getTuppleID(), null);
%>
        <script>
            // retour vers la fiche de la prévision
            document.location.replace("<%= lien %>?but=prevision/prevision-fiche.jsp&id=<%= idPrevision %>");
        </script>
<%
    }catch(Exception e){
        e.printStackTrace();
%>
        <div style="padding:16px;color:#b71c1c;">Erreur attachement: <%= e.getMessage() %></div>
<%
    }
%>
