package se.curanexus.authorization.service;

public class SystemRoleModificationException extends RuntimeException {

    public SystemRoleModificationException(String roleCode) {
        super("Cannot modify or delete system role: " + roleCode);
    }
}
