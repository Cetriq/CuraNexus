package se.curanexus.medication.adapter.fass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * DTO for package information from Fass API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FassPackage(
        @JsonProperty("nplPackId") String nplPackId,
        @JsonProperty("packageText") String packageText,
        @JsonProperty("packageSize") Integer packageSize,
        @JsonProperty("packageSizeUnit") String packageSizeUnit,
        @JsonProperty("packageType") String packageType,
        @JsonProperty("aipPrice") BigDecimal aipPrice,
        @JsonProperty("aupPrice") BigDecimal aupPrice,
        @JsonProperty("reimbursed") Boolean reimbursed,
        @JsonProperty("availabilityStatus") String availabilityStatus,
        @JsonProperty("limitedAvailability") Boolean limitedAvailability
) {
}
