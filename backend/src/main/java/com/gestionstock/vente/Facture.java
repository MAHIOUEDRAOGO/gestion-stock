package com.gestionstock.vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gestionstock.auth.Utilisateur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "facture")
@Getter
@Setter
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "date_facture", nullable = false)
    private LocalDateTime dateFacture = LocalDateTime.now();

    @Column(name = "total_ht", nullable = false)
    private BigDecimal totalHt = BigDecimal.ZERO;

    @Column(name = "total_tva", nullable = false)
    private BigDecimal totalTva = BigDecimal.ZERO;

    @Column(name = "total_ttc", nullable = false)
    private BigDecimal totalTtc = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false)
    private ModePaiement modePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut = StatutFacture.VALIDEE;

    @Column(name = "motif_annulation")
    private String motifAnnulation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();

    public void ajouterLigne(LigneFacture ligne) {
        ligne.setFacture(this);
        lignes.add(ligne);
    }
}
