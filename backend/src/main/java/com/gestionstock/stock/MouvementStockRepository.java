package com.gestionstock.stock;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    @EntityGraph(attributePaths = {"produit", "utilisateur"})
    @Query("""
            SELECT m FROM MouvementStock m
            WHERE (:produitId IS NULL OR m.produit.id = :produitId)
              AND (:type IS NULL OR m.type = :type)
              AND (:motif IS NULL OR m.motif = :motif)
              AND m.dateMouvement >= :debut AND m.dateMouvement < :fin
            ORDER BY m.dateMouvement DESC
            """)
    Page<MouvementStock> rechercher(@Param("produitId") Long produitId,
                                    @Param("type") TypeMouvement type,
                                    @Param("motif") MotifMouvement motif,
                                    @Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin,
                                    Pageable pageable);
    /** Spring Data écrit la requête tout seul à partir du nom de la méthode. */
    void deleteByProduitId(Long produitId);
}
