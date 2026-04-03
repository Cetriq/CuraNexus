package se.curanexus.medication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.medication.domain.MedicationInteraction;
import se.curanexus.medication.domain.MedicationInteraction.InteractionSeverity;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationInteractionRepository extends JpaRepository<MedicationInteraction, UUID> {

    @Query("SELECT mi FROM MedicationInteraction mi WHERE mi.active = true " +
            "AND ((mi.atcCode1 = :atc1 AND mi.atcCode2 = :atc2) " +
            "OR (mi.atcCode1 = :atc2 AND mi.atcCode2 = :atc1))")
    List<MedicationInteraction> findByAtcCodePair(@Param("atc1") String atc1, @Param("atc2") String atc2);

    @Query("SELECT mi FROM MedicationInteraction mi WHERE mi.active = true " +
            "AND (mi.atcCode1 IN :atcCodes OR mi.atcCode2 IN :atcCodes)")
    List<MedicationInteraction> findByAtcCodes(@Param("atcCodes") List<String> atcCodes);

    @Query("SELECT mi FROM MedicationInteraction mi WHERE mi.active = true " +
            "AND mi.severity IN :severities " +
            "AND (mi.atcCode1 IN :atcCodes OR mi.atcCode2 IN :atcCodes)")
    List<MedicationInteraction> findByAtcCodesAndSeverity(@Param("atcCodes") List<String> atcCodes,
                                                           @Param("severities") List<InteractionSeverity> severities);

    List<MedicationInteraction> findBySeverityAndActiveTrue(InteractionSeverity severity);

    @Query("SELECT mi FROM MedicationInteraction mi WHERE mi.active = true " +
            "AND (mi.atcCode1 LIKE CONCAT(:atcPrefix, '%') " +
            "OR mi.atcCode2 LIKE CONCAT(:atcPrefix, '%'))")
    List<MedicationInteraction> findByAtcPrefix(@Param("atcPrefix") String atcPrefix);
}
