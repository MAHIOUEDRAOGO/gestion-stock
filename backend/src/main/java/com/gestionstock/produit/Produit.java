package com.gestionstock.produit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "produit")
@Getter
@Setter
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "code_barre", unique = true)
    private String codeBarre;

    @Column(nullable = false)
    private String designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(nullable = false)
    private String unite = "pièce";

    @Column(name = "prix_achat", nullable = false)
    private BigDecimal prixAchat = BigDecimal.ZERO;

    @Column(name = "prix_vente", nullable = false)
    private BigDecimal prixVente = BigDecimal.ZERO;

    @Column(name = "taux_tva", nullable = false)
    private BigDecimal tauxTva = BigDecimal.ZERO;

    @Column(name = "seuil_alerte", nullable = false)
    private int seuilAlerte;

    /** Mis à jour uniquement par StockService, jamais directement. */
    @Column(name = "stock_actuel", nullable = false)
    private int stockActuel;

    private String emplacement;

    private boolean actif = true;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public boolean isEnAlerte() {
        return stockActuel <= seuilAlerte;
    }
}
