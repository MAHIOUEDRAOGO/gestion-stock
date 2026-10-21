package com.gestionstock.auth;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.gestionstock.audit.AuditService;
import com.gestionstock.auth.AuthDtos.*;
import com.gestionstock.common.BusinessException;
import com.gestionstock.common.NotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Gestion des comptes utilisateurs (administrateur uniquement). */
@RestController
@RequestMapping("/api/utilisateurs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @GetMapping
    public List<UtilisateurDto> lister() {
        return repository.findAll().stream().map(UtilisateurDto::from).toList();
    }

    @PostMapping
    @Transactional
    public UtilisateurDto creer(@Valid @RequestBody CreerUtilisateurRequest req) {
        if (repository.existsByLogin(req.login())) {
            throw new BusinessException("Ce login est déjà utilisé");
        }
        Utilisateur u = new Utilisateur();
        u.setNomComplet(req.nomComplet());
        u.setLogin(req.login());
        u.setMotDePasse(passwordEncoder.encode(req.motDePasse()));
        u.setRole(req.role());
        repository.save(u);
        auditService.log("CREATION_UTILISATEUR", "Utilisateur", u.getId(), u.getLogin() + " (" + u.getRole() + ")");
        return UtilisateurDto.from(u);
    }

    @PutMapping("/{id}")
    @Transactional
    public UtilisateurDto modifier(@PathVariable Long id, @Valid @RequestBody ModifierUtilisateurRequest req) {
        Utilisateur u = repository.findById(id).orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
        String avant = u.getRole() + ", actif=" + u.isActif();
        u.setNomComplet(req.nomComplet());
        u.setRole(req.role());
        u.setActif(req.actif());
        auditService.log("MODIFICATION_UTILISATEUR", "Utilisateur", id,
                avant + " -> " + u.getRole() + ", actif=" + u.isActif());
        return UtilisateurDto.from(u);
    }

    @PostMapping("/{id}/reinitialiser-mot-de-passe")
    @Transactional
    public void reinitialiser(@PathVariable Long id, @RequestBody String nouveauMotDePasse) {
        if (nouveauMotDePasse == null || nouveauMotDePasse.length() < 6) {
            throw new BusinessException("Le mot de passe doit contenir au moins 6 caractères");
        }
        Utilisateur u = repository.findById(id).orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
        u.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        auditService.log("REINITIALISATION_MOT_DE_PASSE", "Utilisateur", id, null);
    }
}
