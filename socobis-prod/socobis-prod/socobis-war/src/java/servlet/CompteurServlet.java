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
import bean.CGenUtil;
import compteur.Compteur;
import compteur.CompteurLib;
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

            // Gestion du JSON ou des paramètres classiques
            String action = request.getParameter("action");
            Map<String, Object> jsonData = null;

            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                jsonData = gson.fromJson(request.getReader(), Map.class);
            } else if (request.getMethod().equalsIgnoreCase("POST")) {
                // Tentative de lecture JSON même si le Content-Type est absent ou incorrect
                try {
                    jsonData = gson.fromJson(request.getReader(), Map.class);
                } catch (Exception e) {
                    // Pas du JSON, on ignore
                }
            }

            if (jsonData != null && action == null) {
                action = (String) jsonData.get("action");
            }

            if (action == null) action = "create"; // Par défaut create pour faciliter les tests POST

            c = new UtilDB().GetConn();

            if (action.equalsIgnoreCase("list")) {
                CompteurLib critere = new CompteurLib();
                String idMachine = request.getParameter("idMachine");
                if (jsonData != null && jsonData.get("idMachine") != null) idMachine = (String) jsonData.get("idMachine");
                
                String where = "";
                if (idMachine != null && !idMachine.isEmpty()) {
                    where = " AND IDMACHINE = '" + idMachine + "'";
                }
                
                Object[] list = u.getData(critere, null, null, c, where);
                res.put("status", "success");
                res.put("data", list);
            } 
            else if (action.equalsIgnoreCase("create")) {
                Compteur compteur = new Compteur();
                
                if (jsonData != null) {
                    System.out.println("DEBUG: JSON data received: " + jsonData);
                    // Extraction manuelle pour éviter les problèmes de type avec Gson
                    compteur.setIdMachine((String) jsonData.get("idMachine"));
                    compteur.setHeure((String) jsonData.get("heure"));
                    compteur.setIdFabrication((String) jsonData.get("idFabrication"));
                    compteur.setIdOrigine((String) jsonData.get("idOrigine"));
                    
                    Object nombreObj = jsonData.get("nombre");
                    if (nombreObj != null) {
                        if (nombreObj instanceof Number) {
                            compteur.setNombre(((Number) nombreObj).doubleValue());
                        } else {
                            compteur.setNombre(Double.parseDouble(nombreObj.toString()));
                        }
                    }
                    
                    if (jsonData.get("daty") != null) {
                        String datyStr = jsonData.get("daty").toString();
                        if (datyStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            compteur.setDaty(java.sql.Date.valueOf(datyStr));
                        } else {
                            compteur.setDaty(Utilitaire.stringDate(datyStr));
                        }
                    } else {
                        compteur.setDaty(Utilitaire.dateDuJourSql());
                    }
                } else {
                    System.out.println("DEBUG: Classic parameters received");
                    // Extraction depuis les paramètres classiques (form-url-encoded)
                    compteur.setIdMachine(request.getParameter("idMachine"));
                    compteur.setHeure(request.getParameter("heure"));
                    compteur.setIdFabrication(request.getParameter("idFabrication"));
                    compteur.setIdOrigine(request.getParameter("idOrigine"));
                    
                    String nombreStr = request.getParameter("nombre");
                    if (nombreStr != null && !nombreStr.isEmpty()) {
                        compteur.setNombre(Double.parseDouble(nombreStr));
                    }
                    
                    String datyStr = request.getParameter("daty");
                    if (datyStr != null && !datyStr.isEmpty()) {
                        if (datyStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            compteur.setDaty(java.sql.Date.valueOf(datyStr));
                        } else {
                            compteur.setDaty(Utilitaire.stringDate(datyStr));
                        }
                    } else {
                        compteur.setDaty(Utilitaire.dateDuJourSql());
                    }
                }

                System.out.println("DEBUG: Compteur to create: Machine=" + compteur.getIdMachine() + ", Nombre=" + compteur.getNombre() + ", Daty=" + compteur.getDaty());

                // Persistance
                c.setAutoCommit(false);
                compteur.createObject(String.valueOf(u.getUser().getRefuser()), c);
                c.commit();

                Map<String, Object> simpleData = new HashMap<>();
                simpleData.put("id", compteur.getId());
                simpleData.put("idMachine", compteur.getIdMachine());
                simpleData.put("nombre", compteur.getNombre());
                simpleData.put("daty", compteur.getDaty());

                res.put("status", "success");
                res.put("message", "Compteur enregistré avec succès");
                res.put("data", simpleData);
            } else {
                throw new Exception("Action non reconnue : " + action);
            }

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
