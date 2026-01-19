package servlet;

import bean.CGenUtil;
import bean.TypeObjet;
import com.google.gson.Gson;
import utilitaire.UtilDB;

import javax.servlet.ServletException;
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

@WebServlet("/MagasinPointServlet")
public class MagasinPointServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection c = null;
        try {
            c = new UtilDB().GetConn();

            TypeObjet typeMagasinPoint = new TypeObjet("MAGASINPOINT");
            TypeObjet[] results = (TypeObjet[]) CGenUtil.rechercher(typeMagasinPoint, null, null, c, "");

            List<Map<String, String>> data = new ArrayList<>();
            if (results != null) {
                for (TypeObjet t : results) {
                    Map<String, String> item = new HashMap<>();
                    item.put("id", t.getId());
                    item.put("libelle", t.getVal());
                    data.add(item);
                }
            }

            Gson gson = new Gson();
            String json = gson.toJson(data);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
