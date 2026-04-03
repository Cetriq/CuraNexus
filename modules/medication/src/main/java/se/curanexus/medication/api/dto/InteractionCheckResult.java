package se.curanexus.medication.api.dto;

import se.curanexus.medication.domain.MedicationInteraction;
import se.curanexus.medication.domain.MedicationInteraction.InteractionSeverity;

import java.util.List;
import java.util.UUID;

/**
 * Resultat från interaktionskontroll.
 */
public record InteractionCheckResult(
        boolean hasInteractions,
        boolean hasContraindications,
        boolean hasSevereInteractions,
        int totalInteractions,
        List<InteractionWarning> warnings
) {
    public static InteractionCheckResult noInteractions() {
        return new InteractionCheckResult(false, false, false, 0, List.of());
    }

    public static InteractionCheckResult from(List<MedicationInteraction> interactions) {
        if (interactions.isEmpty()) {
            return noInteractions();
        }

        List<InteractionWarning> warnings = interactions.stream()
                .map(InteractionWarning::from)
                .toList();

        boolean hasContra = interactions.stream()
                .anyMatch(i -> i.getSeverity() == InteractionSeverity.CONTRAINDICATED);
        boolean hasSevere = interactions.stream()
                .anyMatch(i -> i.getSeverity() == InteractionSeverity.SEVERE);

        return new InteractionCheckResult(true, hasContra, hasSevere, interactions.size(), warnings);
    }

    public record InteractionWarning(
            UUID id,
            String atcCode1,
            String atcCode2,
            InteractionSeverity severity,
            String description,
            String clinicalEffect,
            String recommendation,
            String evidenceLevel,
            String source
    ) {
        public static InteractionWarning from(MedicationInteraction mi) {
            return new InteractionWarning(
                    mi.getId(),
                    mi.getAtcCode1(),
                    mi.getAtcCode2(),
                    mi.getSeverity(),
                    mi.getDescription(),
                    mi.getClinicalEffect(),
                    mi.getRecommendation(),
                    mi.getEvidenceLevel(),
                    mi.getSource()
            );
        }
    }
}
