package servlet;

import bean.CGenUtil;
import com.google.gson.Gson;
import produits.IngredientsLib;
import utilitaire.UtilDB;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/IngredientsServlet")
public class IngredientsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Connection c = null;
        try {
            c = new UtilDB().GetConn();

            IngredientsLib ing = new IngredientsLib();
            IngredientsLib[] results = (IngredientsLib[]) CGenUtil.rechercher(ing, null, null, c, "");

            List<Map<String, Object>> data = new ArrayList<>();
            if (results != null) {
                for (IngredientsLib i : results) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", i.getId());
                    item.put("libelle", i.getLibelle());
                    item.put("unite", i.getUnite());
                    data.add(item);
                }
            }

            String json = new Gson().toJson(data);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            if (c != null) {
                try { c.close(); } catch (Exception ignore) {}
            }
        }
    }
}
