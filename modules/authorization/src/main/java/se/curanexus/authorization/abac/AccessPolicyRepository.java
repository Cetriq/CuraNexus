package se.curanexus.authorization.abac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.ResourceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessPolicyRepository extends JpaRepository<AccessPolicy, UUID> {

    Optional<AccessPolicy> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT p FROM AccessPolicy p WHERE p.active = true ORDER BY p.priority DESC")
    List<AccessPolicy> findAllActive();

    @Query("SELECT p FROM AccessPolicy p WHERE p.active = true AND p.resourceType = :resourceType ORDER BY p.priority DESC")
    List<AccessPolicy> findByResourceType(@Param("resourceType") ResourceType resourceType);

    @Query("SELECT p FROM AccessPolicy p WHERE p.active = true AND " +
            "(p.resourceType = :resourceType OR p.resourceType IS NULL) AND " +
            "(p.actionType = :actionType OR p.actionType IS NULL) " +
            "ORDER BY p.priority DESC")
    List<AccessPolicy> findByResourceAndAction(
            @Param("resourceType") ResourceType resourceType,
            @Param("actionType") ActionType actionType);

    @Query("SELECT p FROM AccessPolicy p WHERE p.active = true AND p.policyType = :policyType ORDER BY p.priority DESC")
    List<AccessPolicy> findByPolicyType(@Param("policyType") PolicyType policyType);

    @Query("SELECT p FROM AccessPolicy p WHERE p.active = true AND p.requiredRole = :roleCode ORDER BY p.priority DESC")
    List<AccessPolicy> findByRequiredRole(@Param("roleCode") String roleCode);
}
