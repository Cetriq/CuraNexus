package se.curanexus.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.booking.domain.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    List<Schedule> findByPractitionerIdAndActiveTrue(UUID practitionerId);

    List<Schedule> findByUnitIdAndActiveTrue(UUID unitId);

    Optional<Schedule> findByPractitionerIdAndServiceTypeAndActiveTrue(UUID practitionerId, String serviceType);

    @Query("SELECT s FROM Schedule s WHERE s.practitionerId = :practitionerId " +
            "AND s.active = true " +
            "AND s.validFrom <= :date " +
            "AND (s.validTo IS NULL OR s.validTo >= :date)")
    List<Schedule> findActiveByPractitionerAndDate(@Param("practitionerId") UUID practitionerId,
                                                    @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.unitId = :unitId " +
            "AND s.active = true " +
            "AND s.validFrom <= :date " +
            "AND (s.validTo IS NULL OR s.validTo >= :date)")
    List<Schedule> findActiveByUnitAndDate(@Param("unitId") UUID unitId,
                                            @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.active = true " +
            "AND s.validFrom <= :date " +
            "AND (s.validTo IS NULL OR s.validTo >= :date)")
    List<Schedule> findAllActiveForDate(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT s.serviceType FROM Schedule s WHERE s.unitId = :unitId AND s.active = true AND s.serviceType IS NOT NULL")
    List<String> findDistinctServiceTypesByUnit(@Param("unitId") UUID unitId);

    List<Schedule> findByPractitionerId(UUID practitionerId);

    @Query("SELECT s FROM Schedule s WHERE s.unitId = :unitId " +
            "AND s.active = true " +
            "AND s.validFrom <= :date " +
            "AND (s.validTo IS NULL OR s.validTo >= :date)")
    List<Schedule> findActiveByUnitId(@Param("unitId") UUID unitId, @Param("date") LocalDate date);
}
