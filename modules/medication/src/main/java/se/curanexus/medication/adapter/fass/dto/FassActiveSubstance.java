package se.curanexus.medication.adapter.fass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for active substance from Fass API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FassActiveSubstance(
        @JsonProperty("substanceId") String substanceId,
        @JsonProperty("substanceName") String substanceName,
        @JsonProperty("strengthText") String strengthText,
        @JsonProperty("strengthNumeric") Double strengthNumeric,
        @JsonProperty("strengthUnit") String strengthUnit
) {
}
