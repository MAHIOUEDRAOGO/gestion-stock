-- Le prix d'achat devient facultatif : NULL signifie "non renseigné"
ALTER TABLE produit ALTER COLUMN prix_achat DROP NOT NULL;
ALTER TABLE produit ALTER COLUMN prix_achat DROP DEFAULT;
