package se.curanexus.authorization.api.dto;

import java.util.Map;

public record AccessCheckResponse(
        boolean granted,
        String reason,
        Map<String, Boolean> permissionResults
) {
    public static AccessCheckResponse accessGranted() {
        return new AccessCheckResponse(true, null, null);
    }

    public static AccessCheckResponse denied(String reason) {
        return new AccessCheckResponse(false, reason, null);
    }

    public static AccessCheckResponse withPermissions(boolean granted, Map<String, Boolean> permissionResults) {
        return new AccessCheckResponse(granted, null, permissionResults);
    }
}
