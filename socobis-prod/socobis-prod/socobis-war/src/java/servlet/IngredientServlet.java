package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import produits.IngredientCpl;
import produits.Recette;
import produits.RecetteLib;
import utilitaire.UtilDB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/IngredientServlet")
public class IngredientServlet extends HttpServlet {

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> res = new HashMap<>();
        Connection c = null;

        try {
            String action = request.getParameter("action");
            if ("getDetail".equalsIgnoreCase(action)) {
                String idIngredient = request.getParameter("id");
                if (idIngredient == null || idIngredient.trim().isEmpty()) {
                    throw new Exception("Paramètre 'id' manquant");
                }

                UtilDB utilDB = new UtilDB();
                c = utilDB.GetConn();

                IngredientCpl ingredient = IngredientCpl.getDetailIngredient(idIngredient, c);

                res.put("status", "success");
                res.put("data", ingredient); // Gson va sérialiser l'objet complexe

            } else {
                throw new Exception("Action inconnue pour IngredientServlet: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage() != null ? e.getMessage() : e.toString());
        } finally {
            if (c != null) {
                try { c.close(); } catch (Exception ignored) {}
            }
            out.print(gson.toJson(res));
        }
    }
}

