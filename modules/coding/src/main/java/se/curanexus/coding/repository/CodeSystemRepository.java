package se.curanexus.coding.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.coding.domain.CodeSystem;
import se.curanexus.coding.domain.CodeSystemType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodeSystemRepository extends JpaRepository<CodeSystem, UUID> {

    Optional<CodeSystem> findByType(CodeSystemType type);

    List<CodeSystem> findByActiveTrue();

    Optional<CodeSystem> findByTypeAndActiveTrue(CodeSystemType type);
}
