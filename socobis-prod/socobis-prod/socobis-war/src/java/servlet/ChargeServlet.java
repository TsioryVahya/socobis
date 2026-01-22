package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import bean.CGenUtil;
import charge.Charge;
import user.UserEJB;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;

@WebServlet("/ChargeServlet")
public class ChargeServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> res = new HashMap<>();

        try {
            String action = request.getParameter("action");
            if ("getTypes".equalsIgnoreCase(action)) {
                UtilDB utilDB = new UtilDB();
                java.sql.Connection conn = null;
                try {
                    conn = utilDB.GetConn();
                    bean.TypeObjet typeC = new bean.TypeObjet();
                    typeC.setNomTable("typecharge");
                    Object[] list = CGenUtil.rechercher(typeC, null, null, conn, "");

                    List<Map<String, Object>> data = new ArrayList<>();
                    if (list != null) {
                        for (Object obj : list) {
                            if (obj instanceof bean.TypeObjet) {
                                bean.TypeObjet t = (bean.TypeObjet) obj;
                                Map<String, Object> item = new HashMap<>();
                                item.put("id", t.getId());
                                item.put("val", t.getVal());
                                data.add(item);
                            }
                        }
                    }
                    res.put("status", "success");
                    res.put("data", data);
                } finally {
                    if (conn != null)
                        conn.close();
                }
            } else if ("list".equalsIgnoreCase(action)) {
                String idFab = request.getParameter("idFabrication");
                if (idFab == null || idFab.isEmpty()) {
                    throw new Exception("ID Fabrication manquant");
                }
                UtilDB utilDB = new UtilDB();
                java.sql.Connection conn = null;
                try {
                    conn = utilDB.GetConn();
                    Charge critere = new Charge();
                    critere.setNomTable("CHARGE");
                    Object[] list = CGenUtil.rechercher(critere, null, null, conn,
                            " AND idfabrication = '" + idFab + "'");

                    List<Map<String, Object>> data = new ArrayList<>();
                    if (list != null) {
                        for (Object obj : list) {
                            if (obj instanceof Charge) {
                                Charge crg = (Charge) obj;
                                Map<String, Object> item = new HashMap<>();
                                item.put("id", crg.getId());
                                item.put("idfabrication", crg.getIdfabrication());
                                item.put("idingredients", crg.getIdingredients());
                                item.put("daty", crg.getDaty());
                                item.put("libelle", crg.getLibelle());
                                item.put("pu", crg.getPu());
                                item.put("qte", crg.getQte());
                                item.put("montant", crg.getPu() * crg.getQte());
                                item.put("type", crg.getType());
                                data.add(item);
                            }
                        }
                    }
                    res.put("status", "success");
                    res.put("data", data);
                } finally {
                    if (conn != null)
                        conn.close();
                }
            } else {
                throw new Exception("Action inconnue");
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        } finally {
            out.print(gson.toJson(res));
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> res = new HashMap<>();

        try {
            HttpSession session = request.getSession();
            UserEJB u = (UserEJB) session.getAttribute("u");
            if (u == null) {
                throw new Exception("Session expirée ou utilisateur non connecté");
            }

            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            if (sb.length() > 0) {
                Map<String, Object> payload = gson.fromJson(sb.toString(), Map.class);
                String idFab = (String) payload.get("idFabrication");
                List<Map<String, Object>> chargesList = (List<Map<String, Object>>) payload.get("charges");

                if (idFab == null || idFab.isEmpty()) {
                    throw new Exception("ID Fabrication manquant");
                }

                if (chargesList != null && !chargesList.isEmpty()) {
                    Charge[] charges = new Charge[chargesList.size()];
                    for (int i = 0; i < chargesList.size(); i++) {
                        Map<String, Object> cMap = chargesList.get(i);
                        charges[i] = new Charge();
                        charges[i].setNomTable("CHARGE");
                        charges[i].setIdfabrication(idFab);

                        if (cMap.get("daty") != null) {
                            charges[i].setDaty(java.sql.Date.valueOf(cMap.get("daty").toString()));
                        } else {
                            charges[i].setDaty(Utilitaire.dateDuJourSql());
                        }

                        if (cMap.get("libelle") != null)
                            charges[i].setLibelle(cMap.get("libelle").toString());
                        if (cMap.get("idIngredients") != null)
                            charges[i].setIdingredients(cMap.get("idIngredients").toString());
                        if (cMap.get("pu") != null)
                            charges[i].setPu(Utilitaire.stringToDouble(cMap.get("pu").toString()));
                        if (cMap.get("qte") != null)
                            charges[i].setQte(Utilitaire.stringToDouble(cMap.get("qte").toString()));
                        if (cMap.get("type") != null)
                            charges[i].setType(cMap.get("type").toString());
                        charges[i].setEtat(1);
                    }
                    u.createObjectFilleMultiple(idFab, "idfabrication", charges);
                    res.put("status", "success");
                    res.put("message", "Charges enregistrées avec succès");
                } else {
                    throw new Exception("Aucune charge à enregistrer");
                }
            } else {
                throw new Exception("Corps de la requête vide");
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        } finally {
            out.print(gson.toJson(res));
            out.close();
        }
    }
}
