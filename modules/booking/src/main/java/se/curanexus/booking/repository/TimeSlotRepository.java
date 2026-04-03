package se.curanexus.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.booking.domain.AppointmentType;
import se.curanexus.booking.domain.TimeSlot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    // Find available slots
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.schedule.id = :scheduleId " +
            "AND ts.status = 'AVAILABLE' " +
            "AND ts.startTime >= :from AND ts.startTime < :to " +
            "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableByScheduleAndDateRange(@Param("scheduleId") UUID scheduleId,
                                                        @Param("from") LocalDateTime from,
                                                        @Param("to") LocalDateTime to);

    // Find available slots by practitioner
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.practitionerId = :practitionerId " +
            "AND ts.status = 'AVAILABLE' " +
            "AND ts.startTime >= :from AND ts.startTime < :to " +
            "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableByPractitionerAndDateRange(@Param("practitionerId") UUID practitionerId,
                                                            @Param("from") LocalDateTime from,
                                                            @Param("to") LocalDateTime to);

    // Find available slots by appointment type
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.practitionerId = :practitionerId " +
            "AND ts.status = 'AVAILABLE' " +
            "AND (ts.appointmentType IS NULL OR ts.appointmentType = :type) " +
            "AND ts.startTime >= :from AND ts.startTime < :to " +
            "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableByPractitionerAndTypeAndDateRange(
            @Param("practitionerId") UUID practitionerId,
            @Param("type") AppointmentType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Find all slots for a schedule in date range
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.schedule.id = :scheduleId " +
            "AND ts.startTime >= :from AND ts.startTime < :to " +
            "ORDER BY ts.startTime")
    List<TimeSlot> findByScheduleAndDateRange(@Param("scheduleId") UUID scheduleId,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    // Find next available slot
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.practitionerId = :practitionerId " +
            "AND ts.status = 'AVAILABLE' " +
            "AND ts.startTime > :after " +
            "ORDER BY ts.startTime")
    List<TimeSlot> findNextAvailableByPractitioner(@Param("practitionerId") UUID practitionerId,
                                                    @Param("after") LocalDateTime after);

    // Count available slots
    @Query("SELECT COUNT(ts) FROM TimeSlot ts WHERE ts.schedule.id = :scheduleId " +
            "AND ts.status = 'AVAILABLE' " +
            "AND ts.startTime >= :from AND ts.startTime < :to")
    long countAvailableByScheduleAndDateRange(@Param("scheduleId") UUID scheduleId,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    // Delete old slots
    @Query("DELETE FROM TimeSlot ts WHERE ts.startTime < :before AND ts.status = 'AVAILABLE'")
    void deleteOldUnbookedSlots(@Param("before") LocalDateTime before);

    // Check if slot exists
    boolean existsByScheduleIdAndStartTime(UUID scheduleId, LocalDateTime startTime);

    // Find slots by schedule and time range (for deletion)
    List<TimeSlot> findByScheduleIdAndStartTimeBetween(UUID scheduleId, LocalDateTime from, LocalDateTime to);

    // Find available slots by unit
    @Query("SELECT ts FROM TimeSlot ts JOIN ts.schedule s WHERE s.unitId = :unitId " +
            "AND ts.status = 'AVAILABLE' " +
            "AND ts.startTime >= :from AND ts.startTime < :to " +
            "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableByUnitAndDateRange(@Param("unitId") UUID unitId,
                                                    @Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to);
}
