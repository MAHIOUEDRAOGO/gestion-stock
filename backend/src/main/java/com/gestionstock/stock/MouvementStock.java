package com.gestionstock.stock;

import java.time.LocalDateTime;

import com.gestionstock.auth.Utilisateur;
import com.gestionstock.produit.Produit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mouvement_stock")
@Getter
@Setter
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotifMouvement motif;

    @Column(nullable = false)
    private int quantite;

    @Column(name = "stock_apres", nullable = false)
    private int stockApres;

    private String reference;

    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement = LocalDateTime.now();
}
