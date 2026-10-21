package com.gestionstock.stock;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gestionstock.audit.AuditService;
import com.gestionstock.auth.CurrentUser;
import com.gestionstock.common.BusinessException;
import com.gestionstock.common.NotFoundException;
import com.gestionstock.produit.Produit;
import com.gestionstock.produit.ProduitRepository;
import com.gestionstock.stock.StockDtos.*;

import lombok.RequiredArgsConstructor;

/**
 * Point d'entrée UNIQUE pour modifier le stock d'un produit.
 * Chaque variation crée un mouvement tracé (qui, quand, combien, pourquoi).
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementRepository;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    /** Mouvement saisi manuellement par un magasinier. */
    @Transactional
    public MouvementDto enregistrerManuel(MouvementRequest req) {
        if (req.motif().isAutomatique()) {
            throw new BusinessException("Le motif " + req.motif() + " est réservé à la facturation");
        }
        return MouvementDto.from(enregistrer(req.produitId(), req.motif(), req.quantite(),
                req.reference(), req.commentaire()));
    }

    /**
     * Doit être appelé dans une transaction existante (ex. création de facture) :
     * si la suite échoue, le mouvement est annulé avec elle.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public MouvementStock enregistrer(Long produitId, MotifMouvement motif, int quantite,
                                      String reference, String commentaire) {
        if (quantite <= 0) {
            throw new BusinessException("La quantité doit être positive");
        }
        Produit produit = produitRepository.findByIdPourMiseAJour(produitId)
                .orElseThrow(() -> new NotFoundException("Produit introuvable"));

        int nouveauStock = motif.getType() == TypeMouvement.ENTREE
                ? produit.getStockActuel() + quantite
                : produit.getStockActuel() - quantite;
        if (nouveauStock < 0) {
            throw new BusinessException("Stock insuffisant pour " + produit.getDesignation()
                    + " (disponible : " + produit.getStockActuel() + ", demandé : " + quantite + ")");
        }
        produit.setStockActuel(nouveauStock);

        MouvementStock m = new MouvementStock();
        m.setProduit(produit);
        m.setType(motif.getType());
        m.setMotif(motif);
        m.setQuantite(quantite);
        m.setStockApres(nouveauStock);
        m.setReference(reference);
        m.setCommentaire(commentaire);
        m.setUtilisateur(currentUser.get());
        mouvementRepository.save(m);

        auditService.log("MOUVEMENT_" + motif.getType(), "Produit", produit.getId(),
                motif + " x" + quantite + " (" + produit.getCode() + ") stock -> " + nouveauStock);
        return m;
    }

    @Transactional(readOnly = true)
    public Page<MouvementDto> rechercher(Long produitId, TypeMouvement type, MotifMouvement motif,
                                         LocalDate du, LocalDate au, int page, int taille) {
        LocalDate debut = du != null ? du : LocalDate.now().minusDays(30);
        LocalDate fin = au != null ? au : LocalDate.now();
        return mouvementRepository.rechercher(produitId, type, motif,
                        debut.atStartOfDay(), fin.plusDays(1).atStartOfDay(),
                        PageRequest.of(page, Math.min(taille, 200)))
                .map(MouvementDto::from);
    }
}
