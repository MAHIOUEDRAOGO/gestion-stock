package com.gestionstock.produit;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.gestionstock.audit.AuditService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    public record CategorieRequest(@NotBlank String nom) {}

    private final CategorieRepository repository;
    private final AuditService auditService;

    @GetMapping
    public List<Categorie> lister() {
        return repository.findAll(Sort.by("nom"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACHATS')")
    @Transactional
    public Categorie creer(@Valid @RequestBody CategorieRequest req) {
        Categorie c = new Categorie();
        c.setNom(req.nom().trim());
        repository.save(c);
        auditService.log("CREATION_CATEGORIE", "Categorie", c.getId(), c.getNom());
        return c;
    }
}
