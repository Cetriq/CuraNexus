package se.curanexus.authorization.service;

import java.util.UUID;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(UUID permissionId) {
        super("Permission not found: " + permissionId);
    }

    public PermissionNotFoundException(String code) {
        super("Permission not found: " + code);
    }
}
