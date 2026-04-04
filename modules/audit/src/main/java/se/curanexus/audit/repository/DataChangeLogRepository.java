package se.curanexus.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.audit.domain.DataChangeLog;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataChangeLogRepository extends JpaRepository<DataChangeLog, UUID> {

    List<DataChangeLog> findByAuditEventIdOrderByTimestampDesc(UUID auditEventId);

    List<DataChangeLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(
            ResourceType resourceType, UUID resourceId);

    List<DataChangeLog> findByResourceTypeAndResourceIdAndTimestampBetweenOrderByTimestampDesc(
            ResourceType resourceType, UUID resourceId, Instant from, Instant to);

    List<DataChangeLog> findByResourceTypeAndResourceIdAndFieldNameOrderByTimestampDesc(
            ResourceType resourceType, UUID resourceId, String fieldName);
}
