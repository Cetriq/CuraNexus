package se.curanexus.audit.domain;

/**
 * Types of resources that can be audited.
 */
public enum ResourceType {
    // Patient data
    PATIENT("Patient"),
    JOURNAL_ENTRY("Journalanteckning"),
    DIAGNOSIS("Diagnos"),
    OBSERVATION("Observation"),
    
    // Care contacts
    ENCOUNTER("Vårdkontakt"),
    TASK("Uppgift"),
    
    // Documents
    CERTIFICATE("Intyg"),
    FORM_SUBMISSION("Formulärinlämning"),
    FORM_TEMPLATE("Formulärmall"),
    
    // Consent
    CONSENT("Samtycke"),
    ACCESS_BLOCK("Spärr"),
    
    // Medications
    PRESCRIPTION("Ordination"),
    MEDICATION_LIST("Läkemedelslista"),
    
    // Lab & imaging
    LAB_ORDER("Labbbeställning"),
    LAB_RESULT("Labbsvar"),
    IMAGING_ORDER("Röntgenbeställning"),
    IMAGING_RESULT("Röntgensvar"),
    
    // Referrals
    REFERRAL("Remiss"),

    // Administration
    USER("Användare"),
    ROLE("Roll"),
    CARE_UNIT("Vårdenhet"),

    // Additional types for backwards compatibility
    CARE_ENCOUNTER("Vårdkontakt"),
    CARE_RELATION("Vårdrelation"),
    PERMISSION("Behörighet"),
    DOCUMENT("Dokument");

    private final String swedishName;

    ResourceType(String swedishName) {
        this.swedishName = swedishName;
    }

    public String getSwedishName() {
        return swedishName;
    }
}
