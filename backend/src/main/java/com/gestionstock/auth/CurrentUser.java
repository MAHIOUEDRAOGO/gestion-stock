package com.gestionstock.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gestionstock.common.NotFoundException;

import lombok.RequiredArgsConstructor;

/** Accès à l'utilisateur connecté depuis les services. */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UtilisateurRepository utilisateurRepository;

    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public Utilisateur get() {
        return utilisateurRepository.findByLogin(login())
                .orElseThrow(() -> new NotFoundException("Utilisateur connecté introuvable"));
    }
}
