package se.curanexus.medication.adapter.fass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for Fass API search results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FassSearchResult(
        @JsonProperty("totalHits") Integer totalHits,
        @JsonProperty("products") List<FassMedicinalProduct> products
) {
}
