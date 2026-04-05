package se.curanexus.medication.adapter.fass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for Fass product document (SMPC/PIL).
 * SMPC = Summary of Product Characteristics (produktresume)
 * PIL = Patient Information Leaflet (bipacksedel)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FassProductDocument(
        @JsonProperty("nplId") String nplId,
        @JsonProperty("documentType") String documentType,
        @JsonProperty("sections") List<FassDocumentSection> sections,
        @JsonProperty("lastUpdated") String lastUpdated,
        @JsonProperty("approvalDate") String approvalDate
) {
}
