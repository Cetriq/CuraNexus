package se.curanexus.medication.adapter.fass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for ATC classification from Fass API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FassAtcCode(
        @JsonProperty("code") String code,
        @JsonProperty("name") String name,
        @JsonProperty("level") Integer level,
        @JsonProperty("parentCode") String parentCode,
        @JsonProperty("children") List<FassAtcCode> children
) {
}
