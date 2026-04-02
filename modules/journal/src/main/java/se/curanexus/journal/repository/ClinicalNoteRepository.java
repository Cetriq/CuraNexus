package se.curanexus.journal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.journal.domain.ClinicalNote;
import se.curanexus.journal.domain.NoteStatus;
import se.curanexus.journal.domain.NoteType;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, UUID> {

    List<ClinicalNote> findByEncounterIdOrderByCreatedAtDesc(UUID encounterId);

    List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<ClinicalNote> findByEncounterIdAndType(UUID encounterId, NoteType type);

    List<ClinicalNote> findByPatientIdAndStatus(UUID patientId, NoteStatus status);

    List<ClinicalNote> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    @Query("SELECT n FROM ClinicalNote n WHERE n.patientId = :patientId AND n.type = :type ORDER BY n.createdAt DESC")
    List<ClinicalNote> findPatientNotesByType(@Param("patientId") UUID patientId, @Param("type") NoteType type);

    @Query("SELECT n FROM ClinicalNote n WHERE n.encounterId = :encounterId AND n.status = 'DRAFT'")
    List<ClinicalNote> findDraftsByEncounter(@Param("encounterId") UUID encounterId);

    @Query("SELECT COUNT(n) FROM ClinicalNote n WHERE n.encounterId = :encounterId AND n.status = 'DRAFT'")
    long countDraftsByEncounter(@Param("encounterId") UUID encounterId);
}
