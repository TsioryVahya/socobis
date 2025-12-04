-- Exemple de refonte: FACTUREFOURNISSEURCPL_TOUS
-- 1) Drop et recréez la vue en incluant la somme des paiements
--    (vérifiez les colonnes exactes de votre vue actuelle)
CREATE OR REPLACE VIEW FACTUREFOURNISSEURCPL_TOUS AS
select
  f.id,
  f.idfournisseur,
  f.idfournisseurlib,
  f.idmodepaiement,
  f.idmodepaiementlib,
  f.daty,
  f.designation,
  f.dateecheancepaiement,
  f.etat,
  f.etatlib,
  f.reference,
  f.idbc,
  f.idmagasin,
  f.devise,
  f.taux,
  f.idmagasinlib,
  f.iddevise,
  f.montanttva,
  f.montantht,
  f.montantttc,
  f.montantttcar,
  -- Recalcule MontantPayé à partir des liaisons paiement validées
  NVL((
    select sum(lp.montant)
    from LIAISONPAIEMENT lp
    where lp.id2 = f.id
      and NVL(lp.etat,0) >= 11
  ), 0) as montantpaye,
  -- Montant Reste = TTC - Payé (borné à >= 0)
  GREATEST(f.montantttc - NVL((
    select sum(lp2.montant)
    from LIAISONPAIEMENT lp2
    where lp2.id2 = f.id
      and NVL(lp2.etat,0) >= 11
  ), 0), 0) as montantreste,
  f.tauxdechange,
  f.idprevision
from FACTUREFOURNISSEURCPL f;
commit;