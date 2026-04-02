package se.curanexus.authorization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.Permission;
import se.curanexus.authorization.domain.ResourceType;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByResource(ResourceType resource);

    List<Permission> findByAction(ActionType action);

    List<Permission> findByResourceAndAction(ResourceType resource, ActionType action);

    @Query("SELECT p FROM Permission p WHERE p.code IN :codes")
    List<Permission> findByCodes(@Param("codes") Set<String> codes);

    @Query("SELECT DISTINCT p FROM Permission p JOIN Role r ON p MEMBER OF r.permissions WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT DISTINCT p FROM Permission p JOIN Role r ON p MEMBER OF r.permissions WHERE r.code = :roleCode")
    List<Permission> findByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT DISTINCT p FROM Permission p JOIN Role r ON p MEMBER OF r.permissions JOIN User u ON r MEMBER OF u.roles WHERE u.id = :userId")
    Set<Permission> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    long countRolesWithPermission(@Param("permissionId") UUID permissionId);
}
