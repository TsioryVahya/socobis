package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import bean.ClassMAPTable;
import bean.CGenUtil;
import fabrication.Fabrication;
import fabrication.FabricationCpl;
import fabrication.FabricationFille;
import fabrication.FabricationFilleCpl;
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
        boolean responseWritten = false;

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

            // Mode LISTE (utilise les vues FABRICATIONCPL* comme la JSP)
            if ("list".equalsIgnoreCase(action)) {
                System.out.println("FabricationServlet: Mode LISTE détecté");

                FabricationCpl critere = new FabricationCpl();

                // Nom de table dynamique comme dans fabrication-liste.jsp
                String etatTable = request.getParameter("etatTable");
                if (etatTable == null || etatTable.trim().isEmpty()) {
                    etatTable = "FABRICATIONCPL"; // Tous par défaut
                }
                critere.setNomTable(etatTable);

                String id = request.getParameter("id");
                String lancePar = request.getParameter("lancePar");
                String cible = request.getParameter("cible");
                String remarque = request.getParameter("remarque");
                String libelle = request.getParameter("libelle");
                String datyMin = request.getParameter("datyMin");
                String datyMax = request.getParameter("datyMax");
                String idOf = request.getParameter("idOf");
                String idOffille = request.getParameter("idOffille");

                String where = "";
                if (id != null && !id.isEmpty()) where += " AND id LIKE '%" + id + "%'";
                if (lancePar != null && !lancePar.isEmpty()) where += " AND lanceparLib LIKE '%" + lancePar + "%'";
                if (cible != null && !cible.isEmpty()) where += " AND cibleLib LIKE '%" + cible + "%'";
                if (remarque != null && !remarque.isEmpty()) where += " AND remarque LIKE '%" + remarque + "%'";
                if (libelle != null && !libelle.isEmpty()) where += " AND libelle LIKE '%" + libelle + "%'";
                if (idOf != null && !idOf.isEmpty()) where += " AND idOf LIKE '%" + idOf + "%'";
                if (idOffille != null && !idOffille.isEmpty()) where += " AND idOffille LIKE '%" + idOffille + "%'";

                if (datyMin != null && !datyMin.isEmpty()) {
                    where += " AND daty >= TO_DATE('" + datyMin + "', 'YYYY-MM-DD')";
                }
                if (datyMax != null && !datyMax.isEmpty()) {
                    where += " AND daty <= TO_DATE('" + datyMax + "', 'YYYY-MM-DD')";
                }

                // Même tri que la JSP : par date puis id
                where += " ORDER BY daty DESC, id DESC";

                utilitaire.UtilDB utilDB = new utilitaire.UtilDB();
                java.sql.Connection conn = null;
                try {
                    conn = utilDB.GetConn();

                    // Compter le total sur la même vue
                    String countQuery = "SELECT COUNT(*) FROM " + etatTable + " WHERE 1=1" + where.replace(" ORDER BY daty DESC, id DESC", "");
                    java.sql.Statement stmt = conn.createStatement();
                    java.sql.ResultSet rs = stmt.executeQuery(countQuery);
                    int total = 0;
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                    rs.close();
                    stmt.close();

                    // Récupérer les données
                    Object[] list = u.getData(critere, null, null, conn, where);

                    System.out.println("FabricationServlet: Recherche terminée, nb resultats = " +
                            (list != null ? list.length : 0));

                    // Pour éviter les problèmes de sérialisation Gson avec FabricationCpl
                    // (champs dupliqués comme etatLib), on construit une représentation simple
                    java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
                    if (list != null) {
                        for (Object obj : list) {
                            if (obj instanceof FabricationCpl) {
                                FabricationCpl f = (FabricationCpl) obj;
                                Map<String, Object> row = new HashMap<>();
                                row.put("id", f.getId());
                                row.put("lancePar", f.getLanceparLib());
                                row.put("cible", f.getCibleLib());
                                row.put("remarque", f.getRemarque());
                                row.put("libelle", f.getLibelle());
                                row.put("daty", f.getDaty());
                                row.put("idOf", f.getIdOf());
                                row.put("idOffille", f.getIdOffille());
                                row.put("etatLib", f.getEtatLib());
                                row.put("etat", f.getEtat());
                                data.add(row);
                            }
                        }
                    }

                    res.put("status", "success");
                    res.put("data", data);
                    res.put("total", total);
                } finally {
                    if (conn != null)
                        conn.close();
                }
                responseWritten = true;
                out.print(gson.toJson(res));
                return;
            }

            // Mode VALIDER: valider une fabrication (même comportement que le bouton "Valider" de la JSP via apresTarif.jsp)
            if ("valider".equalsIgnoreCase(action)) {
                String idFab = request.getParameter("id");
                if (idFab == null || idFab.trim().isEmpty()) {
                    throw new Exception("Paramètre id (fabrication) manquant pour l'action valider");
                }

                // Reproduire le chemin ERP: passer par UserEJB.validerObject
                Fabrication fab = new Fabrication();
                fab.setId(idFab);
                fab.setNomTable("fabrication");

                // Utiliser le framework EJB pour la validation (comme dans apresTarif.jsp)
                u.validerObject(fab);

                res.put("status", "success");
                res.put("message", "Fabrication validée");
                responseWritten = true;
                out.print(gson.toJson(res));
                return;
            }

            // Mode DETAILS: renvoyer les lignes de fabrication (FabricationFilleCpl) pour une fabrication donnée
            if ("details".equalsIgnoreCase(action)) {
                String idFab = request.getParameter("id");
                if (idFab == null || idFab.trim().isEmpty()) {
                    throw new Exception("Paramètre id (fabrication) manquant pour l'action details");
                }

                utilitaire.UtilDB utilDB = new utilitaire.UtilDB();
                java.sql.Connection conn = null;
                try {
                    conn = utilDB.GetConn();

                    FabricationFilleCpl map = new FabricationFilleCpl();
                    // Utiliser exactement le même nom de table que dans fabrication-details.jsp
                    map.setNomTable("FabricationFilleCpl");
                    map.setIdMere(idFab);

                    // Même logique que PageRecherche: filtre explicite sur idMere
                    Object[] list = CGenUtil.rechercher(map, null, null, conn,
                            " and idMere='" + idFab + "'");

                    // Construire une représentation JSON simplifiée pour éviter les champs complexes (RecetteLib, etc.)
                    java.util.List<Map<String, Object>> data = new ArrayList<>();
                    if (list != null) {
                        for (Object obj : list) {
                            if (obj instanceof FabricationFilleCpl) {
                                FabricationFilleCpl ff = (FabricationFilleCpl) obj;
                                Map<String, Object> row = new HashMap<>();
                                row.put("id", ff.getId());
                                row.put("idIngredients", ff.getIdIngredients());
                                row.put("idingredientsLib", ff.getIdingredientsLib());
                                row.put("libelle", ff.getLibelle());
                                row.put("datybesoin", ff.getDatyBesoin());
                                row.put("pu", ff.getPu());
                                row.put("qte", ff.getQte());
                                row.put("montant", ff.getMontant());
                                row.put("idunitelib", ff.getIdunitelib());
                                row.put("idMachineLib", ff.getIdMachineLib());
                                data.add(row);
                            }
                        }
                    }

                    res.put("status", "success");
                    res.put("data", data);
                } finally {
                    if (conn != null)
                        conn.close();
                }
                responseWritten = true;
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
                        if (mereMap.get("equipe") != null) mere.setEquipe(mereMap.get("equipe").toString());

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
                                if (fMap.get("remarque") != null) filles[i].setRemarque(fMap.get("remarque").toString());
                                if (fMap.get("idBcFille") != null) filles[i].setIdBcFille(fMap.get("idBcFille").toString());
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
                if (!responseWritten) {
                    out.print(gson.toJson(res));
                }
            } catch (Exception fatal) {
                // En cas d'erreur ultime de sérialisation
                out.print("{\"status\":\"error\",\"message\":\"Erreur fatale de sérialisation JSON\"}");
            }
        }
    }
}
