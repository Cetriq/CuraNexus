package se.curanexus.triage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.triage.domain.TriageProtocol;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TriageProtocolRepository extends JpaRepository<TriageProtocol, UUID> {

    Optional<TriageProtocol> findByCode(String code);

    List<TriageProtocol> findByActiveTrue();

    List<TriageProtocol> findByCategory(String category);

    @Query("SELECT tp FROM TriageProtocol tp WHERE tp.active = true " +
           "AND (:category IS NULL OR tp.category = :category)")
    List<TriageProtocol> findActiveByCategory(@Param("category") String category);

    @Query("SELECT DISTINCT tp.category FROM TriageProtocol tp WHERE tp.active = true ORDER BY tp.category")
    List<String> findAllActiveCategories();
}
