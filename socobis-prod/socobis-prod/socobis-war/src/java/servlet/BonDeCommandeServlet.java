package servlet;

import bean.CGenUtil;
import com.google.gson.Gson;
import utilitaire.UtilDB;
import vente.BonDeCommandeCpl;

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

@WebServlet("/BonDeCommandeServlet")
public class BonDeCommandeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection c = null;
        try {
            c = new UtilDB().GetConn();

            BonDeCommandeCpl bc = new BonDeCommandeCpl();
            // Table BONDECOMMANDE_CLIENT_CPL déjà configurée dans le constructeur
            BonDeCommandeCpl[] results = (BonDeCommandeCpl[]) CGenUtil.rechercher(bc, null, null, c, "");

            List<Map<String, Object>> data = new ArrayList<>();
            if (results != null) {
                for (BonDeCommandeCpl b : results) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", b.getId());
                    item.put("designation", b.getDesignation());
                    item.put("client", b.getIdclientlib());
                    item.put("reference", b.getReference());
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
                try { c.close(); } catch (Exception ignore) {}
            }
        }
    }
}
