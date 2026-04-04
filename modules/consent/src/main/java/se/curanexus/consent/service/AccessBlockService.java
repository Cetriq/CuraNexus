package se.curanexus.consent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.consent.api.dto.*;
import se.curanexus.consent.domain.AccessBlock;
import se.curanexus.consent.domain.AccessBlockType;
import se.curanexus.consent.exception.AccessBlockNotFoundException;
import se.curanexus.consent.repository.AccessBlockRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccessBlockService {

    private final AccessBlockRepository accessBlockRepository;

    public AccessBlockService(AccessBlockRepository accessBlockRepository) {
        this.accessBlockRepository = accessBlockRepository;
    }

    public AccessBlockDto createAccessBlock(CreateAccessBlockRequest request) {
        validateAccessBlockRequest(request);

        AccessBlock block = new AccessBlock(request.patientId(), request.blockType());
        block.setBlockedUnitId(request.blockedUnitId());
        block.setBlockedUnitName(request.blockedUnitName());
        block.setBlockedPractitionerId(request.blockedPractitionerId());
        block.setBlockedPractitionerName(request.blockedPractitionerName());
        block.setBlockedDataCategory(request.blockedDataCategory());
        block.setReason(request.reason());
        block.setValidFrom(request.validFrom());
        block.setValidUntil(request.validUntil());
        block.setRequestedBy(request.requestedBy());
        block.setRequestedByName(request.requestedByName());

        AccessBlock saved = accessBlockRepository.save(block);
        return AccessBlockDto.fromEntity(saved);
    }

    private void validateAccessBlockRequest(CreateAccessBlockRequest request) {
        switch (request.blockType()) {
            case UNIT -> {
                if (request.blockedUnitId() == null) {
                    throw new IllegalArgumentException("Blocked unit ID is required for UNIT block type");
                }
            }
            case PRACTITIONER -> {
                if (request.blockedPractitionerId() == null) {
                    throw new IllegalArgumentException("Blocked practitioner ID is required for PRACTITIONER block type");
                }
            }
            case DATA_CATEGORY -> {
                if (request.blockedDataCategory() == null || request.blockedDataCategory().isBlank()) {
                    throw new IllegalArgumentException("Blocked data category is required for DATA_CATEGORY block type");
                }
            }
            case EXTERNAL_UNITS, EMERGENCY_OVERRIDE -> {
                // No specific validation needed
            }
        }
    }

    @Transactional(readOnly = true)
    public AccessBlockDto getAccessBlock(UUID id) {
        AccessBlock block = accessBlockRepository.findById(id)
                .orElseThrow(() -> new AccessBlockNotFoundException(id));
        return AccessBlockDto.fromEntity(block);
    }

    @Transactional(readOnly = true)
    public List<AccessBlockSummaryDto> getPatientAccessBlocks(UUID patientId) {
        return accessBlockRepository.findByPatientId(patientId).stream()
                .map(AccessBlockSummaryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccessBlockSummaryDto> getActiveAccessBlocks(UUID patientId) {
        return accessBlockRepository.findActiveBlocksForPatient(patientId).stream()
                .map(AccessBlockSummaryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccessBlockSummaryDto> getAccessBlocksByType(UUID patientId, AccessBlockType blockType) {
        return accessBlockRepository.findByPatientIdAndBlockType(patientId, blockType).stream()
                .map(AccessBlockSummaryDto::fromEntity)
                .toList();
    }

    public AccessBlockDto deactivateAccessBlock(UUID id, DeactivateAccessBlockRequest request) {
        AccessBlock block = accessBlockRepository.findById(id)
                .orElseThrow(() -> new AccessBlockNotFoundException(id));

        if (!block.isActive()) {
            throw new IllegalStateException("Access block is already deactivated");
        }

        block.deactivate(request.deactivatedBy(), request.reason());

        AccessBlock saved = accessBlockRepository.save(block);
        return AccessBlockDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public AccessCheckResult checkAccess(CheckAccessRequest request) {
        List<AccessBlockSummaryDto> activeBlocks = new ArrayList<>();
        boolean unitBlocked = false;
        boolean practitionerBlocked = false;
        boolean dataCategoryBlocked = false;

        // Check unit block
        if (request.unitId() != null) {
            unitBlocked = accessBlockRepository.isUnitBlockedForPatient(request.patientId(), request.unitId());
            if (unitBlocked) {
                activeBlocks.addAll(
                        accessBlockRepository.findActiveBlocksForPatientAndUnit(request.patientId(), request.unitId())
                                .stream()
                                .map(AccessBlockSummaryDto::fromEntity)
                                .toList()
                );
            }
        }

        // Check practitioner block
        if (request.practitionerId() != null) {
            practitionerBlocked = accessBlockRepository.isPractitionerBlockedForPatient(
                    request.patientId(), request.practitionerId());
            if (practitionerBlocked) {
                activeBlocks.addAll(
                        accessBlockRepository.findActiveBlocksForPatientAndPractitioner(
                                        request.patientId(), request.practitionerId())
                                .stream()
                                .map(AccessBlockSummaryDto::fromEntity)
                                .toList()
                );
            }
        }

        // Check data category block
        if (request.dataCategory() != null && !request.dataCategory().isBlank()) {
            List<AccessBlock> categoryBlocks = accessBlockRepository
                    .findByPatientIdAndBlockTypeAndActive(request.patientId(), AccessBlockType.DATA_CATEGORY, true);
            for (AccessBlock block : categoryBlocks) {
                if (block.isCurrentlyActive() &&
                        request.dataCategory().equalsIgnoreCase(block.getBlockedDataCategory())) {
                    dataCategoryBlocked = true;
                    activeBlocks.add(AccessBlockSummaryDto.fromEntity(block));
                }
            }
        }

        // Check external units block
        if (request.unitId() != null) {
            List<AccessBlock> externalBlocks = accessBlockRepository
                    .findByPatientIdAndBlockTypeAndActive(request.patientId(), AccessBlockType.EXTERNAL_UNITS, true);
            for (AccessBlock block : externalBlocks) {
                if (block.isCurrentlyActive()) {
                    unitBlocked = true;
                    activeBlocks.add(AccessBlockSummaryDto.fromEntity(block));
                }
            }
        }

        if (unitBlocked || practitionerBlocked || dataCategoryBlocked) {
            return AccessCheckResult.blocked(request.patientId(), unitBlocked, practitionerBlocked,
                    dataCategoryBlocked, activeBlocks);
        }

        return AccessCheckResult.allowed(request.patientId());
    }

    @Transactional(readOnly = true)
    public boolean isUnitBlocked(UUID patientId, UUID unitId) {
        return accessBlockRepository.isUnitBlockedForPatient(patientId, unitId);
    }

    @Transactional(readOnly = true)
    public boolean isPractitionerBlocked(UUID patientId, UUID practitionerId) {
        return accessBlockRepository.isPractitionerBlockedForPatient(patientId, practitionerId);
    }

    @Transactional(readOnly = true)
    public long countActiveBlocks(UUID patientId) {
        return accessBlockRepository.countActiveBlocksForPatient(patientId);
    }

    public void deleteAccessBlock(UUID id) {
        AccessBlock block = accessBlockRepository.findById(id)
                .orElseThrow(() -> new AccessBlockNotFoundException(id));

        if (block.isActive()) {
            throw new IllegalStateException("Cannot delete active access block. Deactivate it first.");
        }

        accessBlockRepository.delete(block);
    }
}
