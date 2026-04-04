package se.curanexus.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.forms.domain.FormField;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, UUID> {

    List<FormField> findByTemplateIdOrderBySortOrder(UUID templateId);
}
