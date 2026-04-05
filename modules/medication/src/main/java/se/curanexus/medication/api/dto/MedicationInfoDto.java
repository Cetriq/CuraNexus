package se.curanexus.medication.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for comprehensive medication information.
 * Combines local data with external data from Fass.
 */
public record MedicationInfoDto(
        UUID id,
        String nplId,
        String name,
        String genericName,
        String strength,
        String atcCode,
        String atcText,
        String manufacturer,
        String pharmaceuticalForm,
        boolean prescriptionRequired,
        boolean narcotic,
        String narcoticClass,
        boolean substitutable,
        String marketingStatus,
        List<String> activeSubstances,
        String indications,
        String contraindications,
        String warnings,
        String dataSource,
        boolean fassUnavailable,
        String fassLink
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String nplId;
        private String name;
        private String genericName;
        private String strength;
        private String atcCode;
        private String atcText;
        private String manufacturer;
        private String pharmaceuticalForm;
        private boolean prescriptionRequired;
        private boolean narcotic;
        private String narcoticClass;
        private boolean substitutable = true;
        private String marketingStatus;
        private List<String> activeSubstances;
        private String indications;
        private String contraindications;
        private String warnings;
        private String dataSource = "LOCAL";
        private boolean fassUnavailable = false;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder nplId(String nplId) {
            this.nplId = nplId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder genericName(String genericName) {
            this.genericName = genericName;
            return this;
        }

        public Builder strength(String strength) {
            this.strength = strength;
            return this;
        }

        public Builder atcCode(String atcCode) {
            this.atcCode = atcCode;
            return this;
        }

        public Builder atcText(String atcText) {
            this.atcText = atcText;
            return this;
        }

        public Builder manufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public Builder pharmaceuticalForm(String pharmaceuticalForm) {
            this.pharmaceuticalForm = pharmaceuticalForm;
            return this;
        }

        public Builder prescriptionRequired(boolean prescriptionRequired) {
            this.prescriptionRequired = prescriptionRequired;
            return this;
        }

        public Builder narcotic(boolean narcotic) {
            this.narcotic = narcotic;
            return this;
        }

        public Builder narcoticClass(String narcoticClass) {
            this.narcoticClass = narcoticClass;
            return this;
        }

        public Builder substitutable(boolean substitutable) {
            this.substitutable = substitutable;
            return this;
        }

        public Builder marketingStatus(String marketingStatus) {
            this.marketingStatus = marketingStatus;
            return this;
        }

        public Builder activeSubstances(List<String> activeSubstances) {
            this.activeSubstances = activeSubstances;
            return this;
        }

        public Builder indications(String indications) {
            this.indications = indications;
            return this;
        }

        public Builder contraindications(String contraindications) {
            this.contraindications = contraindications;
            return this;
        }

        public Builder warnings(String warnings) {
            this.warnings = warnings;
            return this;
        }

        public Builder dataSource(String dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder fassUnavailable(boolean fassUnavailable) {
            this.fassUnavailable = fassUnavailable;
            return this;
        }

        public MedicationInfoDto build() {
            String fassLink = nplId != null
                    ? "https://fass.se/LIF/product?nplId=" + nplId
                    : null;

            return new MedicationInfoDto(
                    id,
                    nplId,
                    name,
                    genericName,
                    strength,
                    atcCode,
                    atcText,
                    manufacturer,
                    pharmaceuticalForm,
                    prescriptionRequired,
                    narcotic,
                    narcoticClass,
                    substitutable,
                    marketingStatus,
                    activeSubstances,
                    indications,
                    contraindications,
                    warnings,
                    dataSource,
                    fassUnavailable,
                    fassLink
            );
        }
    }
}
