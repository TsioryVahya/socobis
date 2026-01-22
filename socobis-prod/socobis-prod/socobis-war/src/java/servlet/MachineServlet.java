package servlet;

import com.google.gson.Gson;
import machine.Machine;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/MachineServlet")
public class MachineServlet extends HttpServlet {

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
            UtilDB utilDB = new UtilDB();
            conn = utilDB.GetConn();

            Machine critere = new Machine();
            // Utilise la table MACHINE par défaut, possibilité dadapter plus tard si besoin

            Object[] list = CGenUtil.rechercher(critere, null, null, conn, " ORDER BY val");

            List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            if (list != null) {
                for (Object obj : list) {
                    if (obj instanceof Machine) {
                        Machine m = (Machine) obj;
                        Map<String, Object> row = new HashMap<String, Object>();
                        row.put("id", m.getId());
                        // La colonne fonctionnelle est "val" comme dans la JSP (new Liste("idMachine", mach, "val", "id"))
                        row.put("libelle", m.getVal());
                        data.add(row);
                    }
                }
            }

            res.put("status", "success");
            res.put("data", data);

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
        // Pour linstant on ne g8re que la lecture, on route POST vers GET si besoin.
        doGet(request, response);
    }
}
