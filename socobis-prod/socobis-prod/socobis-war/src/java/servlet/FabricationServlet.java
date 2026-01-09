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
            System.out.println("FabricationServlet: Tentative de création de fabrication...");
            HttpSession session = request.getSession();
            UserEJB u = (UserEJB) session.getAttribute("u");
            if (u == null) {
                System.out.println("FabricationServlet: Session non connectée");
                throw new Exception("Session expirée ou utilisateur non connecté");
            }

            Fabrication mere = null;
            FabricationFille[] filles = null;

            // Tentative de lecture du JSON uniquement si le Content-Type est application/json
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
                        mere = gson.fromJson(gson.toJson(data.get("mere")), Fabrication.class);
                        filles = gson.fromJson(gson.toJson(data.get("filles")), FabricationFille[].class);
                    }
                }
            }

            // Si le JSON était vide ou invalide, on tente via les paramètres d'URL (Mode simple)
            if (mere == null) {
                System.out.println("FabricationServlet: Mode paramètres d'URL");
                mere = new Fabrication();
                String datyStr = request.getParameter("daty");
                if (datyStr != null && !datyStr.isEmpty()) {
                    mere.setDaty(Utilitaire.stringDate(datyStr));
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
                
                String besoinStr = request.getParameter("besoin");
                if (besoinStr != null && !besoinStr.isEmpty()) {
                    mere.setBesoin(Utilitaire.stringDate(besoinStr));
                }

                // Pour les filles en mode URL, on n'en crée qu'une seule pour simplifier le test
                if (request.getParameter("idIngredients") != null) {
                    FabricationFille f = new FabricationFille();
                    f.setIdIngredients(request.getParameter("idIngredients"));
                    f.setQte(Utilitaire.stringToDouble(request.getParameter("qte")));
                    f.setIdMachine(request.getParameter("idMachine"));
                    f.setIdunite(request.getParameter("idunite"));
                    f.setLibelle(request.getParameter("libelleFille"));
                    filles = new FabricationFille[]{f};
                }
            }

            if (mere == null || filles == null) {
                throw new Exception("Données manquantes. Envoyez un JSON ou des paramètres d'URL (daty, libelle, idIngredients, qte...)");
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
