package com.gestionstock.vente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionstock.audit.AuditService;
import com.gestionstock.auth.CurrentUser;
import com.gestionstock.common.BusinessException;
import com.gestionstock.common.NotFoundException;
import com.gestionstock.produit.Produit;
import com.gestionstock.produit.ProduitRepository;
import com.gestionstock.stock.MotifMouvement;
import com.gestionstock.stock.StockService;
import com.gestionstock.vente.VenteDtos.*;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FactureService {

    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private final FactureRepository factureRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;
    private final StockService stockService;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    private final EntityManager entityManager;

    /**
     * Crée la facture ET les sorties de stock dans UNE SEULE transaction :
     * si un produit est en rupture, rien n'est enregistré.
     */
    @Transactional
    public FactureDto creer(FactureRequest req) {
        Facture f = new Facture();
        f.setNumero(prochainNumero());
        f.setModePaiement(req.modePaiement());
        f.setUtilisateur(currentUser.get());
        if (req.clientId() != null) {
            f.setClient(clientRepository.findById(req.clientId())
                    .orElseThrow(() -> new NotFoundException("Client introuvable")));
        }

        BigDecimal totalHt = BigDecimal.ZERO;
        BigDecimal totalTva = BigDecimal.ZERO;
        for (LigneRequest lr : req.lignes()) {
            Produit p = produitRepository.findById(lr.produitId())
                    .orElseThrow(() -> new NotFoundException("Produit introuvable : " + lr.produitId()));
            if (!p.isActif()) {
                throw new BusinessException("Le produit " + p.getDesignation() + " n'est plus en vente");
            }
            BigDecimal remise = lr.remise() != null ? lr.remise() : BigDecimal.ZERO;
            BigDecimal brut = p.getPrixVente().multiply(BigDecimal.valueOf(lr.quantite()));
            if (remise.compareTo(brut) > 0) {
                throw new BusinessException("La remise dépasse le montant de la ligne " + p.getDesignation());
            }
            BigDecimal ht = brut.subtract(remise).setScale(2, RoundingMode.HALF_UP);
            BigDecimal tva = ht.multiply(p.getTauxTva()).divide(CENT, 2, RoundingMode.HALF_UP);

            LigneFacture l = new LigneFacture();
            l.setProduit(p);
            l.setQuantite(lr.quantite());
            l.setPrixUnitaire(p.getPrixVente());
            l.setTauxTva(p.getTauxTva());
            l.setRemise(remise);
            l.setTotalLigne(ht);
            f.ajouterLigne(l);

            totalHt = totalHt.add(ht);
            totalTva = totalTva.add(tva);
        }
        f.setTotalHt(totalHt);
        f.setTotalTva(totalTva);
        f.setTotalTtc(totalHt.add(totalTva));
        factureRepository.save(f);

        for (LigneFacture l : f.getLignes()) {
            stockService.enregistrer(l.getProduit().getId(), MotifMouvement.VENTE, l.getQuantite(),
                    f.getNumero(), null);
        }
        auditService.log("CREATION_FACTURE", "Facture", f.getId(),
                f.getNumero() + " - " + f.getTotalTtc() + " (" + f.getModePaiement() + ")");
        return FactureDto.detail(f);
    }

    /** Une facture n'est jamais supprimée : elle est annulée et le stock est réintégré. */
    @Transactional
    public FactureDto annuler(Long id, String motif) {
        Facture f = factureRepository.findDetail(id).orElseThrow(() -> new NotFoundException("Facture introuvable"));
        if (f.getStatut() == StatutFacture.ANNULEE) {
            throw new BusinessException("Cette facture est déjà annulée");
        }
        f.setStatut(StatutFacture.ANNULEE);
        f.setMotifAnnulation(motif);
        for (LigneFacture l : f.getLignes()) {
            stockService.enregistrer(l.getProduit().getId(), MotifMouvement.ANNULATION_VENTE, l.getQuantite(),
                    f.getNumero(), "Annulation : " + motif);
        }
        auditService.log("ANNULATION_FACTURE", "Facture", id, f.getNumero() + " - motif : " + motif);
        return FactureDto.detail(f);
    }

    /**
     * Numéro FAC-AAAA-NNNNNN continu et sans trou (obligation légale) : l'upsert verrouille le compteur
     * de l'année jusqu'à la fin de la transaction, et l'incrément est annulé si la vente échoue.
     */
    private String prochainNumero() {
        int annee = Year.now().getValue();
        Number numero = (Number) entityManager.createNativeQuery("""
                INSERT INTO numerotation_facture (annee, dernier_numero) VALUES (:annee, 1)
                ON CONFLICT (annee) DO UPDATE SET dernier_numero = numerotation_facture.dernier_numero + 1
                RETURNING dernier_numero
                """)
                .setParameter("annee", annee)
                .getSingleResult();
        return "FAC-%d-%06d".formatted(annee, numero.intValue());
    }

    @Transactional(readOnly = true)
    public FactureDto obtenir(Long id) {
        return FactureDto.detail(factureRepository.findDetail(id)
                .orElseThrow(() -> new NotFoundException("Facture introuvable")));
    }

    @Transactional(readOnly = true)
    public List<FactureDto> rechercher(LocalDate du, LocalDate au) {
        LocalDate debut = du != null ? du : LocalDate.now();
        LocalDate fin = au != null ? au : LocalDate.now();
        return factureRepository.rechercher(debut.atStartOfDay(), fin.plusDays(1).atStartOfDay())
                .stream().map(FactureDto::resume).toList();
    }
}
