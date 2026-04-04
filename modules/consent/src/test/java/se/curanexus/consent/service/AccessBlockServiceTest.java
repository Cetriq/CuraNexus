package se.curanexus.consent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.consent.api.dto.*;
import se.curanexus.consent.domain.AccessBlock;
import se.curanexus.consent.domain.AccessBlockType;
import se.curanexus.consent.exception.AccessBlockNotFoundException;
import se.curanexus.consent.repository.AccessBlockRepository;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessBlockServiceTest {

    @Mock
    private AccessBlockRepository accessBlockRepository;

    @InjectMocks
    private AccessBlockService accessBlockService;

    private UUID patientId;
    private UUID blockId;
    private UUID unitId;
    private UUID practitionerId;
    private AccessBlock accessBlock;

    @BeforeEach
    void setUp() throws Exception {
        patientId = UUID.randomUUID();
        blockId = UUID.randomUUID();
        unitId = UUID.randomUUID();
        practitionerId = UUID.randomUUID();

        accessBlock = new AccessBlock(patientId, AccessBlockType.UNIT);
        accessBlock.setBlockedUnitId(unitId);
        accessBlock.setBlockedUnitName("Test Hospital");
        accessBlock.setReason("Privacy concerns");
        setPrivateField(accessBlock, "id", blockId);
        setPrivateField(accessBlock, "createdAt", Instant.now());
        setPrivateField(accessBlock, "updatedAt", Instant.now());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void createAccessBlock_ShouldCreateUnitBlock() {
        CreateAccessBlockRequest request = new CreateAccessBlockRequest(
                patientId,
                AccessBlockType.UNIT,
                unitId,
                "Test Hospital",
                null,
                null,
                null,
                "Privacy",
                null,
                null,
                UUID.randomUUID(),
                "Patient Name"
        );

        when(accessBlockRepository.save(any(AccessBlock.class))).thenReturn(accessBlock);

        AccessBlockDto result = accessBlockService.createAccessBlock(request);

        assertNotNull(result);
        assertEquals(blockId, result.id());
        assertEquals(AccessBlockType.UNIT, result.blockType());
        verify(accessBlockRepository).save(any(AccessBlock.class));
    }

    @Test
    void createAccessBlock_ShouldThrowException_WhenUnitIdMissing() {
        CreateAccessBlockRequest request = new CreateAccessBlockRequest(
                patientId,
                AccessBlockType.UNIT,
                null, // Missing unit ID
                null,
                null,
                null,
                null,
                "Privacy",
                null,
                null,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () ->
                accessBlockService.createAccessBlock(request));
    }

    @Test
    void createAccessBlock_ShouldThrowException_WhenPractitionerIdMissing() {
        CreateAccessBlockRequest request = new CreateAccessBlockRequest(
                patientId,
                AccessBlockType.PRACTITIONER,
                null,
                null,
                null, // Missing practitioner ID
                null,
                null,
                "Privacy",
                null,
                null,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () ->
                accessBlockService.createAccessBlock(request));
    }

    @Test
    void createAccessBlock_ShouldThrowException_WhenDataCategoryMissing() {
        CreateAccessBlockRequest request = new CreateAccessBlockRequest(
                patientId,
                AccessBlockType.DATA_CATEGORY,
                null,
                null,
                null,
                null,
                null, // Missing data category
                "Privacy",
                null,
                null,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () ->
                accessBlockService.createAccessBlock(request));
    }

    @Test
    void getAccessBlock_ShouldReturnBlock_WhenExists() {
        when(accessBlockRepository.findById(blockId)).thenReturn(Optional.of(accessBlock));

        AccessBlockDto result = accessBlockService.getAccessBlock(blockId);

        assertNotNull(result);
        assertEquals(blockId, result.id());
    }

    @Test
    void getAccessBlock_ShouldThrowException_WhenNotExists() {
        when(accessBlockRepository.findById(blockId)).thenReturn(Optional.empty());

        assertThrows(AccessBlockNotFoundException.class, () ->
                accessBlockService.getAccessBlock(blockId));
    }

    @Test
    void getPatientAccessBlocks_ShouldReturnList() {
        when(accessBlockRepository.findByPatientId(patientId)).thenReturn(List.of(accessBlock));

        List<AccessBlockSummaryDto> result = accessBlockService.getPatientAccessBlocks(patientId);

        assertEquals(1, result.size());
        assertEquals(blockId, result.get(0).id());
    }

    @Test
    void getActiveAccessBlocks_ShouldReturnActiveBlocks() {
        when(accessBlockRepository.findActiveBlocksForPatient(patientId)).thenReturn(List.of(accessBlock));

        List<AccessBlockSummaryDto> result = accessBlockService.getActiveAccessBlocks(patientId);

        assertEquals(1, result.size());
    }

    @Test
    void deactivateAccessBlock_ShouldDeactivate_WhenActive() {
        DeactivateAccessBlockRequest request = new DeactivateAccessBlockRequest(
                UUID.randomUUID(),
                "Patient request"
        );

        when(accessBlockRepository.findById(blockId)).thenReturn(Optional.of(accessBlock));
        when(accessBlockRepository.save(any(AccessBlock.class))).thenReturn(accessBlock);

        AccessBlockDto result = accessBlockService.deactivateAccessBlock(blockId, request);

        assertNotNull(result);
        verify(accessBlockRepository).save(accessBlock);
    }

    @Test
    void deactivateAccessBlock_ShouldThrowException_WhenAlreadyInactive() throws Exception {
        setPrivateField(accessBlock, "active", false);

        DeactivateAccessBlockRequest request = new DeactivateAccessBlockRequest(
                UUID.randomUUID(),
                "Patient request"
        );

        when(accessBlockRepository.findById(blockId)).thenReturn(Optional.of(accessBlock));

        assertThrows(IllegalStateException.class, () ->
                accessBlockService.deactivateAccessBlock(blockId, request));
    }

    @Test
    void checkAccess_ShouldReturnAllowed_WhenNoBlocks() {
        CheckAccessRequest request = new CheckAccessRequest(
                patientId,
                unitId,
                practitionerId,
                null
        );

        when(accessBlockRepository.isUnitBlockedForPatient(patientId, unitId)).thenReturn(false);
        when(accessBlockRepository.isPractitionerBlockedForPatient(patientId, practitionerId)).thenReturn(false);
        when(accessBlockRepository.findByPatientIdAndBlockTypeAndActive(patientId, AccessBlockType.EXTERNAL_UNITS, true))
                .thenReturn(List.of());

        AccessCheckResult result = accessBlockService.checkAccess(request);

        assertTrue(result.accessAllowed());
        assertFalse(result.unitBlocked());
        assertFalse(result.practitionerBlocked());
    }

    @Test
    void checkAccess_ShouldReturnBlocked_WhenUnitBlocked() {
        CheckAccessRequest request = new CheckAccessRequest(
                patientId,
                unitId,
                null,
                null
        );

        when(accessBlockRepository.isUnitBlockedForPatient(patientId, unitId)).thenReturn(true);
        when(accessBlockRepository.findActiveBlocksForPatientAndUnit(patientId, unitId))
                .thenReturn(List.of(accessBlock));
        when(accessBlockRepository.findByPatientIdAndBlockTypeAndActive(patientId, AccessBlockType.EXTERNAL_UNITS, true))
                .thenReturn(List.of());

        AccessCheckResult result = accessBlockService.checkAccess(request);

        assertFalse(result.accessAllowed());
        assertTrue(result.unitBlocked());
        assertEquals(1, result.activeBlocks().size());
    }

    @Test
    void checkAccess_ShouldReturnBlocked_WhenPractitionerBlocked() {
        AccessBlock practitionerBlock = new AccessBlock(patientId, AccessBlockType.PRACTITIONER);
        practitionerBlock.setBlockedPractitionerId(practitionerId);
        practitionerBlock.setBlockedPractitionerName("Dr. Smith");

        CheckAccessRequest request = new CheckAccessRequest(
                patientId,
                null,
                practitionerId,
                null
        );

        when(accessBlockRepository.isPractitionerBlockedForPatient(patientId, practitionerId)).thenReturn(true);
        when(accessBlockRepository.findActiveBlocksForPatientAndPractitioner(patientId, practitionerId))
                .thenReturn(List.of(practitionerBlock));

        AccessCheckResult result = accessBlockService.checkAccess(request);

        assertFalse(result.accessAllowed());
        assertTrue(result.practitionerBlocked());
    }

    @Test
    void isUnitBlocked_ShouldReturnTrue_WhenBlocked() {
        when(accessBlockRepository.isUnitBlockedForPatient(patientId, unitId)).thenReturn(true);

        boolean result = accessBlockService.isUnitBlocked(patientId, unitId);

        assertTrue(result);
    }

    @Test
    void isPractitionerBlocked_ShouldReturnTrue_WhenBlocked() {
        when(accessBlockRepository.isPractitionerBlockedForPatient(patientId, practitionerId)).thenReturn(true);

        boolean result = accessBlockService.isPractitionerBlocked(patientId, practitionerId);

        assertTrue(result);
    }

    @Test
    void countActiveBlocks_ShouldReturnCount() {
        when(accessBlockRepository.countActiveBlocksForPatient(patientId)).thenReturn(3L);

        long result = accessBlockService.countActiveBlocks(patientId);

        assertEquals(3L, result);
    }

    @Test
    void deleteAccessBlock_ShouldDelete_WhenInactive() throws Exception {
        setPrivateField(accessBlock, "active", false);

        when(accessBlockRepository.findById(blockId)).thenReturn(Optional.of(accessBlock));

        accessBlockService.deleteAccessBlock(blockId);

        verify(accessBlockRepository).delete(accessBlock);
    }

    @Test
    void deleteAccessBlock_ShouldThrowException_WhenActive() {
        when(accessBlockRepository.findById(blockId)).thenReturn(Optional.of(accessBlock));

        assertThrows(IllegalStateException.class, () -> accessBlockService.deleteAccessBlock(blockId));
    }
}
