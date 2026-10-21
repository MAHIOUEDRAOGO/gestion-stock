package com.gestionstock.audit;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.gestionstock.auth.CurrentUser;

import lombok.RequiredArgsConstructor;

/**
 * Enregistre les actions des utilisateurs dans le journal d'audit.
 * Appelé dans la même transaction que l'action : si l'action échoue, la trace est annulée aussi.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final JournalAuditRepository repository;
    private final CurrentUser currentUser;

    public void log(String action, String entite, Long entiteId, String details) {
        log(currentUser.login(), action, entite, entiteId, details);
    }

    public void log(String login, String action, String entite, Long entiteId, String details) {
        JournalAudit j = new JournalAudit();
        j.setUtilisateurLogin(login);
        j.setAction(action);
        j.setEntite(entite);
        j.setEntiteId(entiteId);
        j.setDetails(details);
        j.setAdresseIp(adresseIp());
        repository.save(j);
    }

    private String adresseIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest().getRemoteAddr();
        }
        return null;
    }
}
