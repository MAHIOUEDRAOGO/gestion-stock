package com.gestionstock.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Crée un compte administrateur au premier démarrage si la base est vide. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.login}")
    private String login;

    @Value("${app.admin.password}")
    private String password;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            Utilisateur admin = new Utilisateur();
            admin.setNomComplet("Administrateur");
            admin.setLogin(login);
            admin.setMotDePasse(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            repository.save(admin);
            log.warn("Compte administrateur '{}' créé. Changez son mot de passe dès la première connexion !", login);
        }
    }
}
