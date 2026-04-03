package se.curanexus.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.notification.domain.StoredEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoredEventRepository extends JpaRepository<StoredEvent, UUID> {

    Optional<StoredEvent> findByEventId(UUID eventId);

    Page<StoredEvent> findByAggregateIdOrderByOccurredAtDesc(UUID aggregateId, Pageable pageable);

    Page<StoredEvent> findByAggregateTypeOrderByOccurredAtDesc(String aggregateType, Pageable pageable);

    Page<StoredEvent> findByEventTypeOrderByOccurredAtDesc(String eventType, Pageable pageable);

    @Query("""
        SELECT e FROM StoredEvent e
        WHERE e.aggregateType = :aggregateType
        AND e.occurredAt >= :fromDate
        AND e.occurredAt <= :toDate
        ORDER BY e.occurredAt DESC
        """)
    Page<StoredEvent> findByAggregateTypeAndDateRange(
            @Param("aggregateType") String aggregateType,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    List<StoredEvent> findByProcessedFalseOrderByOccurredAtAsc();

    @Query("SELECT COUNT(e) FROM StoredEvent e WHERE e.aggregateType = :aggregateType")
    long countByAggregateType(@Param("aggregateType") String aggregateType);

    @Query("SELECT COUNT(e) FROM StoredEvent e WHERE e.eventType = :eventType")
    long countByEventType(@Param("eventType") String eventType);
}
