package se.curanexus.authorization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.authorization.domain.CareRelation;
import se.curanexus.authorization.domain.CareRelationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CareRelationRepository extends JpaRepository<CareRelation, UUID> {

    List<CareRelation> findByUserId(UUID userId);

    List<CareRelation> findByPatientId(UUID patientId);

    List<CareRelation> findByEncounterId(UUID encounterId);

    List<CareRelation> findByUserIdAndActiveTrue(UUID userId);

    List<CareRelation> findByPatientIdAndActiveTrue(UUID patientId);

    List<CareRelation> findByRelationType(CareRelationType relationType);

    @Query("SELECT cr FROM CareRelation cr WHERE cr.userId = :userId AND cr.patientId = :patientId AND cr.active = true")
    List<CareRelation> findActiveByUserAndPatient(
            @Param("userId") UUID userId,
            @Param("patientId") UUID patientId);

    @Query("SELECT cr FROM CareRelation cr WHERE cr.userId = :userId AND cr.patientId = :patientId AND cr.encounterId = :encounterId AND cr.active = true")
    Optional<CareRelation> findActiveByUserPatientAndEncounter(
            @Param("userId") UUID userId,
            @Param("patientId") UUID patientId,
            @Param("encounterId") UUID encounterId);

    @Query("SELECT cr FROM CareRelation cr WHERE cr.userId = :userId AND cr.active = true AND cr.validFrom <= :now AND (cr.validUntil IS NULL OR cr.validUntil >= :now)")
    List<CareRelation> findCurrentlyValidByUser(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now);

    @Query("SELECT cr FROM CareRelation cr WHERE cr.patientId = :patientId AND cr.active = true AND cr.validFrom <= :now AND (cr.validUntil IS NULL OR cr.validUntil >= :now)")
    List<CareRelation> findCurrentlyValidByPatient(
            @Param("patientId") UUID patientId,
            @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM CareRelation cr WHERE cr.userId = :userId AND cr.patientId = :patientId AND cr.active = true AND cr.validFrom <= :now AND (cr.validUntil IS NULL OR cr.validUntil >= :now)")
    boolean hasActiveCareRelation(
            @Param("userId") UUID userId,
            @Param("patientId") UUID patientId,
            @Param("now") LocalDateTime now);

    @Query("SELECT cr FROM CareRelation cr WHERE cr.userId = :userId AND cr.relationType = :relationType AND cr.active = true")
    List<CareRelation> findActiveByUserAndType(
            @Param("userId") UUID userId,
            @Param("relationType") CareRelationType relationType);

    @Query("SELECT cr FROM CareRelation cr WHERE cr.patientId = :patientId AND cr.relationType = :relationType AND cr.active = true")
    List<CareRelation> findActiveByPatientAndType(
            @Param("patientId") UUID patientId,
            @Param("relationType") CareRelationType relationType);

    @Query("SELECT cr FROM CareRelation cr WHERE cr.active = true AND cr.validUntil IS NOT NULL AND cr.validUntil < :now")
    List<CareRelation> findExpiredRelations(@Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT cr.patientId FROM CareRelation cr WHERE cr.userId = :userId AND cr.active = true")
    List<UUID> findPatientIdsByUser(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT cr.userId FROM CareRelation cr WHERE cr.patientId = :patientId AND cr.active = true")
    List<UUID> findUserIdsByPatient(@Param("patientId") UUID patientId);
}
