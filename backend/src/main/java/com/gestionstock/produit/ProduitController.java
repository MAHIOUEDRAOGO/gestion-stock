package com.gestionstock.produit;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.gestionstock.produit.ProduitDtos.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService service;

    /** Lecture ouverte à tous les rôles connectés. */
    @GetMapping
    public List<ProduitDto> lister(@RequestParam(required = false) String recherche,
                                   @RequestParam(defaultValue = "true") boolean actifsSeulement) {
        return service.rechercher(recherche, actifsSeulement);
    }

    @GetMapping("/{id}")
    public ProduitDto obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACHATS')")
    public ProduitDto creer(@Valid @RequestBody ProduitRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACHATS')")
    public ProduitDto modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequest req) {
        return service.modifier(id, req);
    }
}
