package se.curanexus.medication.adapter.fass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for Fass API MedicinalProduct response.
 * Maps to /medicinal-product endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FassMedicinalProduct(
        @JsonProperty("nplId") String nplId,
        @JsonProperty("name") String name,
        @JsonProperty("strengthText") String strengthText,
        @JsonProperty("strengthNumeric") Double strengthNumeric,
        @JsonProperty("strengthUnit") String strengthUnit,
        @JsonProperty("pharmaceuticalForm") String pharmaceuticalForm,
        @JsonProperty("atcCode") String atcCode,
        @JsonProperty("atcText") String atcText,
        @JsonProperty("marketingAuthorizationHolder") String marketingAuthorizationHolder,
        @JsonProperty("activeSubstances") List<FassActiveSubstance> activeSubstances,
        @JsonProperty("packages") List<FassPackage> packages,
        @JsonProperty("prescriptionRequired") Boolean prescriptionRequired,
        @JsonProperty("narcotic") Boolean narcotic,
        @JsonProperty("narcoticClass") String narcoticClass,
        @JsonProperty("humanMedicineOnly") Boolean humanMedicineOnly,
        @JsonProperty("substitutable") Boolean substitutable,
        @JsonProperty("marketingStatus") String marketingStatus,
        @JsonProperty("approved") Boolean approved,
        @JsonProperty("approvalDate") String approvalDate,
        @JsonProperty("parallelImport") Boolean parallelImport,
        @JsonProperty("exportCountry") String exportCountry
) {
}
