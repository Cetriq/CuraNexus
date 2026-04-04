package se.curanexus.forms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.forms.domain.FormSubmission;
import se.curanexus.forms.domain.SubmissionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {

    List<FormSubmission> findByPatientId(UUID patientId);

    Page<FormSubmission> findByPatientId(UUID patientId, Pageable pageable);

    List<FormSubmission> findByPatientIdAndStatus(UUID patientId, SubmissionStatus status);

    List<FormSubmission> findByEncounterId(UUID encounterId);

    List<FormSubmission> findByTemplateId(UUID templateId);

    @Query("""
        SELECT s FROM FormSubmission s
        WHERE s.patientId = :patientId
        AND s.template.code = :templateCode
        ORDER BY s.startedAt DESC
        """)
    List<FormSubmission> findByPatientIdAndTemplateCode(
            @Param("patientId") UUID patientId,
            @Param("templateCode") String templateCode);

    @Query("""
        SELECT s FROM FormSubmission s
        WHERE s.status = 'IN_PROGRESS'
        AND s.expiresAt IS NOT NULL
        AND s.expiresAt < :now
        """)
    List<FormSubmission> findExpiredSubmissions(@Param("now") Instant now);

    @Query("""
        SELECT s FROM FormSubmission s
        WHERE s.status = 'COMPLETED'
        AND s.reviewedAt IS NULL
        ORDER BY s.completedAt ASC
        """)
    Page<FormSubmission> findPendingReview(Pageable pageable);

    @Query("""
        SELECT COUNT(s) FROM FormSubmission s
        WHERE s.patientId = :patientId
        AND s.template.code = :templateCode
        AND s.status = 'COMPLETED'
        AND s.completedAt > :since
        """)
    long countRecentSubmissions(
            @Param("patientId") UUID patientId,
            @Param("templateCode") String templateCode,
            @Param("since") Instant since);
}
