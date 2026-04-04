package se.curanexus.audit.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.audit.domain.DataChangeLog.ChangeType;
import se.curanexus.audit.domain.ResourceType;
import java.util.UUID;

public record DataChangeRequest(
    @NotNull ResourceType resourceType, @NotNull UUID resourceId, @NotBlank String fieldName,
    String oldValue, String newValue, @NotNull ChangeType changeType
) {}
