package se.curanexus.consent.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.consent.api.dto.*;
import se.curanexus.consent.domain.AccessBlockType;
import se.curanexus.consent.service.AccessBlockService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/access-blocks")
@Tag(name = "Access Blocks", description = "Patient access block management (spärr)")
public class AccessBlockController {

    private final AccessBlockService accessBlockService;

    public AccessBlockController(AccessBlockService accessBlockService) {
        this.accessBlockService = accessBlockService;
    }

    @PostMapping
    @Operation(summary = "Create a new access block")
    public ResponseEntity<AccessBlockDto> createAccessBlock(@Valid @RequestBody CreateAccessBlockRequest request) {
        AccessBlockDto block = accessBlockService.createAccessBlock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(block);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get access block by ID")
    public ResponseEntity<AccessBlockDto> getAccessBlock(@PathVariable UUID id) {
        AccessBlockDto block = accessBlockService.getAccessBlock(id);
        return ResponseEntity.ok(block);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get all access blocks for a patient")
    public ResponseEntity<List<AccessBlockSummaryDto>> getPatientAccessBlocks(@PathVariable UUID patientId) {
        List<AccessBlockSummaryDto> blocks = accessBlockService.getPatientAccessBlocks(patientId);
        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/patient/{patientId}/active")
    @Operation(summary = "Get active access blocks for a patient")
    public ResponseEntity<List<AccessBlockSummaryDto>> getActiveAccessBlocks(@PathVariable UUID patientId) {
        List<AccessBlockSummaryDto> blocks = accessBlockService.getActiveAccessBlocks(patientId);
        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/patient/{patientId}/type/{blockType}")
    @Operation(summary = "Get access blocks by type for a patient")
    public ResponseEntity<List<AccessBlockSummaryDto>> getAccessBlocksByType(
            @PathVariable UUID patientId,
            @PathVariable AccessBlockType blockType) {
        List<AccessBlockSummaryDto> blocks = accessBlockService.getAccessBlocksByType(patientId, blockType);
        return ResponseEntity.ok(blocks);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an access block")
    public ResponseEntity<AccessBlockDto> deactivateAccessBlock(
            @PathVariable UUID id,
            @Valid @RequestBody DeactivateAccessBlockRequest request) {
        AccessBlockDto block = accessBlockService.deactivateAccessBlock(id, request);
        return ResponseEntity.ok(block);
    }

    @PostMapping("/check-access")
    @Operation(summary = "Check if access is blocked for a patient")
    public ResponseEntity<AccessCheckResult> checkAccess(@Valid @RequestBody CheckAccessRequest request) {
        AccessCheckResult result = accessBlockService.checkAccess(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/patient/{patientId}/unit/{unitId}/blocked")
    @Operation(summary = "Check if unit is blocked for patient")
    public ResponseEntity<Boolean> isUnitBlocked(
            @PathVariable UUID patientId,
            @PathVariable UUID unitId) {
        boolean blocked = accessBlockService.isUnitBlocked(patientId, unitId);
        return ResponseEntity.ok(blocked);
    }

    @GetMapping("/patient/{patientId}/practitioner/{practitionerId}/blocked")
    @Operation(summary = "Check if practitioner is blocked for patient")
    public ResponseEntity<Boolean> isPractitionerBlocked(
            @PathVariable UUID patientId,
            @PathVariable UUID practitionerId) {
        boolean blocked = accessBlockService.isPractitionerBlocked(patientId, practitionerId);
        return ResponseEntity.ok(blocked);
    }

    @GetMapping("/patient/{patientId}/count")
    @Operation(summary = "Count active blocks for patient")
    public ResponseEntity<Long> countActiveBlocks(@PathVariable UUID patientId) {
        long count = accessBlockService.countActiveBlocks(patientId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an access block (only deactivated)")
    public ResponseEntity<Void> deleteAccessBlock(@PathVariable UUID id) {
        accessBlockService.deleteAccessBlock(id);
        return ResponseEntity.noContent().build();
    }
}
