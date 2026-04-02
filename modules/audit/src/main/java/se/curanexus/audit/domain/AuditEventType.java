package se.curanexus.audit.domain;

public enum AuditEventType {
    ACCESS,
    CREATE,
    READ,
    UPDATE,
    DELETE,
    SIGN,
    EXPORT,
    PRINT,
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PERMISSION_DENIED,
    EMERGENCY_ACCESS,
    SEARCH,
    ACCESS_DENIED
}
