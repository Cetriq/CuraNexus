package se.curanexus.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.lab.domain.LabOrderItem;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabOrderItemRepository extends JpaRepository<LabOrderItem, UUID> {

    /**
     * Hämta LabOrderItem med dess tillhörande LabOrder (eager fetch).
     * Undviker N+1 problem och ineffektiv findAll().stream().filter().
     */
    @Query("SELECT i FROM LabOrderItem i JOIN FETCH i.labOrder WHERE i.id = :id")
    Optional<LabOrderItem> findByIdWithOrder(@Param("id") UUID id);

    /**
     * Hämta LabOrderItem via testkod och order.
     */
    @Query("SELECT i FROM LabOrderItem i WHERE i.labOrder.id = :orderId AND i.testCode = :testCode")
    Optional<LabOrderItem> findByOrderIdAndTestCode(@Param("orderId") UUID orderId, @Param("testCode") String testCode);
}
