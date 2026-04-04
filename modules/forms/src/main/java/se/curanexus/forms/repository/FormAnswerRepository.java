package se.curanexus.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.forms.domain.FormAnswer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormAnswerRepository extends JpaRepository<FormAnswer, UUID> {

    List<FormAnswer> findBySubmissionId(UUID submissionId);

    Optional<FormAnswer> findBySubmissionIdAndFieldKey(UUID submissionId, String fieldKey);

    void deleteBySubmissionId(UUID submissionId);
}
