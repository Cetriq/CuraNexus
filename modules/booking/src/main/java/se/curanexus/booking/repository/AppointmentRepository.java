package se.curanexus.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.booking.domain.Appointment;
import se.curanexus.booking.domain.AppointmentStatus;
import se.curanexus.booking.domain.AppointmentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByBookingReference(String bookingReference);

    // Patient queries
    List<Appointment> findByPatientIdOrderByStartTimeDesc(UUID patientId);

    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId AND a.status = :status ORDER BY a.startTime")
    List<Appointment> findByPatientIdAndStatus(@Param("patientId") UUID patientId,
                                                @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId AND a.startTime > :now AND a.status = 'BOOKED' ORDER BY a.startTime")
    List<Appointment> findUpcomingByPatientId(@Param("patientId") UUID patientId,
                                               @Param("now") LocalDateTime now);

    // Practitioner queries
    @Query("SELECT a FROM Appointment a WHERE a.practitionerId = :practitionerId AND a.startTime >= :start AND a.startTime < :end ORDER BY a.startTime")
    List<Appointment> findByPractitionerIdAndDateRange(@Param("practitionerId") UUID practitionerId,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.practitionerId = :practitionerId AND a.startTime >= :today AND a.status IN ('BOOKED', 'CHECKED_IN') ORDER BY a.startTime")
    List<Appointment> findTodayByPractitionerId(@Param("practitionerId") UUID practitionerId,
                                                 @Param("today") LocalDateTime today);

    // Unit queries
    @Query("SELECT a FROM Appointment a WHERE a.unitId = :unitId AND a.startTime >= :start AND a.startTime < :end ORDER BY a.startTime")
    List<Appointment> findByUnitIdAndDateRange(@Param("unitId") UUID unitId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    // Time-based queries
    @Query("SELECT a FROM Appointment a WHERE a.startTime >= :start AND a.startTime < :end ORDER BY a.startTime")
    List<Appointment> findByDateRange(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    // Conflict detection
    @Query("SELECT a FROM Appointment a WHERE a.practitionerId = :practitionerId " +
            "AND a.status = 'BOOKED' " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findConflictingAppointments(@Param("practitionerId") UUID practitionerId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    // Reminder queries
    @Query("SELECT a FROM Appointment a WHERE a.status = 'BOOKED' " +
            "AND a.reminderSent = false " +
            "AND a.startTime BETWEEN :from AND :to")
    List<Appointment> findAppointmentsNeedingReminder(@Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);

    // Statistics
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.practitionerId = :practitionerId " +
            "AND a.startTime >= :start AND a.startTime < :end AND a.status = :status")
    long countByPractitionerAndDateRangeAndStatus(@Param("practitionerId") UUID practitionerId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("status") AppointmentStatus status);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a WHERE a.unitId = :unitId " +
            "AND a.startTime >= :start AND a.startTime < :end GROUP BY a.status")
    List<Object[]> countByUnitAndDateRangeGroupByStatus(@Param("unitId") UUID unitId,
                                                         @Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    // Search
    @Query("SELECT a FROM Appointment a WHERE " +
            "(:patientId IS NULL OR a.patientId = :patientId) AND " +
            "(:practitionerId IS NULL OR a.practitionerId = :practitionerId) AND " +
            "(:unitId IS NULL OR a.unitId = :unitId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:type IS NULL OR a.appointmentType = :type) AND " +
            "(:from IS NULL OR a.startTime >= :from) AND " +
            "(:to IS NULL OR a.startTime < :to)")
    Page<Appointment> search(@Param("patientId") UUID patientId,
                              @Param("practitionerId") UUID practitionerId,
                              @Param("unitId") UUID unitId,
                              @Param("status") AppointmentStatus status,
                              @Param("type") AppointmentType type,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              Pageable pageable);

    // Link to encounter
    Optional<Appointment> findByEncounterId(UUID encounterId);
}
