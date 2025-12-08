CREATE OR REPLACE FORCE VIEW SOCOBISPROD.FACTUREFOURNISSEURCPL_TOUS
(
  ID, IDFOURNISSEUR, IDFOURNISSEURLIB, IDMODEPAIEMENT, IDMODEPAIEMENTLIB,
  DATY, DESIGNATION, DATEECHEANCEPAIEMENT, ETATLIB, REFERENCE,
  IDBC, IDMAGASIN, IDMAGASINLIB, DEVISE, IDDEVISE, ETAT, TAUX,
  MONTANTTVA, MONTANTHT, TAUXDECHANGE, MONTANTTTC, MONTANTPAYE, MONTANTRESTE
) AS
SELECT
  x.id,
  x.idfournisseur,
  x.idfournisseurlib,
  x.idmodepaiement,
  x.idmodepaiementlib,
  x.daty,
  x.designation,
  x.dateecheancepaiement,
  x.etatlib,
  x.reference,
  x.idbc,
  x.idmagasin,
  x.idmagasinlib,
  x.devise,
  x.iddevise,
  x.etat,
  x.taux,
  x.montanttva,
  x.montantht,
  x.tauxdechange,
  x.montantttc,
  ROUND(
    NVL((
      SELECT SUM(lp.montant)
      FROM SOCOBISPROD.LIAISONPAIEMENT lp
      WHERE lp.id2 = x.id AND NVL(lp.etat, 0) >= 11
    ), 0) * NVL(x.tauxdechange, 1), 2
  ) AS montantpaye,
  ROUND(
    GREATEST(
      NVL(x.montantttc, 0) * NVL(x.tauxdechange, 1) -
      NVL((
        SELECT SUM(lp2.montant)
        FROM SOCOBISPROD.LIAISONPAIEMENT lp2
        WHERE lp2.id2 = x.id AND NVL(lp2.etat, 0) >= 11
      ), 0) * NVL(x.tauxdechange, 1),
      0
    ), 2
  ) AS montantreste
FROM (
  SELECT
    f.*,
    ROW_NUMBER() OVER (PARTITION BY f.id ORDER BY f.daty DESC, f.etat DESC) rn
  FROM SOCOBISPROD.FACTUREFOURNISSEURCPL f
) x
WHERE x.rn = 1;