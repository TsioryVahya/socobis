package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import bean.ClassMAPTable;
import fabrication.Fabrication;
import fabrication.FabricationFille;
import user.UserEJB;
import utilitaire.Utilitaire;

@WebServlet("/FabricationServlet")
public class FabricationServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> res = new HashMap<>();

        try {
            String action = request.getParameter("action");
            System.out.println("FabricationServlet: Tentative avec action = " + action);

            HttpSession session = request.getSession();
            UserEJB u = (UserEJB) session.getAttribute("u");
            if (u == null) {
                System.out.println("FabricationServlet: Session non connectée");
                throw new Exception("Session expirée ou utilisateur non connecté");
            }

            Fabrication mere = null;
            FabricationFille[] filles = null;

            // Mode LISTE
            // Mode LISTE avec pagination
            if ("list".equalsIgnoreCase(action)) {
                System.out.println("FabricationServlet: Mode LISTE détecté");
                Fabrication critere = new Fabrication();
                String idFabrication = request.getParameter("id");

                String where = "";
                if (idFabrication != null && !idFabrication.isEmpty()) {
                    where = " AND id = '" + idFabrication + "'";
                }
                // Ajouter un ORDER BY compatible Oracle (sans LIMIT/OFFSET)
                where += " ORDER BY daty DESC, id DESC";

                utilitaire.UtilDB utilDB = new utilitaire.UtilDB();
                java.sql.Connection conn = null;
                try {
                    conn = utilDB.GetConn();

                    // Compter le total pour la pagination
                    String countQuery = "SELECT COUNT(*) FROM FABRICATION WHERE 1=1" +
                            (idFabrication != null && !idFabrication.isEmpty()
                                    ? " AND id = '" + idFabrication + "'"
                                    : "");
                    java.sql.Statement stmt = conn.createStatement();
                    java.sql.ResultSet rs = stmt.executeQuery(countQuery);
                    int total = 0;
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                    rs.close();
                    stmt.close();

                    // Récupérer les données (sans pagination Oracle pour l'instant)
                    Object[] list = u.getData(critere, null, null, conn, where);

                    System.out.println("FabricationServlet: Recherche terminée, nb resultats = " +
                            (list != null ? list.length : 0));

                    res.put("status", "success");
                    res.put("data", list);
                } finally {
                    if (conn != null)
                        conn.close();
                }
                out.print(gson.toJson(res));
                return;
            }

            // Tentative de lecture du JSON uniquement si le Content-Type est
            // application/json
            String contentType = request.getContentType();
            System.out.println("FabricationServlet: Content-Type = " + contentType);
            if (contentType != null && contentType.contains("application/json")) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = request.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }

                if (sb.length() > 0) {
                    System.out.println("FabricationServlet: JSON reçu = " + sb.toString());
                    // Mode JSON
                    Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
                    if (data != null && data.containsKey("mere") && data.containsKey("filles")) {
                        Map<String, Object> mereMap = (Map<String, Object>) data.get("mere");
                        mere = new Fabrication();
                        
                        // Gestion de la date
                        if (mereMap.get("daty") != null) {
                            String datyStr = mereMap.get("daty").toString();
                            if (datyStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                mere.setDaty(java.sql.Date.valueOf(datyStr));
                            } else {
                                mere.setDaty(Utilitaire.stringDate(datyStr));
                            }
                        } else {
                            mere.setDaty(Utilitaire.dateDuJourSql());
                        }
                        
                        // Autres champs de la mère
                        if (mereMap.get("lancePar") != null) mere.setLancePar(mereMap.get("lancePar").toString());
                        if (mereMap.get("cible") != null) mere.setCible(mereMap.get("cible").toString());
                        if (mereMap.get("libelle") != null) mere.setLibelle(mereMap.get("libelle").toString());
                        if (mereMap.get("remarque") != null) mere.setRemarque(mereMap.get("remarque").toString());
                        if (mereMap.get("idOf") != null) mere.setIdOf(mereMap.get("idOf").toString());
                        if (mereMap.get("idOffille") != null) mere.setIdOffille(mereMap.get("idOffille").toString());
                        if (mereMap.get("fabricationPrec") != null) mere.setFabricationPrec(mereMap.get("fabricationPrec").toString());
                        if (mereMap.get("fabricationSuiv") != null) mere.setFabricationSuiv(mereMap.get("fabricationSuiv").toString());
                        if (mereMap.get("idBc") != null) mere.setIdBc(mereMap.get("idBc").toString());
                        if (mereMap.get("idBonDeCommande") != null) mere.setIdBc(mereMap.get("idBonDeCommande").toString());
                        if (mereMap.get("ordreDeFab") != null) mere.setOrdreDeFab(mereMap.get("ordreDeFab").toString());

                        // Gestion des filles
                        java.util.List<Map<String, Object>> fillesList = (java.util.List<Map<String, Object>>) data.get("filles");
                        if (fillesList != null) {
                            filles = new FabricationFille[fillesList.size()];
                            for (int i = 0; i < fillesList.size(); i++) {
                                Map<String, Object> fMap = fillesList.get(i);
                                filles[i] = new FabricationFille();
                                if (fMap.get("idIngredients") != null) filles[i].setIdIngredients(fMap.get("idIngredients").toString());
                                if (fMap.get("qte") != null) filles[i].setQte(Utilitaire.stringToDouble(fMap.get("qte").toString()));
                                if (fMap.get("idMachine") != null) filles[i].setIdMachine(fMap.get("idMachine").toString());
                                if (fMap.get("idunite") != null) filles[i].setIdunite(fMap.get("idunite").toString());
                                if (fMap.get("libelle") != null) filles[i].setLibelle(fMap.get("libelle").toString());
                            }
                        }
                    }
                }
            }

            // Si le JSON était vide ou invalide, on tente via les paramètres d'URL (Mode
            // simple)
            if (mere == null) {
                System.out.println("FabricationServlet: Mode paramètres d'URL");
                mere = new Fabrication();
                String datyStr = request.getParameter("daty");
                if (datyStr != null && !datyStr.isEmpty()) {
                    if (datyStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        mere.setDaty(java.sql.Date.valueOf(datyStr));
                    } else {
                        mere.setDaty(Utilitaire.stringDate(datyStr));
                    }
                } else {
                    mere.setDaty(Utilitaire.dateDuJourSql());
                }
                mere.setLancePar(request.getParameter("lancePar"));
                mere.setCible(request.getParameter("cible"));
                mere.setLibelle(request.getParameter("libelle"));
                mere.setRemarque(request.getParameter("remarque"));
                mere.setIdOf(request.getParameter("idOf"));
                mere.setIdOffille(request.getParameter("idOffille"));
                mere.setFabricationPrec(request.getParameter("fabricationPrec"));
                mere.setFabricationSuiv(request.getParameter("fabricationSuiv"));
                mere.setIdBc(request.getParameter("idBonDeCommande"));
                mere.setOrdreDeFab(request.getParameter("ordreDeFab"));

                String besoinStr = request.getParameter("besoin");
                if (besoinStr != null && !besoinStr.isEmpty()) {
                    if (besoinStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        mere.setBesoin(java.sql.Date.valueOf(besoinStr));
                    } else {
                        mere.setBesoin(Utilitaire.stringDate(besoinStr));
                    }
                }

                // Pour les filles en mode URL, on n'en crée qu'une seule pour simplifier le
                // test
                if (request.getParameter("idIngredients") != null) {
                    FabricationFille f = new FabricationFille();
                    f.setIdIngredients(request.getParameter("idIngredients"));
                    f.setQte(Utilitaire.stringToDouble(request.getParameter("qte")));
                    f.setIdMachine(request.getParameter("idMachine"));
                    f.setIdunite(request.getParameter("idunite"));
                    f.setLibelle(request.getParameter("libelleFille"));
                    filles = new FabricationFille[] { f };
                }
            }

            if (mere == null || filles == null) {
                throw new Exception(
                        "Données manquantes. Envoyez un JSON ou des paramètres d'URL (daty, libelle, idIngredients, qte...)");
            }

            System.out.println("FabricationServlet: Appel EJB createObjectMultiple...");
            // Insertion
            Object resultObj = u.createObjectMultiple(mere, "idMere", (bean.ClassMAPTable[]) filles);
            System.out.println("FabricationServlet: Insertion réussie");

            res.put("status", "success");
            res.put("message", "Fabrication enregistrée avec succès");

            if (resultObj != null) {
                if (resultObj instanceof bean.ClassMAPTable) {
                    String generatedId = ((bean.ClassMAPTable) resultObj).getTuppleID();
                    System.out.println("FabricationServlet: ID généré = " + generatedId);
                    res.put("idGenerated", generatedId);
                    res.put("data", generatedId); // On ne met que l'ID pour éviter les erreurs de sérialisation
                } else {
                    res.put("data", resultObj.toString());
                }
            }

        } catch (Exception e) {
            System.err.println("FabricationServlet: Erreur détectée !");
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage() != null ? e.getMessage() : e.toString());
            // Pour plus de détails en cas d'erreur framework
            if (e.getCause() != null) {
                res.put("cause", e.getCause().getMessage());
            }
        } finally {
            try {
                out.print(gson.toJson(res));
            } catch (Exception fatal) {
                // En cas d'erreur ultime de sérialisation
                out.print("{\"status\":\"error\",\"message\":\"Erreur fatale de sérialisation JSON\"}");
            }
        }
    }
}
