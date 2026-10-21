package com.gestionstock.vente;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.gestionstock.vente.VenteDtos.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('CAISSIERE','ADMIN')")
    public FactureDto creer(@Valid @RequestBody FactureRequest req) {
        return service.creer(req);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CAISSIERE','DIRECTION','ADMIN')")
    public List<FactureDto> lister(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return service.rechercher(du, au);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAISSIERE','DIRECTION','ADMIN')")
    public FactureDto obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    // TODO Sprint 2 : GET /{id}/pdf (génération PDF avec OpenPDF ou JasperReports)

    /** L'annulation est réservée à la direction : une caissière ne peut pas annuler seule. */
    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('DIRECTION','ADMIN')")
    public FactureDto annuler(@PathVariable Long id, @Valid @RequestBody AnnulationRequest req) {
        return service.annuler(id, req.motif());
    }
}
