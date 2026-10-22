-- Entreprise non assujettie à la TVA : tous les produits passent à 0 %.
-- Les factures déjà émises ne sont PAS modifiées (obligation légale).
UPDATE produit SET taux_tva = 0;