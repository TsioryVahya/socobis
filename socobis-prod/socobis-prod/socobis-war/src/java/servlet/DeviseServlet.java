package servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import bean.CGenUtil;
import change.TauxDeChange;
import javax.servlet.annotation.*;
import java.sql.Connection;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;
import prevision.PrevisionComplet;

@WebServlet("/DeviseServlet")
public class DeviseServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            String idDevise = request.getParameter("idDevise");
            String daty = request.getParameter("daty"); // format attendu: YYYY-MM-DD
            String idOrigine = request.getParameter("idOrigine"); // VNT... ou FCF...
            String idPrevision = request.getParameter("idPrevision"); // identifiant de la prévision sélectionnée
            Gson gson = new Gson();
            Map<String, Object> jsonObject = new HashMap<>();
            if (idDevise == null || idDevise.compareToIgnoreCase("") == 0) {
                throw new Exception("Devise nulle");
            }
            jsonObject.put("idDevise", idDevise);

            double tauxValue = 1;
            String source = "fallback";
            String datyRef = daty;
            String deviseRef = idDevise;
            // Priorité 1: si une prévision est fournie, utiliser la date de la prévision pour calculer le dernier taux et retourner immédiatement
            if (idPrevision != null && idPrevision.trim().length() > 0) {
                Connection c = null;
                try{
                    c = new UtilDB().GetConn();
                    PrevisionComplet[] res = (PrevisionComplet[]) CGenUtil.rechercher(new PrevisionComplet(), null, null, c, " and id='"+idPrevision+"'");
                    if(res != null && res.length > 0){
                        String datyPrev = Utilitaire.formatterDaty(res[0].getDaty());
                        String devForRate = (idDevise != null && idDevise.trim().length() > 0) ? idDevise : res[0].getIdDevise();
                        if (devForRate != null && !devForRate.equalsIgnoreCase("AR")){
                            tauxValue = TauxDeChange.getLastTaux(c, datyPrev, devForRate);
                            source = "prevision";
                            datyRef = datyPrev;
                            deviseRef = devForRate;
                        } else {
                            tauxValue = 1;
                            source = "prevision";
                            datyRef = datyPrev;
                            deviseRef = devForRate;
                        }
                    } else {
                        source = "prevision_not_found";
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                } finally {
                    if(c!=null) try { c.close(); } catch(Exception ignore) {}
                }
                jsonObject.put("taux", tauxValue);
                jsonObject.put("source", source);
                jsonObject.put("datyRef", datyRef);
                jsonObject.put("deviseRef", deviseRef);
                String jsonResponse = gson.toJson(jsonObject);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
                return;
            }
            // Priorité 2: si une origine (vente/facture) est fournie, utiliser le taux de la Prévision liée
            if (idOrigine != null && idOrigine.trim().length() > 0 &&
                (idOrigine.startsWith("VNT") || idOrigine.startsWith("FCF"))) {
                try {
                    tauxValue = TauxDeChange.getTauxPrevision(null, idOrigine);
                    source = "origine_prevision";
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // fallback ci-dessous
                }
            }
            if (tauxValue == 1) {
                // Si Ariary, taux = 1
                if (idDevise.compareToIgnoreCase("AR") == 0) {
                    tauxValue = 1;
                } else {
                    // Récupère le dernier taux à la date fournie (ou aujourdhui si null)
                    tauxValue = TauxDeChange.getLastTaux(null, daty, idDevise);
                    source = "last_taux";
                    datyRef = daty;
                    deviseRef = idDevise;
                }
            }
            jsonObject.put("taux", tauxValue);
            jsonObject.put("source", source);
            jsonObject.put("datyRef", datyRef);
            jsonObject.put("deviseRef", deviseRef);
            String jsonResponse = gson.toJson(jsonObject);
            System.out.println(jsonResponse);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(DeviseServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(req, resp);
    }

}
