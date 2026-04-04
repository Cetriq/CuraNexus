package se.curanexus.certificates.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.certificates.domain.Certificate;
import se.curanexus.certificates.domain.CertificateStatus;
import se.curanexus.certificates.domain.CertificateType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    List<Certificate> findByPatientId(UUID patientId);

    Page<Certificate> findByPatientId(UUID patientId, Pageable pageable);

    List<Certificate> findByPatientIdAndStatus(UUID patientId, CertificateStatus status);

    List<Certificate> findByEncounterId(UUID encounterId);

    List<Certificate> findByIssuerId(UUID issuerId);

    Page<Certificate> findByIssuerId(UUID issuerId, Pageable pageable);

    @Query("""
        SELECT c FROM Certificate c
        WHERE c.patientId = :patientId
        AND c.template.type = :type
        ORDER BY c.createdAt DESC
        """)
    List<Certificate> findByPatientIdAndType(
            @Param("patientId") UUID patientId,
            @Param("type") CertificateType type);

    @Query("""
        SELECT c FROM Certificate c
        WHERE c.status = 'SIGNED'
        AND c.template.recipientSystem IS NOT NULL
        ORDER BY c.signedAt ASC
        """)
    List<Certificate> findPendingSend();

    @Query("""
        SELECT c FROM Certificate c
        WHERE c.status IN ('SIGNED', 'SENT')
        AND c.validUntil IS NOT NULL
        AND c.validUntil < :date
        """)
    List<Certificate> findExpiredCertificates(@Param("date") LocalDate date);

    @Query("""
        SELECT c FROM Certificate c
        WHERE c.status = 'DRAFT'
        AND c.issuerId = :issuerId
        ORDER BY c.updatedAt DESC
        """)
    List<Certificate> findDraftsByIssuer(@Param("issuerId") UUID issuerId);

    @Query("""
        SELECT c FROM Certificate c
        WHERE c.patientId = :patientId
        AND c.template.type = 'SICK_LEAVE'
        AND c.status IN ('SIGNED', 'SENT')
        AND (c.periodEnd >= :date OR c.periodEnd IS NULL)
        ORDER BY c.periodStart DESC
        """)
    List<Certificate> findActiveSickLeaves(
            @Param("patientId") UUID patientId,
            @Param("date") LocalDate date);
}
