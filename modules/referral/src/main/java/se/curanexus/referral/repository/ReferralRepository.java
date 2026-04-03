package se.curanexus.referral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.referral.domain.Referral;
import se.curanexus.referral.domain.ReferralPriority;
import se.curanexus.referral.domain.ReferralStatus;
import se.curanexus.referral.domain.ReferralType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, UUID> {

    Optional<Referral> findByReferralReference(String referralReference);

    List<Referral> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<Referral> findByPatientIdAndStatusIn(UUID patientId, List<ReferralStatus> statuses);

    // Skickade remisser (avsändarperspektiv)
    List<Referral> findBySenderUnitIdOrderByCreatedAtDesc(UUID senderUnitId);

    List<Referral> findBySenderPractitionerIdOrderByCreatedAtDesc(UUID senderPractitionerId);

    @Query("SELECT r FROM Referral r WHERE r.senderUnitId = :unitId " +
            "AND r.status IN :statuses ORDER BY r.createdAt DESC")
    List<Referral> findBySenderUnitAndStatus(@Param("unitId") UUID unitId,
                                              @Param("statuses") List<ReferralStatus> statuses);

    // Mottagna remisser (mottagarperspektiv)
    List<Referral> findByReceiverUnitIdOrderByReceivedAtDesc(UUID receiverUnitId);

    @Query("SELECT r FROM Referral r WHERE r.receiverUnitId = :unitId " +
            "AND r.status IN :statuses ORDER BY r.receivedAt DESC")
    List<Referral> findByReceiverUnitAndStatus(@Param("unitId") UUID unitId,
                                                @Param("statuses") List<ReferralStatus> statuses);

    // Remisser som behöver bedömning
    @Query("SELECT r FROM Referral r WHERE r.receiverUnitId = :unitId " +
            "AND r.status IN ('RECEIVED', 'UNDER_ASSESSMENT') " +
            "ORDER BY " +
            "CASE r.priority " +
            "  WHEN 'IMMEDIATE' THEN 1 " +
            "  WHEN 'URGENT' THEN 2 " +
            "  WHEN 'SEMI_URGENT' THEN 3 " +
            "  WHEN 'ROUTINE' THEN 4 " +
            "  ELSE 5 " +
            "END, r.receivedAt")
    List<Referral> findPendingAssessmentByUnit(@Param("unitId") UUID unitId);

    // Remisser väntande på komplettering
    @Query("SELECT r FROM Referral r WHERE r.senderUnitId = :unitId " +
            "AND r.status = 'PENDING_INFORMATION' " +
            "ORDER BY r.updatedAt DESC")
    List<Referral> findPendingInformationBySenderUnit(@Param("unitId") UUID unitId);

    // Accepterade remisser (kommande besök)
    @Query("SELECT r FROM Referral r WHERE r.receiverUnitId = :unitId " +
            "AND r.status = 'ACCEPTED' " +
            "ORDER BY r.requestedDate")
    List<Referral> findAcceptedByReceiverUnit(@Param("unitId") UUID unitId);

    // Sök remisser
    @Query("SELECT r FROM Referral r " +
            "WHERE (:patientId IS NULL OR r.patientId = :patientId) " +
            "AND (:senderUnitId IS NULL OR r.senderUnitId = :senderUnitId) " +
            "AND (:receiverUnitId IS NULL OR r.receiverUnitId = :receiverUnitId) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:referralType IS NULL OR r.referralType = :referralType) " +
            "AND (:priority IS NULL OR r.priority = :priority) " +
            "AND (:fromDate IS NULL OR r.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR r.createdAt <= :toDate)")
    Page<Referral> search(@Param("patientId") UUID patientId,
                           @Param("senderUnitId") UUID senderUnitId,
                           @Param("receiverUnitId") UUID receiverUnitId,
                           @Param("status") ReferralStatus status,
                           @Param("referralType") ReferralType referralType,
                           @Param("priority") ReferralPriority priority,
                           @Param("fromDate") Instant fromDate,
                           @Param("toDate") Instant toDate,
                           Pageable pageable);

    // Statistik
    @Query("SELECT COUNT(r) FROM Referral r WHERE r.receiverUnitId = :unitId " +
            "AND r.status IN ('RECEIVED', 'UNDER_ASSESSMENT')")
    long countPendingByReceiverUnit(@Param("unitId") UUID unitId);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.senderUnitId = :unitId " +
            "AND r.status = 'PENDING_INFORMATION'")
    long countPendingInfoBySenderUnit(@Param("unitId") UUID unitId);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.receiverUnitId = :unitId " +
            "AND r.status = 'ACCEPTED'")
    long countAcceptedByReceiverUnit(@Param("unitId") UUID unitId);

    // Utgångna remisser
    @Query("SELECT r FROM Referral r WHERE r.status IN ('SENT', 'FORWARDED') " +
            "AND r.validUntil IS NOT NULL AND r.validUntil < :date")
    List<Referral> findExpiredReferrals(@Param("date") LocalDate date);

    // Koppling till vårdkontakt
    Optional<Referral> findBySourceEncounterId(UUID sourceEncounterId);

    List<Referral> findByResultingEncounterId(UUID resultingEncounterId);
}
