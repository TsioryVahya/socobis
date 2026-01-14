package servlet;

import com.google.gson.Gson;
import fabrication.Of;
import fabrication.OfFille;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import user.UserEJB;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;
import bean.ClassMAPTable;

@WebServlet(name = "OfServlet", urlPatterns = {"/OfServlet"})
public class OfServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Map<String, Object> res = new HashMap<>();

        try {
            HttpSession session = request.getSession();
            UserEJB u = (UserEJB) session.getAttribute("u");
            if (u == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.put("status", "error");
                res.put("message", "Session expirée ou non connectée");
                out.print(gson.toJson(res));
                return;
            }

            String action = request.getParameter("action");
            System.out.println("OfServlet: Action = " + action);

            // Mode LISTE
            if ("list".equalsIgnoreCase(action)) {
                Of critere = new Of();
                // Utiliser la même vue que les JSP de liste (OFABLIB)
                critere.setNomTable("OFABLIB");

                // Même logique de critères que ordre-fabrication-liste.jsp
                String[] listeCrt = {"id", "lancepar", "cible", "remarque", "libelle", "besoin", "daty"};
                String[] listeInt = {"daty", "besoin"};

                Connection c = new UtilDB().GetConn();
                try {
                    Object[] list = u.getData(critere, listeCrt, listeInt, c, "");
                    res.put("status", "success");
                    res.put("count", list != null ? list.length : 0);
                    res.put("data", list);
                } finally {
                    c.close();
                }
                out.print(gson.toJson(res));
                return;
            }

            // Mode CREATION
            Of mere = new Of();
            OfFille[] filles = null;

            // Tentative de lecture JSON
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            String body = sb.toString();

            if (!body.isEmpty()) {
                try {
                    Map<String, Object> data = gson.fromJson(body, Map.class);
                    if (data.containsKey("mere")) {
                        Map<String, Object> mereMap = (Map<String, Object>) data.get("mere");

                        String datyStr = mereMap.get("daty").toString();
                        // Accepter YYYY-MM-DD (front) ou dd/MM/yyyy (outil existant)
                        if (datyStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            mere.setDaty(java.sql.Date.valueOf(datyStr));
                        } else {
                            mere.setDaty(Utilitaire.stringDate(datyStr));
                        }

                        mere.setLancePar(mereMap.get("lancePar").toString());
                        mere.setCible(mereMap.get("cible").toString());
                        mere.setLibelle(mereMap.get("libelle").toString());
                        mere.setRemarque(mereMap.get("remarque") != null ? mereMap.get("remarque").toString() : "");
                    }
                    if (data.containsKey("filles")) {
                        java.util.List<Map<String, Object>> fillesList = (java.util.List<Map<String, Object>>) data.get("filles");
                        filles = new OfFille[fillesList.size()];
                        for (int i = 0; i < fillesList.size(); i++) {
                            Map<String, Object> fMap = fillesList.get(i);
                            filles[i] = new OfFille();
                            filles[i].setIdIngredients(fMap.get("idIngredients").toString());
                            filles[i].setQte(Double.parseDouble(fMap.get("qte").toString()));
                            filles[i].setIdunite(fMap.get("idunite").toString());
                            filles[i].setLibelle(fMap.get("libelle") != null ? fMap.get("libelle").toString() : "");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("OfServlet JSON Error: " + e.getMessage());
                }
            }

            // Si JSON vide ou incomplet, essayer les paramètres d'URL
            if (mere.getDaty() == null) {
                String daty = request.getParameter("daty");
                if (daty != null) {
                    // Accepter aussi YYYY-MM-DD en mode paramètres URL
                    if (daty.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        mere.setDaty(java.sql.Date.valueOf(daty));
                    } else {
                        mere.setDaty(Utilitaire.stringDate(daty));
                    }
                    mere.setLancePar(request.getParameter("lancePar"));
                    mere.setCible(request.getParameter("cible"));
                    mere.setLibelle(request.getParameter("libelle"));
                    mere.setRemarque(request.getParameter("remarque"));

                    String idIngredients = request.getParameter("idIngredients");
                    if (idIngredients != null) {
                        filles = new OfFille[1];
                        filles[0] = new OfFille();
                        filles[0].setIdIngredients(idIngredients);
                        filles[0].setQte(Double.parseDouble(request.getParameter("qte")));
                        filles[0].setIdunite(request.getParameter("idunite"));
                        filles[0].setLibelle(request.getParameter("libelleFille") != null ? request.getParameter("libelleFille") : request.getParameter("libelle"));
                    }
                }
            }

            if (mere.getDaty() == null || filles == null || filles.length == 0) {
                res.put("status", "error");
                res.put("message", "Données manquantes pour la création de l'OF (daty, lancePar, cible, libelle, idIngredients, qte, idunite)");
                out.print(gson.toJson(res));
                return;
            }

            // Création Multiple
            Of result = (Of) u.createObjectMultiple(mere, "idMere", filles);
            
            // On prépare une réponse propre sans les métadonnées EJB/Sécurité
            Map<String, Object> simpleData = new HashMap<>();
            simpleData.put("id", result.getId());
            simpleData.put("libelle", result.getLibelle());
            simpleData.put("daty", result.getDaty());
            
            if (result.getOfFilles() != null) {
                java.util.List<Map<String, Object>> simpleFilles = new java.util.ArrayList<>();
                for (OfFille f : result.getOfFilles()) {
                    Map<String, Object> fMap = new HashMap<>();
                    fMap.put("id", f.getId());
                    fMap.put("idIngredients", f.getIdIngredients());
                    fMap.put("qte", f.getQte());
                    simpleFilles.add(fMap);
                }
                simpleData.put("filles", simpleFilles);
            }

            res.put("status", "success");
            res.put("message", "Ordre de Fabrication créé avec succès");
            res.put("data", simpleData);
            out.print(gson.toJson(res));

        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
            res.put("cause", e.getCause() != null ? e.getCause().getMessage() : "Inconnue");
            out.print(gson.toJson(res));
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
