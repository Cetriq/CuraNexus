package se.curanexus.authorization.service;

public class DuplicateRoleException extends RuntimeException {

    public DuplicateRoleException(String code) {
        super("Role with code '" + code + "' already exists");
    }
}
