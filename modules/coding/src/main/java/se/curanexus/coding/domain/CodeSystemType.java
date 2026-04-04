package se.curanexus.coding.domain;

/**
 * Types of code systems supported in Swedish healthcare.
 */
public enum CodeSystemType {
    ICD10_SE("ICD-10-SE", "Internationell klassifikation av sjukdomar", "http://hl7.org/fhir/sid/icd-10-se"),
    KVA("KVÅ", "Klassifikation av vårdåtgärder", "urn:oid:1.2.752.116.1.3.2.1.4"),
    ATC("ATC", "Anatomical Therapeutic Chemical Classification", "http://www.whocc.no/atc"),
    ICF("ICF", "Internationell klassifikation av funktionstillstånd", "http://hl7.org/fhir/sid/icf"),
    SNOMED_CT("SNOMED CT", "Systematized Nomenclature of Medicine", "http://snomed.info/sct"),
    NPU("NPU", "Nomenclature for Properties and Units", "urn:oid:1.2.752.108.1");

    private final String displayName;
    private final String description;
    private final String systemUri;

    CodeSystemType(String displayName, String description, String systemUri) {
        this.displayName = displayName;
        this.description = description;
        this.systemUri = systemUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getSystemUri() {
        return systemUri;
    }
}
