package com.gestionstock.stock;

/** Motif d'un mouvement : explique "comment" le produit est entré ou sorti. */
public enum MotifMouvement {
    // Entrées
    STOCK_INITIAL(TypeMouvement.ENTREE),
    RECEPTION_FOURNISSEUR(TypeMouvement.ENTREE),
    RETOUR_CLIENT(TypeMouvement.ENTREE),
    AJUSTEMENT_POSITIF(TypeMouvement.ENTREE),
    ANNULATION_VENTE(TypeMouvement.ENTREE),
    // Sorties
    VENTE(TypeMouvement.SORTIE),
    TRANSFERT(TypeMouvement.SORTIE),
    PERTE(TypeMouvement.SORTIE),
    CASSE(TypeMouvement.SORTIE),
    PEREMPTION(TypeMouvement.SORTIE),
    USAGE_INTERNE(TypeMouvement.SORTIE),
    RETOUR_FOURNISSEUR(TypeMouvement.SORTIE),
    AJUSTEMENT_NEGATIF(TypeMouvement.SORTIE);

    private final TypeMouvement type;

    MotifMouvement(TypeMouvement type) {
        this.type = type;
    }

    public TypeMouvement getType() {
        return type;
    }

    /** Motifs réservés aux opérations automatiques (création de produit, facturation), pas à la saisie manuelle. */
    public boolean isAutomatique() {
        return this == STOCK_INITIAL || this == VENTE || this == ANNULATION_VENTE;
    }
}
