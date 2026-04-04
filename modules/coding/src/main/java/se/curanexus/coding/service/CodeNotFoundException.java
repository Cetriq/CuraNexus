package se.curanexus.coding.service;

public class CodeNotFoundException extends RuntimeException {

    public CodeNotFoundException(String codeType, String code) {
        super(String.format("%s code not found: %s", codeType, code));
    }
}
