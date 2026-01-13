package servlet;

import com.google.gson.Gson;
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
import user.UserEJB;
import user.UserEJBClient;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> res = new HashMap<>();

        try {
            String identifiant = request.getParameter("identifiant");
            String passe = request.getParameter("passe");
            String interim = request.getParameter("interim");
            String service = request.getParameter("service");

            // Fallback JSON si paramètres URL absents
            if (identifiant == null) {
                Map<String, Object> jsonData = gson.fromJson(request.getReader(), Map.class);
                if (jsonData != null) {
                    identifiant = (String) jsonData.get("identifiant");
                    passe = (String) jsonData.get("passe");
                    interim = (String) jsonData.get("interim");
                    service = (String) jsonData.get("service");
                }
            }

            if (identifiant == null || passe == null) {
                throw new Exception("Identifiant et mot de passe requis");
            }

            UserEJB u = UserEJBClient.lookupUserEJBBeanLocal();
            u.testLogin(identifiant, passe, interim, service);
            
            HttpSession session = request.getSession();
            session.setMaxInactiveInterval(216000);
            session.setAttribute("u", u);

            res.put("status", "success");
            res.put("message", "Connexion réussie");
            res.put("user", u.getUser());
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.put("status", "error");
            res.put("message", e.getMessage());
        } finally {
            out.print(gson.toJson(res));
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> res = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("u") != null) {
            UserEJB u = (UserEJB) session.getAttribute("u");
            res.put("status", "success");
            res.put("authenticated", true);
            res.put("user", u.getUser());
        } else {
            res.put("status", "success");
            res.put("authenticated", false);
        }
        out.print(gson.toJson(res));
        out.close();
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{\"status\":\"success\",\"message\":\"Déconnecté\"}");
        out.close();
    }
}
