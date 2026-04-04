package se.curanexus.consent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.consent.domain.Consent;
import se.curanexus.consent.domain.ConsentStatus;
import se.curanexus.consent.domain.ConsentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {

    List<Consent> findByPatientId(UUID patientId);

    List<Consent> findByPatientIdAndStatus(UUID patientId, ConsentStatus status);

    List<Consent> findByPatientIdAndType(UUID patientId, ConsentType type);

    List<Consent> findByPatientIdAndTypeAndStatus(UUID patientId, ConsentType type, ConsentStatus status);

    Optional<Consent> findByPatientIdAndTypeAndStatusIn(UUID patientId, ConsentType type, List<ConsentStatus> statuses);

    List<Consent> findByManagingUnitId(UUID managingUnitId);

    List<Consent> findByManagingUnitIdAndStatus(UUID managingUnitId, ConsentStatus status);

    @Query("SELECT c FROM Consent c WHERE c.patientId = :patientId AND c.status = 'ACTIVE' " +
           "AND (c.validFrom IS NULL OR c.validFrom <= CURRENT_DATE) " +
           "AND (c.validUntil IS NULL OR c.validUntil >= CURRENT_DATE)")
    List<Consent> findActiveConsentsForPatient(@Param("patientId") UUID patientId);

    @Query("SELECT c FROM Consent c WHERE c.patientId = :patientId AND c.type = :type " +
           "AND c.status = 'ACTIVE' " +
           "AND (c.validFrom IS NULL OR c.validFrom <= CURRENT_DATE) " +
           "AND (c.validUntil IS NULL OR c.validUntil >= CURRENT_DATE)")
    Optional<Consent> findActiveConsentForPatientAndType(
            @Param("patientId") UUID patientId,
            @Param("type") ConsentType type);

    @Query("SELECT c FROM Consent c WHERE c.status = 'ACTIVE' " +
           "AND c.validUntil IS NOT NULL AND c.validUntil < CURRENT_DATE")
    List<Consent> findExpiredConsents();

    @Query("SELECT COUNT(c) FROM Consent c WHERE c.patientId = :patientId AND c.status = :status")
    long countByPatientIdAndStatus(@Param("patientId") UUID patientId, @Param("status") ConsentStatus status);
}
