package se.curanexus.authorization.service;

import java.util.UUID;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(UUID roleId) {
        super("Role not found: " + roleId);
    }

    public RoleNotFoundException(String code) {
        super("Role not found: " + code);
    }
}
