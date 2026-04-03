package se.curanexus.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.lab.domain.LabSpecimen;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabSpecimenRepository extends JpaRepository<LabSpecimen, UUID> {

    Optional<LabSpecimen> findByBarcode(String barcode);

    List<LabSpecimen> findByLabOrderId(UUID labOrderId);

    /** Hitta prover som inte tagits emot på lab */
    @Query("SELECT s FROM LabSpecimen s WHERE s.collectedAt IS NOT NULL " +
           "AND s.receivedAtLab IS NULL AND s.rejected = false " +
           "ORDER BY s.collectedAt ASC")
    List<LabSpecimen> findPendingLabReceipt();

    /** Hitta avvisade prover */
    @Query("SELECT s FROM LabSpecimen s JOIN s.labOrder o " +
           "WHERE o.orderingUnitId = :unitId AND s.rejected = true " +
           "ORDER BY s.createdAt DESC")
    List<LabSpecimen> findRejectedByUnit(@Param("unitId") UUID unitId);
}
