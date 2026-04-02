package se.curanexus.journal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.journal.domain.Diagnosis;
import se.curanexus.journal.domain.DiagnosisType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID> {

    List<Diagnosis> findByEncounterIdOrderByRankAsc(UUID encounterId);

    List<Diagnosis> findByPatientIdOrderByRecordedAtDesc(UUID patientId);

    List<Diagnosis> findByEncounterIdAndType(UUID encounterId, DiagnosisType type);

    List<Diagnosis> findByPatientIdAndCode(UUID patientId, String code);

    @Query("SELECT d FROM Diagnosis d WHERE d.encounterId = :encounterId AND d.type = 'PRINCIPAL'")
    Optional<Diagnosis> findPrincipalDiagnosis(@Param("encounterId") UUID encounterId);

    @Query("SELECT d FROM Diagnosis d WHERE d.patientId = :patientId AND d.resolvedDate IS NULL ORDER BY d.recordedAt DESC")
    List<Diagnosis> findActiveByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT d FROM Diagnosis d WHERE d.patientId = :patientId AND d.code LIKE :codePrefix% ORDER BY d.recordedAt DESC")
    List<Diagnosis> findByPatientAndCodePrefix(@Param("patientId") UUID patientId, @Param("codePrefix") String codePrefix);

    @Query("SELECT DISTINCT d.code FROM Diagnosis d WHERE d.patientId = :patientId")
    List<String> findDistinctCodesByPatient(@Param("patientId") UUID patientId);
}
