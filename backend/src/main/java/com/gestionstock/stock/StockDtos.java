package com.gestionstock.stock;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class StockDtos {
    private StockDtos() {}

    public record MouvementRequest(
            @NotNull Long produitId,
            @NotNull MotifMouvement motif,
            @Min(1) int quantite,
            @Size(max = 100) String reference,
            @Size(max = 500) String commentaire) {}

    public record MouvementDto(Long id, LocalDateTime date, Long produitId, String produitCode,
                               String produitDesignation, TypeMouvement type, MotifMouvement motif,
                               int quantite, int stockApres, String reference, String commentaire,
                               String utilisateur) {
        public static MouvementDto from(MouvementStock m) {
            return new MouvementDto(m.getId(), m.getDateMouvement(), m.getProduit().getId(),
                    m.getProduit().getCode(), m.getProduit().getDesignation(), m.getType(), m.getMotif(),
                    m.getQuantite(), m.getStockApres(), m.getReference(), m.getCommentaire(),
                    m.getUtilisateur().getNomComplet());
        }
    }

    public record MotifDto(MotifMouvement code, TypeMouvement type) {}
}
