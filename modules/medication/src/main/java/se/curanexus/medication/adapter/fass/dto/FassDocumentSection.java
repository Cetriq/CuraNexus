package se.curanexus.medication.adapter.fass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a section in Fass product document.
 *
 * Common sections in SMPC:
 * - 4.1 Terapeutiska indikationer
 * - 4.2 Dosering och administreringssatt
 * - 4.3 Kontraindikationer
 * - 4.4 Varningar och forsiktighet
 * - 4.5 Interaktioner
 * - 4.6 Fertilitet, graviditet och amning
 * - 4.8 Biverkningar
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FassDocumentSection(
        @JsonProperty("sectionId") String sectionId,
        @JsonProperty("sectionNumber") String sectionNumber,
        @JsonProperty("sectionTitle") String sectionTitle,
        @JsonProperty("content") String content,
        @JsonProperty("htmlContent") String htmlContent
) {
}
