package servlet;

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

import com.google.gson.Gson;
import compteur.Compteur;
import user.UserEJB;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;

@WebServlet("/CompteurServlet")
public class CompteurServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> res = new HashMap<>();
        Connection c = null;

        try {
            HttpSession session = request.getSession();
            UserEJB u = (UserEJB) session.getAttribute("u");

            if (u == null) {
                throw new Exception("Session expirée ou utilisateur non connecté");
            }

            // Extraction des paramètres
            String datyStr = request.getParameter("daty");
            String idMachine = request.getParameter("idMachine");
            String nombreStr = request.getParameter("nombre");
            String heure = request.getParameter("heure");
            String idFabrication = request.getParameter("idFabrication");
            String idOrigine = request.getParameter("idOrigine");

            // Création de l'objet Compteur
            Compteur compteur = new Compteur();
            
            if (datyStr != null && !datyStr.isEmpty()) {
                compteur.setDaty(Utilitaire.stringDate(datyStr));
            } else {
                compteur.setDaty(Utilitaire.dateDuJourSql());
            }

            compteur.setIdMachine(idMachine);
            compteur.setHeure(heure);
            compteur.setIdFabrication(idFabrication);
            compteur.setIdOrigine(idOrigine);
            
            if (nombreStr != null && !nombreStr.isEmpty()) {
                compteur.setNombre(Double.parseDouble(nombreStr));
            }

            // Persistance
            c = new UtilDB().GetConn();
            c.setAutoCommit(false);
            
            // On utilise la fonction existante de création d'objet du framework
            compteur.createObject(String.valueOf(u.getUser().getRefuser()), c);
            
            c.commit();

            res.put("status", "success");
            res.put("message", "Compteur enregistré avec succès");
            res.put("data", compteur);

        } catch (Exception e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            res.put("status", "error");
            res.put("message", e.getMessage());
            e.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            out.print(gson.toJson(res));
            out.flush();
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
