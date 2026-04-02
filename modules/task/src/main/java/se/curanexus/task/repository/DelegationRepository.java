package se.curanexus.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.task.domain.Delegation;
import se.curanexus.task.domain.DelegationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DelegationRepository extends JpaRepository<Delegation, UUID> {

    List<Delegation> findByFromUserIdOrderByCreatedAtDesc(UUID fromUserId);

    List<Delegation> findByToUserIdOrderByCreatedAtDesc(UUID toUserId);

    List<Delegation> findByFromUserIdAndStatus(UUID fromUserId, DelegationStatus status);

    List<Delegation> findByToUserIdAndStatus(UUID toUserId, DelegationStatus status);

    @Query("SELECT d FROM Delegation d WHERE d.status = 'ACTIVE' AND d.validFrom <= :now AND d.validUntil > :now")
    List<Delegation> findCurrentlyActive(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM Delegation d WHERE d.fromUserId = :userId AND d.status = 'ACTIVE' AND d.validFrom <= :now AND d.validUntil > :now")
    List<Delegation> findActiveDelegationsFrom(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT d FROM Delegation d WHERE d.toUserId = :userId AND d.status = 'ACTIVE' AND d.validFrom <= :now AND d.validUntil > :now")
    List<Delegation> findActiveDelegationsTo(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT d FROM Delegation d WHERE d.status = 'ACTIVE' AND d.validUntil <= :now")
    List<Delegation> findExpiredDelegations(@Param("now") LocalDateTime now);
}
