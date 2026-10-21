package com.gestionstock.vente;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.gestionstock.audit.AuditService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clients")
@PreAuthorize("hasAnyRole('CAISSIERE','DIRECTION','ADMIN')")
@RequiredArgsConstructor
public class ClientController {

    public record ClientRequest(@NotBlank String nom, String telephone, String adresse) {}

    private final ClientRepository repository;
    private final AuditService auditService;

    @GetMapping
    public List<Client> rechercher(@RequestParam(defaultValue = "") String nom) {
        return repository.findByNomContainingIgnoreCaseOrderByNom(nom);
    }

    @PostMapping
    @Transactional
    public Client creer(@Valid @RequestBody ClientRequest req) {
        Client c = new Client();
        c.setNom(req.nom().trim());
        c.setTelephone(req.telephone());
        c.setAdresse(req.adresse());
        repository.save(c);
        auditService.log("CREATION_CLIENT", "Client", c.getId(), c.getNom());
        return c;
    }
}
