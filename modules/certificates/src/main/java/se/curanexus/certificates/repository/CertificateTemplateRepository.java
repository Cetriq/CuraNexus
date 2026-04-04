package se.curanexus.certificates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.certificates.domain.CertificateTemplate;
import se.curanexus.certificates.domain.CertificateType;
import se.curanexus.certificates.domain.TemplateStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, UUID> {

    Optional<CertificateTemplate> findByCode(String code);

    @Query("SELECT t FROM CertificateTemplate t WHERE t.code = :code AND t.status = 'ACTIVE'")
    Optional<CertificateTemplate> findActiveByCode(@Param("code") String code);

    List<CertificateTemplate> findByStatus(TemplateStatus status);

    List<CertificateTemplate> findByType(CertificateType type);

    List<CertificateTemplate> findByTypeAndStatus(CertificateType type, TemplateStatus status);

    boolean existsByCode(String code);
}
