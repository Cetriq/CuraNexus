package se.curanexus.triage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.triage.domain.Symptom;

import java.util.List;
import java.util.UUID;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, UUID> {

    List<Symptom> findByAssessmentIdOrderByRecordedAtAsc(UUID assessmentId);

    @Query("SELECT s FROM Symptom s WHERE s.assessment.id = :assessmentId AND s.isChiefComplaint = true")
    List<Symptom> findChiefComplaintsByAssessmentId(@Param("assessmentId") UUID assessmentId);

    @Query("SELECT s.symptomCode, COUNT(s) FROM Symptom s " +
           "JOIN s.assessment a " +
           "WHERE a.priority = :priority " +
           "GROUP BY s.symptomCode " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> findMostCommonSymptomsByPriority(@Param("priority") String priority);
}
