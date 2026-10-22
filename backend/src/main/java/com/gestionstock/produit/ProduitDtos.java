package com.gestionstock.produit;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProduitDtos {
    private ProduitDtos() {}

    public record ProduitDto(Long id, String code, String codeBarre, String designation,
                             Long categorieId, String categorieNom, String unite,
                             BigDecimal prixAchat, BigDecimal prixVente, BigDecimal tauxTva,
                             int seuilAlerte, int stockActuel, String emplacement,
                             boolean actif, boolean enAlerte) {
        public static ProduitDto from(Produit p) {
            Categorie c = p.getCategorie();
            return new ProduitDto(p.getId(), p.getCode(), p.getCodeBarre(), p.getDesignation(),
                    c != null ? c.getId() : null, c != null ? c.getNom() : null, p.getUnite(),
                    p.getPrixAchat(), p.getPrixVente(), p.getTauxTva(),
                    p.getSeuilAlerte(), p.getStockActuel(), p.getEmplacement(),
                    p.isActif(), p.isEnAlerte());
        }
    }

    /**
     * stockInitial : quantité déjà en magasin à la création (enregistrée comme un mouvement STOCK_INITIAL).
     * Ignoré en modification : ensuite le stock ne change QUE par des mouvements.
     * tauxTva : facultatif, le taux par défaut (app.tva-par-defaut) est appliqué s'il est absent.
     */
    public record ProduitRequest(
            String code,
            String codeBarre,
            @NotBlank String designation,
            Long categorieId,
            @NotBlank String unite,
            @NotNull @DecimalMin("0") BigDecimal prixAchat,
            @NotNull @DecimalMin("0") BigDecimal prixVente,
            @DecimalMin("0") BigDecimal tauxTva,
            @Min(0) int seuilAlerte,
            String emplacement,
            boolean actif,
            @Min(0) Integer stockInitial) {}
}
