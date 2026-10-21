package com.gestionstock.audit;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

/** Consultation du journal d'activité (direction et administrateur). */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAnyRole('DIRECTION','ADMIN')")
@RequiredArgsConstructor
public class AuditController {

    private final JournalAuditRepository repository;

    @GetMapping
    public Page<JournalAudit> rechercher(
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int taille) {
        LocalDate debut = du != null ? du : LocalDate.now().minusDays(30);
        LocalDate fin = au != null ? au : LocalDate.now();
        return repository.rechercher(vide(login), vide(action),
                debut.atStartOfDay(), fin.plusDays(1).atStartOfDay(),
                PageRequest.of(page, Math.min(taille, 200)));
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
