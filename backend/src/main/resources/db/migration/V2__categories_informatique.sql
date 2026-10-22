-- Catégories de départ (d'autres peuvent être ajoutées depuis l'écran Produits)
INSERT INTO categorie (nom) VALUES
    ('Ordinateur'),
    ('Smartphone'),
    ('Tablette'),
    ('Imprimante'),
    ('Accessoire')
ON CONFLICT (nom) DO NOTHING;
