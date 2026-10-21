package com.gestionstock.produit;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    boolean existsByCode(String code);

    Optional<Produit> findByCodeBarre(String codeBarre);

    @Query("""
            SELECT p FROM Produit p LEFT JOIN FETCH p.categorie
            WHERE (:actifsSeulement = FALSE OR p.actif = TRUE)
              AND (:recherche IS NULL
                   OR LOWER(p.designation) LIKE LOWER(CONCAT('%', :recherche, '%'))
                   OR LOWER(p.code) LIKE LOWER(CONCAT('%', :recherche, '%'))
                   OR p.codeBarre = :recherche)
            ORDER BY p.designation
            """)
    List<Produit> rechercher(@Param("recherche") String recherche, @Param("actifsSeulement") boolean actifsSeulement);

    @Query("SELECT p FROM Produit p LEFT JOIN FETCH p.categorie WHERE p.actif = TRUE AND p.stockActuel <= p.seuilAlerte ORDER BY p.stockActuel")
    List<Produit> findEnAlerte();

    /** Verrouille la ligne pendant la transaction : évite que deux postes vendent le même dernier article. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Produit p WHERE p.id = :id")
    Optional<Produit> findByIdPourMiseAJour(@Param("id") Long id);
}
