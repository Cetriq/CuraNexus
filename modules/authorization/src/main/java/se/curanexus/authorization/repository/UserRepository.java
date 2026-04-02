package se.curanexus.authorization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.authorization.domain.User;
import se.curanexus.authorization.domain.UserType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByHsaId(String hsaId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByHsaId(String hsaId);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    List<User> findByUserType(UserType userType);

    List<User> findByDepartment(String department);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.code = :roleCode")
    List<User> findByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT u FROM User u WHERE u.active = true AND u.userType = :userType")
    List<User> findActiveByUserType(@Param("userType") UserType userType);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);

    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.roles r JOIN FETCH r.permissions WHERE u.id = :userId")
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}
