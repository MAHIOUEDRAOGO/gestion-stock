package com.gestionstock.audit;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    @Query("""
            SELECT j FROM JournalAudit j
            WHERE (:login IS NULL OR j.utilisateurLogin = :login)
              AND (:action IS NULL OR j.action = :action)
              AND j.dateAction >= :debut AND j.dateAction < :fin
            ORDER BY j.dateAction DESC
            """)
    Page<JournalAudit> rechercher(@Param("login") String login,
                                  @Param("action") String action,
                                  @Param("debut") LocalDateTime debut,
                                  @Param("fin") LocalDateTime fin,
                                  Pageable pageable);
}
