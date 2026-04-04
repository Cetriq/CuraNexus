package se.curanexus.audit.domain;

/**
 * Types of auditable actions in the system.
 * Based on PDL requirements for healthcare data access logging.
 */
public enum AuditAction {
    // Read operations
    READ("Läsning"),
    SEARCH("Sökning"),
    EXPORT("Export"),
    PRINT("Utskrift"),
    
    // Write operations
    CREATE("Skapande"),
    UPDATE("Uppdatering"),
    DELETE("Radering"),
    
    // Workflow operations
    SIGN("Signering"),
    SEND("Sändning"),
    REVOKE("Makulering"),
    
    // Access control
    LOGIN("Inloggning"),
    LOGOUT("Utloggning"),
    ACCESS_DENIED("Nekad åtkomst"),
    EMERGENCY_ACCESS("Nödöppning"),
    
    // Consent operations
    CONSENT_GIVEN("Samtycke givet"),
    CONSENT_WITHDRAWN("Samtycke återkallat"),
    ACCESS_BLOCK_CREATED("Spärr skapad"),
    ACCESS_BLOCK_REMOVED("Spärr borttagen");

    private final String swedishName;

    AuditAction(String swedishName) {
        this.swedishName = swedishName;
    }

    public String getSwedishName() {
        return swedishName;
    }
}
