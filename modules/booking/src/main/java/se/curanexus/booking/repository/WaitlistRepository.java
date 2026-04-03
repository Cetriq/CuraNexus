package se.curanexus.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.booking.domain.WaitlistEntry;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistPriority;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, UUID> {

    List<WaitlistEntry> findByPatientIdAndStatusIn(UUID patientId, List<WaitlistStatus> statuses);

    List<WaitlistEntry> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    @Query("SELECT w FROM WaitlistEntry w WHERE w.practitionerId = :practitionerId " +
            "AND w.status = 'WAITING' " +
            "ORDER BY w.priority DESC, w.createdAt ASC")
    List<WaitlistEntry> findWaitingByPractitioner(@Param("practitionerId") UUID practitionerId);

    @Query("SELECT w FROM WaitlistEntry w WHERE w.unitId = :unitId " +
            "AND w.status = 'WAITING' " +
            "ORDER BY w.priority DESC, w.createdAt ASC")
    List<WaitlistEntry> findWaitingByUnit(@Param("unitId") UUID unitId);

    @Query("SELECT w FROM WaitlistEntry w WHERE w.serviceType = :serviceType " +
            "AND w.status = 'WAITING' " +
            "AND (w.preferredDateFrom IS NULL OR w.preferredDateFrom <= :availableDate) " +
            "AND (w.preferredDateTo IS NULL OR w.preferredDateTo >= :availableDate) " +
            "ORDER BY w.priority DESC, w.createdAt ASC")
    List<WaitlistEntry> findMatchingForAvailableSlot(@Param("serviceType") String serviceType,
                                                      @Param("availableDate") LocalDate availableDate);

    @Query("SELECT COUNT(w) FROM WaitlistEntry w WHERE w.practitionerId = :practitionerId AND w.status = 'WAITING'")
    long countWaitingByPractitioner(@Param("practitionerId") UUID practitionerId);

    @Query("SELECT COUNT(w) FROM WaitlistEntry w WHERE w.unitId = :unitId AND w.status = 'WAITING'")
    long countWaitingByUnit(@Param("unitId") UUID unitId);

    @Query("SELECT w FROM WaitlistEntry w WHERE w.status = 'NOTIFIED' " +
            "AND w.notifiedAt < :threshold")
    List<WaitlistEntry> findExpiredNotifications(@Param("threshold") java.time.Instant threshold);
}
