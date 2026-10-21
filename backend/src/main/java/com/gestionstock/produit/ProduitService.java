package com.gestionstock.produit;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionstock.audit.AuditService;
import com.gestionstock.common.BusinessException;
import com.gestionstock.common.NotFoundException;
import com.gestionstock.produit.ProduitDtos.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final AuditService auditService;

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
        if (produitRepository.existsByCode(req.code())) {
            throw new BusinessException("Le code produit " + req.code() + " existe déjà");
        }
        Produit p = new Produit();
        appliquer(p, req);
        produitRepository.save(p);
        auditService.log("CREATION_PRODUIT", "Produit", p.getId(), p.getCode() + " - " + p.getDesignation());
        return ProduitDto.from(p);
    }

    @Transactional
    public ProduitDto modifier(Long id, ProduitRequest req) {
        Produit p = trouver(id);
        if (!p.getCode().equals(req.code()) && produitRepository.existsByCode(req.code())) {
            throw new BusinessException("Le code produit " + req.code() + " existe déjà");
        }
        String avant = "prixVente=" + p.getPrixVente() + ", prixAchat=" + p.getPrixAchat() + ", actif=" + p.isActif();
        appliquer(p, req);
        auditService.log("MODIFICATION_PRODUIT", "Produit", id, avant + " -> prixVente=" + p.getPrixVente()
                + ", prixAchat=" + p.getPrixAchat() + ", actif=" + p.isActif());
        return ProduitDto.from(p);
    }

    private void appliquer(Produit p, ProduitRequest req) {
        p.setCode(req.code().trim());
        p.setCodeBarre(req.codeBarre() == null || req.codeBarre().isBlank() ? null : req.codeBarre().trim());
        p.setDesignation(req.designation().trim());
        p.setCategorie(req.categorieId() == null ? null : categorieRepository.findById(req.categorieId())
                .orElseThrow(() -> new NotFoundException("Catégorie introuvable")));
        p.setUnite(req.unite());
        p.setPrixAchat(req.prixAchat());
        p.setPrixVente(req.prixVente());
        p.setTauxTva(req.tauxTva());
        p.setSeuilAlerte(req.seuilAlerte());
        p.setEmplacement(req.emplacement());
        p.setActif(req.actif());
    }

    private Produit trouver(Long id) {
        return produitRepository.findById(id).orElseThrow(() -> new NotFoundException("Produit introuvable"));
    }
}
