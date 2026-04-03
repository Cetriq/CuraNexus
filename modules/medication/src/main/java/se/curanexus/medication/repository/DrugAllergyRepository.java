package se.curanexus.medication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.medication.domain.DrugAllergy;

import java.util.List;
import java.util.UUID;

@Repository
public interface DrugAllergyRepository extends JpaRepository<DrugAllergy, UUID> {

    List<DrugAllergy> findByPatientIdAndActiveTrue(UUID patientId);

    List<DrugAllergy> findByPatientId(UUID patientId);

    @Query("SELECT da FROM DrugAllergy da WHERE da.patientId = :patientId " +
            "AND da.active = true " +
            "AND (da.atcCode = :atcCode OR da.atcCode LIKE CONCAT(SUBSTRING(:atcCode, 1, 3), '%'))")
    List<DrugAllergy> findMatchingAllergies(@Param("patientId") UUID patientId,
                                             @Param("atcCode") String atcCode);

    @Query("SELECT da FROM DrugAllergy da WHERE da.patientId = :patientId " +
            "AND da.active = true " +
            "AND da.atcCode IN :atcCodes")
    List<DrugAllergy> findByPatientAndAtcCodes(@Param("patientId") UUID patientId,
                                                @Param("atcCodes") List<String> atcCodes);

    List<DrugAllergy> findByPatientIdAndVerifiedTrue(UUID patientId);

    @Query("SELECT da FROM DrugAllergy da WHERE da.patientId = :patientId " +
            "AND da.active = true " +
            "AND LOWER(da.substanceName) LIKE LOWER(CONCAT('%', :substance, '%'))")
    List<DrugAllergy> findByPatientAndSubstance(@Param("patientId") UUID patientId,
                                                 @Param("substance") String substance);

    long countByPatientIdAndActiveTrue(UUID patientId);
}
