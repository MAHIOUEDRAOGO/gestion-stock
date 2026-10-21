package com.gestionstock.vente;

import java.math.BigDecimal;

import com.gestionstock.produit.Produit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ligne_facture")
@Getter
@Setter
public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    /** Prix HT figé au moment de la vente (le prix du produit peut changer ensuite). */
    @Column(name = "prix_unitaire", nullable = false)
    private BigDecimal prixUnitaire;

    @Column(name = "taux_tva", nullable = false)
    private BigDecimal tauxTva;

    @Column(nullable = false)
    private BigDecimal remise = BigDecimal.ZERO;

    /** Total HT de la ligne, remise déduite. */
    @Column(name = "total_ligne", nullable = false)
    private BigDecimal totalLigne;
}
