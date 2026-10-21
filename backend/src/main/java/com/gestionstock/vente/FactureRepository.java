package com.gestionstock.vente;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    @EntityGraph(attributePaths = {"client", "utilisateur"})
    @Query("""
            SELECT f FROM Facture f
            WHERE f.dateFacture >= :debut AND f.dateFacture < :fin
            ORDER BY f.dateFacture DESC
            """)
    List<Facture> rechercher(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @EntityGraph(attributePaths = {"client", "utilisateur", "lignes", "lignes.produit"})
    @Query("SELECT f FROM Facture f WHERE f.id = :id")
    Optional<Facture> findDetail(@Param("id") Long id);
}
