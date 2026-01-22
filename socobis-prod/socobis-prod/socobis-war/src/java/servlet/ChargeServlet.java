package servlet;

import bean.CGenUtil;
import bean.TypeObjet;
import charge.Charge;
import charge.ChargeLib;
import com.google.gson.Gson;
import user.UserEJB;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            String action = request.getParameter("action");
            if ("prepareFromFab".equalsIgnoreCase(action)) {
                handlePrepareFromFab(request, res);
            } else if ("saveFromFab".equalsIgnoreCase(action)) {
                handleSaveFromFab(request, u, res);
            } else if ("listByFab".equalsIgnoreCase(action)) {
                handleListByFab(request, res);
            } else if ("viserCharge".equalsIgnoreCase(action)) {
                handleViserCharge(request, u, res);
            } else {
                throw new Exception("Action inconnue pour ChargeServlet : " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage() != null ? e.getMessage() : e.toString());
            if (e.getCause() != null) {
                res.put("cause", e.getCause().getMessage());
            }
        } finally {
            out.print(gson.toJson(res));
        }
    }

    private void handlePrepareFromFab(HttpServletRequest request, Map<String, Object> res) throws Exception {
        String idFab = request.getParameter("idFab");
        if (idFab == null || idFab.trim().isEmpty()) {
            throw new Exception("Paramètre idFab manquant");
        }

        Map<String, Object> mere = new HashMap<>();
        mere.put("idFabrication", idFab);
        Date daty = Utilitaire.dateDuJourSql();
        mere.put("daty", daty != null ? daty.toString() : null);

        Connection c = null;
        try {
            c = new UtilDB().GetConn();
            TypeObjet t = new TypeObjet();
            t.setNomTable("typecharge");
            Object[] types = CGenUtil.rechercher(t, null, null, c, "");

            List<Map<String, Object>> typesJson = new ArrayList<>();
            if (types != null) {
                for (Object obj : types) {
                    if (obj instanceof TypeObjet) {
                        TypeObjet to = (TypeObjet) obj;
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", to.getId());
                        row.put("val", to.getVal());
                        typesJson.add(row);
                    }
                }
            }

            res.put("status", "success");
            res.put("mere", mere);
            res.put("types", typesJson);
            res.put("lignes", new Object[0]);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void handleListByFab(HttpServletRequest request, Map<String, Object> res) throws Exception {
        String idFab = request.getParameter("idFab");
        if (idFab == null || idFab.trim().isEmpty()) {
            throw new Exception("Paramètre idFab manquant");
        }

        Connection c = null;
        try {
            c = new UtilDB().GetConn();
            Charge bc = new Charge();
            bc.setNomTable("charge_cpl");

            Object[] list = CGenUtil.rechercher(bc, null, null, c, " and idfabrication='" + idFab + "' order by daty desc, id desc");

            List<Map<String, Object>> data = new ArrayList<>();
            if (list != null) {
                for (Object obj : list) {
                    if (obj instanceof Charge) {
                        Charge ch = (Charge) obj;
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", ch.getId());
                        row.put("daty", ch.getDaty() != null ? ch.getDaty().toString() : null);
                        row.put("libelle", ch.getLibelle());
                        row.put("typelib", ch.getTypelib());
                        row.put("qte", ch.getQte());
                        row.put("pu", ch.getPu());
                        row.put("montant", ch.getMontant());
                        row.put("etat", ch.getEtat());
                        row.put("etatlib", ch.getEtatlib());
                        row.put("idIngredients", ch.getIdingredients());
                        data.add(row);
                    }
                }
            }

            res.put("status", "success");
            res.put("data", data);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSaveFromFab(HttpServletRequest request, UserEJB u, Map<String, Object> res) throws Exception {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            throw new Exception("Content-Type application/json attendu pour saveFromFab");
        }

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        if (sb.length() == 0) {
            throw new Exception("Corps JSON vide pour saveFromFab");
        }

        Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
        if (data == null) {
            throw new Exception("JSON invalide pour saveFromFab");
        }

        Object idFabObj = data.get("idFab");
        if (idFabObj == null || idFabObj.toString().trim().isEmpty()) {
            throw new Exception("Paramètre idFab manquant dans le JSON");
        }
        String idFab = idFabObj.toString();

        List<Map<String, Object>> lignesList = null;
        Object lignesObj = data.get("lignes");
        if (lignesObj instanceof List) {
            lignesList = (List<Map<String, Object>>) lignesObj;
        }

        if (lignesList == null || lignesList.isEmpty()) {
            throw new Exception("Aucune ligne de charge à enregistrer");
        }

        Charge[] charges = new Charge[lignesList.size()];
        for (int i = 0; i < lignesList.size(); i++) {
            Map<String, Object> fMap = lignesList.get(i);
            Charge ch = new Charge();
            ch.setNomTable("CHARGE");
            ch.setIdfabrication(idFab);

            Object datyObj = fMap.get("daty");
            if (datyObj != null) {
                String datyStr = datyObj.toString().trim();
                if (!datyStr.isEmpty() && !"null".equalsIgnoreCase(datyStr)) {
                    if (datyStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        ch.setDaty(Date.valueOf(datyStr));
                    } else {
                        ch.setDaty(Utilitaire.stringDate(datyStr));
                    }
                }
            }
            if (ch.getDaty() == null) {
                ch.setDaty(Utilitaire.dateDuJourSql());
            }

            if (fMap.get("libelle") != null) {
                ch.setLibelle(fMap.get("libelle").toString());
            }
            if (fMap.get("type") != null) {
                ch.setType(fMap.get("type").toString());
            }
            if (fMap.get("idIngredients") != null) {
                String s = fMap.get("idIngredients").toString().trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                    ch.setIdingredients(s);
                }
            }

            Object qteObj = fMap.get("qte");
            if (qteObj != null) {
                String s = qteObj.toString().trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                    ch.setQte(Utilitaire.stringToDouble(s));
                }
            }

            Object puObj = fMap.get("pu");
            if (puObj != null) {
                String s = puObj.toString().trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                    ch.setPu(Utilitaire.stringToDouble(s));
                }
            }

            charges[i] = ch;
        }

        u.createObjectFilleMultiple(idFab, "idfabrication", charges);

        res.put("status", "success");
        res.put("message", "Charges enregistrées avec succès");
    }

    private void handleViserCharge(HttpServletRequest request, UserEJB u, Map<String, Object> res) throws Exception {
        String id = request.getParameter("id");
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("Paramètre id manquant pour l'action viserCharge");
        }

        Charge ch = new Charge();
        ch.setId(id);
        ch.setNomTable("CHARGE");
        u.validerObject(ch);

        res.put("status", "success");
        res.put("message", "Charge visée avec succès");
    }
}
