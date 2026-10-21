package com.gestionstock.audit;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Trace d'une action utilisateur. Immuable : jamais modifiée ni supprimée. */
@Entity
@Table(name = "journal_audit")
@Immutable
@Getter
@Setter
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();

    @Column(name = "utilisateur_login")
    private String utilisateurLogin;

    @Column(nullable = false)
    private String action;

    private String entite;

    @Column(name = "entite_id")
    private Long entiteId;

    private String details;

    @Column(name = "adresse_ip")
    private String adresseIp;
}
