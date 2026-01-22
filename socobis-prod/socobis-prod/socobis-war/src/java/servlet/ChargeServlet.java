package servlet;

import com.google.gson.Gson;
import charge.Charge;
import bean.CGenUtil;
import utilitaire.UtilDB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/ChargeServlet")
public class ChargeServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        Map<String, Object> res = new HashMap<String, Object>();
        Connection conn = null;
        try {
            String action = request.getParameter("action");
            if (action == null || action.isEmpty()) {
                action = "listByFabrication";
            }

            if ("listByFabrication".equalsIgnoreCase(action)) {
                String idFabrication = request.getParameter("idFabrication");
                if (idFabrication == null || idFabrication.trim().isEmpty()) {
                    throw new Exception("Paramètre idFabrication manquant");
                }

                UtilDB utilDB = new UtilDB();
                conn = utilDB.GetConn();

                Charge crit = new Charge();
                // Utilise la vue charge_cpl comme dans fabrication-charge.jsp
                crit.setNomTable("charge_cpl");
                crit.setIdfabrication(idFabrication);

                Object[] list = CGenUtil.rechercher(crit, null, null, conn, "");

                List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                if (list != null) {
                    for (Object obj : list) {
                        if (obj instanceof Charge) {
                            Charge c = (Charge) obj;
                            Map<String, Object> row = new HashMap<String, Object>();
                            row.put("id", c.getId());
                            Date d = c.getDaty();
                            row.put("daty", d != null ? d.toString() : null);
                            row.put("libelle", c.getLibelle());
                            row.put("typelib", c.getTypelib());
                            row.put("qte", c.getQte());
                            row.put("pu", c.getPu());
                            row.put("montant", c.getMontant());
                            row.put("etatlib", c.getEtatlib());
                            data.add(row);
                        }
                    }
                }

                res.put("status", "success");
                res.put("data", data);
            } else {
                throw new Exception("Action inconnue : " + action);
            }

        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception ignore) {
            }
        }

        out.print(gson.toJson(res));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
