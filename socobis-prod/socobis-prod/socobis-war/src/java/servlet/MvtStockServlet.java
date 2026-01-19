package servlet;

import com.google.gson.Gson;
import fabrication.Fabrication;
import stock.MvtStock;
import stock.MvtStockFille;
import user.UserEJB;
import utilitaire.Utilitaire;
import utilitaire.UtilDB;
import utils.ConstanteStation;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/MvtStockServlet")
public class MvtStockServlet extends HttpServlet {

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
            } else if ("getDetail".equalsIgnoreCase(action)) {
                handleGetDetail(request, res);
            } else if ("viserMouvement".equalsIgnoreCase(action)) {
                handleViserMouvement(request, u, res);
            } else {
                throw new Exception("Action inconnue pour MvtStockServlet : " + action);
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
        String type = request.getParameter("type"); // entree / sortie / residu

        if (idFab == null || idFab.trim().isEmpty()) {
            throw new Exception("Paramètre idFab manquant");
        }

        if (type == null || type.trim().isEmpty()) {
            type = "entree";
        }

        String typeMvtStock;
        if ("entree".equalsIgnoreCase(type)) {
            typeMvtStock = ConstanteStation.TYPEMVTSTOCKENTREE;
        } else if ("sortie".equalsIgnoreCase(type)) {
            typeMvtStock = ConstanteStation.TYPEMVTSTOCKSORTIE;
        } else {
            // Pour l'instant, on traite "residu" comme une entrée de stock classique
            typeMvtStock = ConstanteStation.TYPEMVTSTOCKENTREE;
        }

        Fabrication fab = new Fabrication();
        fab.setId(idFab);

        MvtStock mvt = fab.genererMvtStock(typeMvtStock, null);
        if (mvt == null) {
            res.put("status", "success");
            res.put("mere", null);
            res.put("filles", new Object[0]);
            return;
        }

        Map<String, Object> mere = new HashMap<>();
        mere.put("designation", mvt.getDesignation());
        mere.put("idMagasin", mvt.getIdMagasin());
        mere.put("idTypeMvStock", mvt.getIdTypeMvStock());
        mere.put("idobjet", mvt.getIdobjet());
        // Date : on utilise la date du jour si non renseignée
        Date daty = mvt.getDaty() != null ? mvt.getDaty() : Utilitaire.dateDuJourSql();
        mere.put("daty", daty.toString());

        bean.ClassFille[] fillesGeneriques = mvt.getFille();
        MvtStockFille[] filles;

        if (fillesGeneriques != null) {
            filles = new MvtStockFille[fillesGeneriques.length];
            for (int i = 0; i < fillesGeneriques.length; i++) {
                filles[i] = (MvtStockFille) fillesGeneriques[i];
            }
        } else {
            filles = new MvtStockFille[0];
        }
        Object[] fillesJson = new Object[filles.length];
        for (int i = 0; i < filles.length; i++) {
            MvtStockFille f = filles[i];
            Map<String, Object> fMap = new HashMap<>();
            fMap.put("idProduit", f.getIdProduit());
            fMap.put("designation", f.getDesignation());
            fMap.put("entree", f.getEntree());
            fMap.put("sortie", f.getSortie());
            fMap.put("pu", f.getPu());
            fMap.put("mvtSrc", f.getMvtSrc());
            fillesJson[i] = fMap;
        }

        res.put("status", "success");
        res.put("mere", mere);
        res.put("filles", fillesJson);
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

        String type = "entree";
        Object typeObj = data.get("type");
        if (typeObj != null && !typeObj.toString().trim().isEmpty()) {
            type = typeObj.toString();
        }

        String typeMvtStock;
        if ("entree".equalsIgnoreCase(type) || "residu".equalsIgnoreCase(type)) {
            typeMvtStock = ConstanteStation.TYPEMVTSTOCKENTREE;
        } else if ("sortie".equalsIgnoreCase(type)) {
            typeMvtStock = ConstanteStation.TYPEMVTSTOCKSORTIE;
        } else {
            typeMvtStock = ConstanteStation.TYPEMVTSTOCKENTREE;
        }

        Map<String, Object> mereMap = null;
        Object mereObj = data.get("mere");
        if (mereObj instanceof Map) {
            mereMap = (Map<String, Object>) mereObj;
        }

        List<Map<String, Object>> lignesList = null;
        Object lignesObj = data.get("lignes");
        if (lignesObj instanceof List) {
            lignesList = (List<Map<String, Object>>) lignesObj;
        }

        if (lignesList == null || lignesList.isEmpty()) {
            throw new Exception("Aucune ligne de mouvement de stock à enregistrer");
        }

        Connection c = null;
        try {
            UtilDB utilDB = new UtilDB();
            c = utilDB.GetConn();
            c.setAutoCommit(false);

            MvtStock mvt = new MvtStock();
            mvt.setIdobjet(idFab);
            mvt.setIdTypeMvStock(typeMvtStock);

            if (mereMap != null) {
                if (mereMap.get("designation") != null) {
                    mvt.setDesignation(mereMap.get("designation").toString());
                } else {
                    mvt.setDesignation("Mouvement de stock de l'ordre de fabrication : " + idFab);
                }

                if (mereMap.get("idMagasin") != null) {
                    mvt.setIdMagasin(mereMap.get("idMagasin").toString());
                }

                Object datyObj = mereMap.get("daty");
                if (datyObj != null) {
                    String datyStr = datyObj.toString();
                    if (datyStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        mvt.setDaty(Date.valueOf(datyStr));
                    } else {
                        mvt.setDaty(Utilitaire.stringDate(datyStr));
                    }
                } else {
                    mvt.setDaty(Utilitaire.dateDuJourSql());
                }
            } else {
                mvt.setDesignation("Mouvement de stock de l'ordre de fabrication : " + idFab);
                mvt.setDaty(Utilitaire.dateDuJourSql());
            }

            MvtStockFille[] filles = new MvtStockFille[lignesList.size()];
            for (int i = 0; i < lignesList.size(); i++) {
                Map<String, Object> fMap = lignesList.get(i);
                MvtStockFille f = new MvtStockFille();

                if (fMap.get("idProduit") != null) {
                    f.setIdProduit(fMap.get("idProduit").toString());
                }
                if (fMap.get("designation") != null) {
                    f.setDesignation(fMap.get("designation").toString());
                }
                // Sécuriser les conversions numériques pour éviter ORA-01722 (Nombre non valide)
                Object entreeObj = fMap.get("entree");
                if (entreeObj != null) {
                    String s = entreeObj.toString().trim();
                    if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                        f.setEntree(Utilitaire.stringToDouble(s));
                    }
                }

                Object sortieObj = fMap.get("sortie");
                if (sortieObj != null) {
                    String s = sortieObj.toString().trim();
                    if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                        f.setSortie(Utilitaire.stringToDouble(s));
                    }
                }

                Object puObj = fMap.get("pu");
                if (puObj != null) {
                    String s = puObj.toString().trim();
                    if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                        f.setPu(Utilitaire.stringToDouble(s));
                    }
                }
                if (fMap.get("mvtSrc") != null) {
                    f.setMvtSrc(fMap.get("mvtSrc").toString());
                }

                // Sécuriser les champs optionnels pouvant être numériques en base
                // (si ces colonnes sont de type NUMBER, une chaîne vide '' provoque ORA-01722)
                f.setIdVenteDetail(null);
                f.setIdTransfertDetail(null);
                if (f.getMvtSrc() != null && f.getMvtSrc().trim().isEmpty()) {
                    f.setMvtSrc(null);
                }

                System.out.println("DEBUG FROM saveFromFab: idx=" + i
                        + ", idProduit=" + f.getIdProduit()
                        + ", entree=" + f.getEntree()
                        + ", sortie=" + f.getSortie()
                        + ", pu=" + f.getPu()
                        + ", mvtSrc=" + f.getMvtSrc());

                filles[i] = f;
            }

            // La méthode createObjectMultiple de l'EJB gère l'enregistrement de la mère et des filles.
            // C'est la méthode standard utilisée par le framework JSP, garantissant que les données
            // sont traitées correctement pour éviter les erreurs de type (ORA-01722).
            u.createObjectMultiple(mvt, "idMvtStock", filles);

            // IMPORTANT : on ne lance pas ici la validation métier complète du stock
            // (MvtStock.validerObject), afin de reproduire le comportement de la JSP
            // mvtstock-saisie.jsp qui se contente d'enregistrer le mouvement.

            c.commit();

            res.put("status", "success");
            res.put("message", "Mouvement de stock enregistré avec succès");
            res.put("idMvtStock", mvt.getId());
        } catch (Exception e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (Exception ignore) {
                }
            }
            throw e;
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void handleGetDetail(HttpServletRequest request, Map<String, Object> res) throws Exception {
        String idMvt = request.getParameter("id");
        if (idMvt == null || idMvt.trim().isEmpty()) {
            throw new Exception("Paramètre id (mouvement) manquant pour l'action getDetail");
        }

        Connection c = null;
        try {
            UtilDB utilDB = new UtilDB();
            c = utilDB.GetConn();

            stock.MvtStockCpl mvtCpl = stock.MvtStockCpl.getSingleMouvement(idMvt, c);

            if (mvtCpl == null) {
                res.put("status", "error");
                res.put("message", "Mouvement de stock non trouvé pour l'ID: " + idMvt);
                return;
            }

            // Sérialisation manuelle pour un contrôle total du JSON
            Map<String, Object> data = new HashMap<>();
            data.put("id", mvtCpl.getId());
            data.put("designation", mvtCpl.getDesignation());
            data.put("idobjet", mvtCpl.getIdobjet());
            data.put("idMagasin", mvtCpl.getIdMagasin());
            data.put("libelleMagasin", mvtCpl.getLibelleMagasin());
            data.put("idVente", mvtCpl.getIdVente());
            data.put("idTransfert", mvtCpl.getIdTransfert());
            data.put("idTypeMvStock", mvtCpl.getIdTypeMvStock());
            data.put("libelleTypeMvtStock", mvtCpl.getLibelleTypeMvtStock());
            data.put("daty", mvtCpl.getDaty() != null ? mvtCpl.getDaty().toString() : null);
            data.put("etat", mvtCpl.getEtat());
            data.put("montant", mvtCpl.getMontant());

            List<Map<String, Object>> fillesJson = new java.util.ArrayList<>();
            if (mvtCpl.getFille() != null) {
                for (bean.ClassFille fille : mvtCpl.getFille()) {
                    if (fille instanceof MvtStockFille) {
                        MvtStockFille f = (MvtStockFille) fille;
                        Map<String, Object> fMap = new HashMap<>();
                        fMap.put("id", f.getId());
                        fMap.put("idProduit", f.getIdProduit());
                        fMap.put("designation", f.getDesignation());
                        fMap.put("entree", f.getEntree());
                        fMap.put("sortie", f.getSortie());
                        fMap.put("pu", f.getPu());
                        fMap.put("mvtSrc", f.getMvtSrc());
                        fillesJson.add(fMap);
                    }
                }
            }
            data.put("filles", fillesJson);

            res.put("status", "success");
            res.put("data", data);

        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void handleViserMouvement(HttpServletRequest request, UserEJB u, Map<String, Object> res) throws Exception {
        String idMvt = request.getParameter("id");
        if (idMvt == null || idMvt.trim().isEmpty()) {
            throw new Exception("Paramètre id (mouvement) manquant pour l'action viserMouvement");
        }

        MvtStock mvt = new MvtStock();
        mvt.setId(idMvt);

        // Utiliser le framework EJB pour la validation, comme pour la fabrication
        u.validerObject(mvt);

        res.put("status", "success");
        res.put("message", "Mouvement de stock visé avec succès");
    }
}
