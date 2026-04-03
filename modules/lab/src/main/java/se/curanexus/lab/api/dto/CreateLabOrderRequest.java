package se.curanexus.lab.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.lab.domain.LabOrderPriority;

import java.util.List;
import java.util.UUID;

public record CreateLabOrderRequest(
        @NotNull UUID patientId,
        String patientPersonnummer,
        String patientName,
        LabOrderPriority priority,
        String orderingUnitHsaId,
        String orderingUnitName,
        String orderingPractitionerHsaId,
        String orderingPractitionerName,
        UUID performingLabId,
        String performingLabHsaId,
        String performingLabName,
        String clinicalIndication,
        String diagnosisCode,
        String diagnosisText,
        String relevantMedication,
        Boolean fastingRequired,
        String labComment,
        UUID encounterId,
        UUID referralId,
        List<LabTestRequest> tests,
        boolean sendImmediately
) {
}
