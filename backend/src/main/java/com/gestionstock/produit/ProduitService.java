package com.gestionstock.produit;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.gestionstock.common.BusinessException;
import com.gestionstock.stock.MouvementStockRepository;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionstock.audit.AuditService;
import com.gestionstock.common.NotFoundException;
import com.gestionstock.produit.ProduitDtos.*;
import com.gestionstock.stock.MotifMouvement;
import com.gestionstock.stock.StockService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final AuditService auditService;
    private final StockService stockService;
    private final MouvementStockRepository mouvementRepository;

    /** Taux appliqué quand le formulaire ne précise pas la TVA (modifiable dans application.yml). */
    @Value("${app.tva-par-defaut:18}")
    private BigDecimal tvaParDefaut;

    @Transactional(readOnly = true)
    public List<ProduitDto> rechercher(String recherche, boolean actifsSeulement) {
        String r = recherche == null || recherche.isBlank() ? null : recherche.trim();
        return produitRepository.rechercher(r, actifsSeulement).stream().map(ProduitDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ProduitDto obtenir(Long id) {
        return ProduitDto.from(trouver(id));
    }

    @Transactional
    public ProduitDto creer(ProduitRequest req) {
       // if (produitRepository.existsByCode(req.code())) {
       //     throw new BusinessException("Le code produit " + req.code() + " existe déjà");
      //  }
      //  Produit p = new Produit();
      //  p.setTauxTva(tvaParDefaut);
       // appliquer(p, req);
       // produitRepository.save(p);

        Produit p = new Produit();
        p.setTauxTva(tvaParDefaut);
        p.setCode("TEMP-" + UUID.randomUUID()); // code provisoire : l'id n'existe pas encore
        appliquer(p, req);
        produitRepository.save(p);              // la base attribue l'id
        p.setCode("P%05d".formatted(p.getId())); // code définitif : P00001, P00002…


        auditService.log("CREATION_PRODUIT", "Produit", p.getId(), p.getCode() + " - " + p.getDesignation());
        if (req.stockInitial() != null && req.stockInitial() > 0) {
            // Le stock de départ passe lui aussi par un mouvement : il reste visible dans l'historique
            stockService.enregistrer(p.getId(), MotifMouvement.STOCK_INITIAL, req.stockInitial(),
                    null, "Stock à la création du produit");
        }
        return ProduitDto.from(p); // même instance que celle mise à jour par StockService
    }

    @Transactional
    public ProduitDto modifier(Long id, ProduitRequest req) {
        Produit p = trouver(id);
      //  if (!p.getCode().equals(req.code()) && produitRepository.existsByCode(req.code())) {
      //      throw new BusinessException("Le code produit " + req.code() + " existe déjà");
      //  }
        String avant = "prixVente=" + p.getPrixVente() + ", prixAchat=" + p.getPrixAchat() + ", actif=" + p.isActif();
        appliquer(p, req);
        auditService.log("MODIFICATION_PRODUIT", "Produit", id, avant + " -> prixVente=" + p.getPrixVente()
                + ", prixAchat=" + p.getPrixAchat() + ", actif=" + p.isActif());
        return ProduitDto.from(p);
    }

    private void appliquer(Produit p, ProduitRequest req) {
     //   p.setCode(req.code().trim());
        p.setCodeBarre(req.codeBarre() == null || req.codeBarre().isBlank() ? null : req.codeBarre().trim());
        p.setDesignation(req.designation().trim());
        p.setCategorie(req.categorieId() == null ? null : categorieRepository.findById(req.categorieId())
                .orElseThrow(() -> new NotFoundException("Catégorie introuvable")));
        p.setUnite(req.unite());
        p.setPrixAchat(req.prixAchat());
        p.setPrixVente(req.prixVente());
        if (req.tauxTva() != null) {
            p.setTauxTva(req.tauxTva());
        }
        p.setSeuilAlerte(req.seuilAlerte());
        p.setEmplacement(req.emplacement());
        p.setActif(req.actif());
    }

    /**
     * Suppression définitive, seulement pour un produit jamais vendu (ex. créé par erreur).
     * Un produit déjà vendu figure sur des factures : il faut l'archiver.
     */
    @Transactional
    public void supprimer(Long id) {
        Produit p = trouver(id);
        if (produitRepository.aEteVendu(id)) {
            throw new BusinessException("« " + p.getDesignation()
                    + " » figure sur des factures : il ne peut pas être supprimé. Archivez-le plutôt.");
        }
        mouvementRepository.deleteByProduitId(id); // ses entrées/sorties de stock disparaissent avec lui
        produitRepository.delete(p);
        auditService.log("SUPPRESSION_PRODUIT", "Produit", id,
                p.getCode() + " - " + p.getDesignation() + " (stock au moment de la suppression : " + p.getStockActuel() + ")");
    }

    private Produit trouver(Long id) {
        return produitRepository.findById(id).orElseThrow(() -> new NotFoundException("Produit introuvable"));
    }
}
