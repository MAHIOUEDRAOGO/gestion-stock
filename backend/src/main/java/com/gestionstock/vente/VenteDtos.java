package com.gestionstock.vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public final class VenteDtos {
    private VenteDtos() {}

    public record LigneRequest(@NotNull Long produitId, @Min(1) int quantite,
                               @DecimalMin("0") BigDecimal remise) {}

    public record FactureRequest(Long clientId,
                                 @NotNull ModePaiement modePaiement,
                                 @NotEmpty List<@Valid LigneRequest> lignes) {}

    public record AnnulationRequest(@NotBlank String motif) {}

    public record LigneDto(Long produitId, String produitCode, String designation, int quantite,
                           BigDecimal prixUnitaire, BigDecimal tauxTva, BigDecimal remise, BigDecimal totalLigne) {
        static LigneDto from(LigneFacture l) {
            return new LigneDto(l.getProduit().getId(), l.getProduit().getCode(), l.getProduit().getDesignation(),
                    l.getQuantite(), l.getPrixUnitaire(), l.getTauxTva(), l.getRemise(), l.getTotalLigne());
        }
    }

    public record FactureDto(Long id, String numero, LocalDateTime date, Long clientId, String client,
                             BigDecimal totalHt, BigDecimal totalTva, BigDecimal totalTtc,
                             ModePaiement modePaiement, StatutFacture statut, String motifAnnulation,
                             String caissier, List<LigneDto> lignes) {
        static FactureDto resume(Facture f) {
            return of(f, List.of());
        }

        static FactureDto detail(Facture f) {
            return of(f, f.getLignes().stream().map(LigneDto::from).toList());
        }

        private static FactureDto of(Facture f, List<LigneDto> lignes) {
            Client c = f.getClient();
            return new FactureDto(f.getId(), f.getNumero(), f.getDateFacture(),
                    c != null ? c.getId() : null, c != null ? c.getNom() : "Client de passage",
                    f.getTotalHt(), f.getTotalTva(), f.getTotalTtc(), f.getModePaiement(), f.getStatut(),
                    f.getMotifAnnulation(), f.getUtilisateur().getNomComplet(), lignes);
        }
    }
}
