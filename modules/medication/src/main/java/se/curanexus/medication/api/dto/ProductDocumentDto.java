package se.curanexus.medication.api.dto;

import java.util.List;

/**
 * DTO for product documents from Fass (SMPC/PIL).
 */
public record ProductDocumentDto(
        String nplId,
        String documentType,
        List<Section> sections,
        String lastUpdated,
        String approvalDate
) {

    /**
     * A section in the product document.
     */
    public record Section(
            String sectionNumber,
            String sectionTitle,
            String content,
            String htmlContent
    ) {
    }

    /**
     * Common SMPC section numbers for reference.
     */
    public static class SmpcSections {
        public static final String INDICATIONS = "4.1";
        public static final String DOSAGE = "4.2";
        public static final String CONTRAINDICATIONS = "4.3";
        public static final String WARNINGS = "4.4";
        public static final String INTERACTIONS = "4.5";
        public static final String PREGNANCY = "4.6";
        public static final String DRIVING = "4.7";
        public static final String ADVERSE_REACTIONS = "4.8";
        public static final String OVERDOSE = "4.9";

        private SmpcSections() {
        }
    }
}
