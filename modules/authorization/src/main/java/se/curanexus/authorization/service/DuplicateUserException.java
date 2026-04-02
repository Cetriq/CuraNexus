package se.curanexus.authorization.service;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String field, String value) {
        super("User with " + field + " '" + value + "' already exists");
    }
}
