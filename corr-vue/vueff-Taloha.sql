
CREATE OR REPLACE FORCE VIEW "SOCOBISPROD"."FACTUREFOURNISSEURCPL_TOUS" ("ID", "IDFOURNISSEUR", "IDFOURNISSEURLIB", "IDMODEPAIEMENT", "IDMODEPAIEMENTLIB", "DATY", "DESIGNATION", "DATEECHEANCEPAIEMENT", "ETATLIB", "REFERENCE", "IDBC", "IDMAGASIN", "IDMAGASINLIB", "DEVISE", "IDDEVISE", "ETAT", "TAUX", "MONTANTTVA", "MONTANTHT", "TAUXDECHANGE", "MONTANTTTC", "MONTANTPAYE", "MONTANTRESTE") AS 
  SELECT 
	fcpl.id,
	fcpl.idfournisseur,
	fcpl.idfournisseurlib,
	fcpl.idmodepaiement,
	fcpl.idmodepaiementlib,
	fcpl.daty,
	fcpl.designation,
	fcpl.dateecheancepaiement,
	fcpl.etatlib,
	fcpl.reference,
	fcpl.idbc,
	fcpl.idmagasin,
	fcpl.idmagasinlib,
	fcpl.devise,
	fcpl.iddevise,
	fcpl.etat,
	fcpl.taux,
	fcpl.montanttva,
	fcpl.montantht,
	fcpl.tauxdechange,
	fcpl.montantttc AS  MONTANTTTC, 
	nvl((fcpl.montantpaye * fcpl.TAUXDECHANGE),0) AS MONTANTPAYE,
	nvl((fcpl.montantreste * fcpl.TAUXDECHANGE),0) AS MONTANTRESTE
FROM FACTUREFOURNISSEURCPL fcpl;