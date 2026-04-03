package se.curanexus.authorization.abac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.authorization.domain.*;
import se.curanexus.authorization.repository.CareRelationRepository;
import se.curanexus.authorization.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private AccessPolicyRepository policyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CareRelationRepository careRelationRepository;

    @Mock
    private AccessAuditLogRepository auditLogRepository;

    private AccessControlService accessControlService;
    private PolicyEvaluator policyEvaluator;

    private UUID userId;
    private UUID patientId;
    private UUID encounterId;
    private User testUser;

    @BeforeEach
    void setUp() {
        policyEvaluator = new PolicyEvaluator(policyRepository, userRepository, careRelationRepository);
        accessControlService = new AccessControlService(policyEvaluator, policyRepository, auditLogRepository);

        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        encounterId = UUID.randomUUID();

        testUser = new User("dr.smith", "dr.smith@hospital.se", "John", "Smith", UserType.INTERNAL);
        Role doctorRole = new Role("Doctor", "DOCTOR");
        Permission readPermission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        doctorRole.addPermission(readPermission);
        testUser.addRole(doctorRole);
    }

    @Test
    void shouldPermitAccessWithCareRelation() {
        // Given
        when(userRepository.findByIdWithRolesAndPermissions(userId))
                .thenReturn(Optional.of(testUser));
        when(careRelationRepository.hasActiveCareRelation(eq(userId), eq(patientId), any(LocalDateTime.class)))
                .thenReturn(true);

        AccessPolicy permitPolicy = new AccessPolicy("clinical-patient-read", PolicyType.PERMIT);
        permitPolicy.setResourceType(ResourceType.PATIENT);
        permitPolicy.setActionType(ActionType.READ);
        permitPolicy.setRequiredPermission("PATIENT_READ");
        permitPolicy.setRequireCareRelation(true);
        permitPolicy.setPriority(50);

        when(policyRepository.findByResourceAndAction(ResourceType.PATIENT, ActionType.READ))
                .thenReturn(List.of(permitPolicy));

        // When
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .build();

        AccessDecision decision = accessControlService.checkAccess(context);

        // Then
        assertThat(decision.granted()).isTrue();
        assertThat(decision.outcome()).isEqualTo("PERMIT");
    }

    @Test
    void shouldDenyAccessWithoutCareRelation() {
        // Given
        when(userRepository.findByIdWithRolesAndPermissions(userId))
                .thenReturn(Optional.of(testUser));
        when(careRelationRepository.hasActiveCareRelation(eq(userId), eq(patientId), any(LocalDateTime.class)))
                .thenReturn(false);

        AccessPolicy requireContextPolicy = new AccessPolicy("require-patient-care-relation", PolicyType.REQUIRE_CONTEXT);
        requireContextPolicy.setResourceType(ResourceType.PATIENT);
        requireContextPolicy.setRequireCareRelation(true);
        requireContextPolicy.setPriority(100);

        when(policyRepository.findByResourceAndAction(ResourceType.PATIENT, ActionType.READ))
                .thenReturn(List.of(requireContextPolicy));

        // When
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .build();

        AccessDecision decision = accessControlService.checkAccess(context);

        // Then
        assertThat(decision.granted()).isFalse();
        assertThat(decision.reasons()).anyMatch(r -> r.contains("care relation"));
    }

    @Test
    void shouldDenyAccessWithoutPermission() {
        // Given - user without PATIENT_READ permission
        User userWithoutPermission = new User("nurse.doe", "nurse@hospital.se", "Jane", "Doe", UserType.INTERNAL);
        Role nurseRole = new Role("Nurse", "NURSE");
        // No PATIENT_READ permission added
        userWithoutPermission.addRole(nurseRole);

        when(userRepository.findByIdWithRolesAndPermissions(userId))
                .thenReturn(Optional.of(userWithoutPermission));

        AccessPolicy permitPolicy = new AccessPolicy("clinical-patient-read", PolicyType.PERMIT);
        permitPolicy.setResourceType(ResourceType.PATIENT);
        permitPolicy.setActionType(ActionType.READ);
        permitPolicy.setRequiredPermission("PATIENT_READ");
        permitPolicy.setRequireCareRelation(true);
        permitPolicy.setPriority(50);

        when(policyRepository.findByResourceAndAction(ResourceType.PATIENT, ActionType.READ))
                .thenReturn(List.of(permitPolicy));

        // When
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .build();

        AccessDecision decision = accessControlService.checkAccess(context);

        // Then - no permit because missing permission
        assertThat(decision.granted()).isFalse();
    }

    @Test
    void shouldDenyAccessForInactiveUser() {
        // Given
        testUser.deactivate();
        when(userRepository.findByIdWithRolesAndPermissions(userId))
                .thenReturn(Optional.of(testUser));

        AccessPolicy permitPolicy = new AccessPolicy("clinical-patient-read", PolicyType.PERMIT);
        when(policyRepository.findByResourceAndAction(ResourceType.PATIENT, ActionType.READ))
                .thenReturn(List.of(permitPolicy));

        // When
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .build();

        AccessDecision decision = accessControlService.checkAccess(context);

        // Then
        assertThat(decision.granted()).isFalse();
        assertThat(decision.reasons()).anyMatch(r -> r.contains("not active"));
    }

    @Test
    void shouldLogAllAccessDecisions() {
        // Given
        when(userRepository.findByIdWithRolesAndPermissions(userId))
                .thenReturn(Optional.of(testUser));
        when(careRelationRepository.hasActiveCareRelation(eq(userId), eq(patientId), any(LocalDateTime.class)))
                .thenReturn(true);

        AccessPolicy permitPolicy = new AccessPolicy("clinical-patient-read", PolicyType.PERMIT);
        permitPolicy.setResourceType(ResourceType.PATIENT);
        permitPolicy.setActionType(ActionType.READ);
        permitPolicy.setRequiredPermission("PATIENT_READ");
        permitPolicy.setRequireCareRelation(true);

        when(policyRepository.findByResourceAndAction(ResourceType.PATIENT, ActionType.READ))
                .thenReturn(List.of(permitPolicy));

        // When
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .build();

        AccessDecision decision = accessControlService.checkAccess(context);

        // Then
        assertThat(decision.decisionId()).isNotNull();
        assertThat(decision.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleEmergencyAccess() {
        // Given
        when(userRepository.findByIdWithRolesAndPermissions(userId))
                .thenReturn(Optional.of(testUser));

        AccessPolicy emergencyPolicy = new AccessPolicy("emergency-access-override", PolicyType.EMERGENCY_OVERRIDE);
        emergencyPolicy.setResourceType(ResourceType.PATIENT);
        emergencyPolicy.setRequireCareRelation(false);
        emergencyPolicy.setPriority(200);

        when(policyRepository.findByResourceAndAction(ResourceType.PATIENT, ActionType.READ))
                .thenReturn(List.of(emergencyPolicy));

        // When
        AccessDecision decision = accessControlService.checkEmergencyAccess(
                userId, patientId, "Patient collapsed in hallway - emergency assessment needed");

        // Then
        assertThat(decision.context().isEmergencyAccess()).isTrue();
        assertThat(decision.context().getAccessReason()).isPresent();
    }

    @Test
    void shouldDenyEmergencyAccessWithoutReason() {
        // When
        AccessDecision decision = accessControlService.checkEmergencyAccess(userId, patientId, "");

        // Then
        assertThat(decision.granted()).isFalse();
        assertThat(decision.reasons()).anyMatch(r -> r.contains("requires a reason"));
    }

    @Test
    void shouldTrackEvaluatedPolicies() {
        // Given
        when(userRepository.findByIdWithRolesAndPermissions(userId))
                .thenReturn(Optional.of(testUser));
        when(careRelationRepository.hasActiveCareRelation(eq(userId), eq(patientId), any(LocalDateTime.class)))
                .thenReturn(true);

        AccessPolicy requireContext = new AccessPolicy("require-context", PolicyType.REQUIRE_CONTEXT);
        requireContext.setResourceType(ResourceType.PATIENT);
        requireContext.setRequireCareRelation(true);
        requireContext.setPriority(100);

        AccessPolicy permitPolicy = new AccessPolicy("permit-access", PolicyType.PERMIT);
        permitPolicy.setResourceType(ResourceType.PATIENT);
        permitPolicy.setActionType(ActionType.READ);
        permitPolicy.setRequiredPermission("PATIENT_READ");
        permitPolicy.setPriority(50);

        when(policyRepository.findByResourceAndAction(ResourceType.PATIENT, ActionType.READ))
                .thenReturn(List.of(requireContext, permitPolicy));

        // When
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .build();

        AccessDecision decision = accessControlService.checkAccess(context);

        // Then
        assertThat(decision.evaluatedPolicies()).isNotEmpty();
        assertThat(decision.evaluatedPolicies())
                .anyMatch(pe -> "require-context".equals(pe.policyName()));
    }
}
