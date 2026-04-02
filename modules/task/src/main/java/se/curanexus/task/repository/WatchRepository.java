package se.curanexus.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.task.domain.Watch;
import se.curanexus.task.domain.WatchType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchRepository extends JpaRepository<Watch, UUID> {

    List<Watch> findByUserIdAndActiveTrue(UUID userId);

    List<Watch> findByUserId(UUID userId);

    List<Watch> findByWatchTypeAndTargetIdAndActiveTrue(WatchType watchType, UUID targetId);

    @Query("SELECT w FROM Watch w WHERE w.userId = :userId AND w.watchType = :watchType AND w.targetId = :targetId")
    Optional<Watch> findByUserAndTarget(@Param("userId") UUID userId, @Param("watchType") WatchType watchType, @Param("targetId") UUID targetId);

    @Query("SELECT w FROM Watch w WHERE w.watchType = :watchType AND w.targetId = :targetId AND w.active = true AND w.notifyOnChange = true")
    List<Watch> findWatchersToNotify(@Param("watchType") WatchType watchType, @Param("targetId") UUID targetId);

    @Query("SELECT COUNT(w) FROM Watch w WHERE w.userId = :userId AND w.active = true")
    long countActiveByUser(@Param("userId") UUID userId);
}
