package se.curanexus.consent.api.dto;

import java.util.List;
import java.util.UUID;

public record AccessCheckResult(
        UUID patientId,
        boolean accessAllowed,
        boolean unitBlocked,
        boolean practitionerBlocked,
        boolean dataCategoryBlocked,
        List<AccessBlockSummaryDto> activeBlocks,
        String message
) {
    public static AccessCheckResult allowed(UUID patientId) {
        return new AccessCheckResult(
                patientId,
                true,
                false,
                false,
                false,
                List.of(),
                "Access allowed"
        );
    }

    public static AccessCheckResult blocked(UUID patientId, boolean unitBlocked, boolean practitionerBlocked,
                                            boolean dataCategoryBlocked, List<AccessBlockSummaryDto> activeBlocks) {
        return new AccessCheckResult(
                patientId,
                false,
                unitBlocked,
                practitionerBlocked,
                dataCategoryBlocked,
                activeBlocks,
                "Access blocked by patient restriction (spärr)"
        );
    }
}
