package se.curanexus.consent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.consent.domain.AccessBlock;
import se.curanexus.consent.domain.AccessBlockType;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccessBlockRepository extends JpaRepository<AccessBlock, UUID> {

    List<AccessBlock> findByPatientId(UUID patientId);

    List<AccessBlock> findByPatientIdAndActive(UUID patientId, boolean active);

    List<AccessBlock> findByPatientIdAndBlockType(UUID patientId, AccessBlockType blockType);

    List<AccessBlock> findByPatientIdAndBlockTypeAndActive(UUID patientId, AccessBlockType blockType, boolean active);

    List<AccessBlock> findByBlockedUnitId(UUID blockedUnitId);

    List<AccessBlock> findByBlockedUnitIdAndActive(UUID blockedUnitId, boolean active);

    List<AccessBlock> findByBlockedPractitionerId(UUID blockedPractitionerId);

    List<AccessBlock> findByBlockedPractitionerIdAndActive(UUID blockedPractitionerId, boolean active);

    @Query("SELECT ab FROM AccessBlock ab WHERE ab.patientId = :patientId AND ab.active = true " +
           "AND (ab.validFrom IS NULL OR ab.validFrom <= CURRENT_DATE) " +
           "AND (ab.validUntil IS NULL OR ab.validUntil >= CURRENT_DATE)")
    List<AccessBlock> findActiveBlocksForPatient(@Param("patientId") UUID patientId);

    @Query("SELECT ab FROM AccessBlock ab WHERE ab.patientId = :patientId " +
           "AND ab.blockedUnitId = :unitId AND ab.active = true " +
           "AND (ab.validFrom IS NULL OR ab.validFrom <= CURRENT_DATE) " +
           "AND (ab.validUntil IS NULL OR ab.validUntil >= CURRENT_DATE)")
    List<AccessBlock> findActiveBlocksForPatientAndUnit(
            @Param("patientId") UUID patientId,
            @Param("unitId") UUID unitId);

    @Query("SELECT ab FROM AccessBlock ab WHERE ab.patientId = :patientId " +
           "AND ab.blockedPractitionerId = :practitionerId AND ab.active = true " +
           "AND (ab.validFrom IS NULL OR ab.validFrom <= CURRENT_DATE) " +
           "AND (ab.validUntil IS NULL OR ab.validUntil >= CURRENT_DATE)")
    List<AccessBlock> findActiveBlocksForPatientAndPractitioner(
            @Param("patientId") UUID patientId,
            @Param("practitionerId") UUID practitionerId);

    @Query("SELECT CASE WHEN COUNT(ab) > 0 THEN true ELSE false END FROM AccessBlock ab " +
           "WHERE ab.patientId = :patientId AND ab.blockedUnitId = :unitId AND ab.active = true " +
           "AND (ab.validFrom IS NULL OR ab.validFrom <= CURRENT_DATE) " +
           "AND (ab.validUntil IS NULL OR ab.validUntil >= CURRENT_DATE)")
    boolean isUnitBlockedForPatient(@Param("patientId") UUID patientId, @Param("unitId") UUID unitId);

    @Query("SELECT CASE WHEN COUNT(ab) > 0 THEN true ELSE false END FROM AccessBlock ab " +
           "WHERE ab.patientId = :patientId AND ab.blockedPractitionerId = :practitionerId AND ab.active = true " +
           "AND (ab.validFrom IS NULL OR ab.validFrom <= CURRENT_DATE) " +
           "AND (ab.validUntil IS NULL OR ab.validUntil >= CURRENT_DATE)")
    boolean isPractitionerBlockedForPatient(
            @Param("patientId") UUID patientId,
            @Param("practitionerId") UUID practitionerId);

    @Query("SELECT COUNT(ab) FROM AccessBlock ab WHERE ab.patientId = :patientId AND ab.active = true")
    long countActiveBlocksForPatient(@Param("patientId") UUID patientId);
}
