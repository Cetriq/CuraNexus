package se.curanexus.integration.fhir;

/**
 * Constants for Swedish FHIR extensions and identifiers.
 * Based on Swedish National e-Health specifications and Inera standards.
 */
public final class SwedishFhirExtensions {

    private SwedishFhirExtensions() {}

    // ========== Identifier Systems (OIDs) ==========

    /**
     * Swedish personal identity number (personnummer).
     * OID: 1.2.752.129.2.1.3.1
     */
    public static final String PERSONNUMMER_SYSTEM = "urn:oid:1.2.752.129.2.1.3.1";

    /**
     * Swedish coordination number (samordningsnummer).
     * OID: 1.2.752.129.2.1.3.3
     */
    public static final String SAMORDNINGSNUMMER_SYSTEM = "urn:oid:1.2.752.129.2.1.3.3";

    /**
     * HSA-ID (Healthcare Service Actor identifier).
     * Used for healthcare personnel and organizations in Sweden.
     * OID: 1.2.752.129.2.1.4.1
     */
    public static final String HSA_ID_SYSTEM = "urn:oid:1.2.752.129.2.1.4.1";

    /**
     * Swedish organizational number (organisationsnummer).
     * OID: 1.2.752.129.2.1.4.3
     */
    public static final String ORGANISATIONSNUMMER_SYSTEM = "urn:oid:1.2.752.129.2.1.4.3";

    /**
     * Swedish workplace code (arbetsplatskod).
     * OID: 1.2.752.29.4.19
     */
    public static final String ARBETSPLATSKOD_SYSTEM = "urn:oid:1.2.752.29.4.19";

    // ========== Extension URLs ==========

    /**
     * Extension for protected identity (sekretessmarkering).
     */
    public static final String PROTECTED_IDENTITY_URL = "http://curanexus.se/fhir/StructureDefinition/protected-identity";

    /**
     * Extension for deceased status with additional Swedish requirements.
     */
    public static final String DECEASED_INFO_URL = "http://curanexus.se/fhir/StructureDefinition/deceased-info";

    /**
     * Extension for responsible HSA-ID on encounter.
     */
    public static final String RESPONSIBLE_HSA_ID_URL = "http://curanexus.se/fhir/StructureDefinition/responsible-hsa-id";

    /**
     * Extension for responsible unit HSA-ID on encounter.
     */
    public static final String RESPONSIBLE_UNIT_HSA_ID_URL = "http://curanexus.se/fhir/StructureDefinition/responsible-unit-hsa-id";

    /**
     * Extension for Swedish triage level (RETTS).
     */
    public static final String TRIAGE_LEVEL_URL = "http://curanexus.se/fhir/StructureDefinition/triage-level";

    // ========== Code Systems ==========

    /**
     * Swedish medical procedure codes (KVÅ - Klassifikation av vårdåtgärder).
     * OID: 1.2.752.116.1.3.2.1.4
     */
    public static final String KVA_SYSTEM = "urn:oid:1.2.752.116.1.3.2.1.4";

    /**
     * ICD-10-SE (Swedish version of ICD-10).
     * OID: 1.2.752.116.1.1.1.1.3
     */
    public static final String ICD10_SE_SYSTEM = "urn:oid:1.2.752.116.1.1.1.1.3";

    /**
     * Swedish drug codes (NPL - Nationellt Produkt Läkemedel).
     */
    public static final String NPL_SYSTEM = "http://snomed.info/sct";

    /**
     * SNOMED CT (Swedish edition).
     */
    public static final String SNOMED_CT_SWEDEN = "http://snomed.info/sct/45991000052106";

    /**
     * RETTS triage system (Rapid Emergency Triage and Treatment System).
     */
    public static final String RETTS_SYSTEM = "http://curanexus.se/fhir/CodeSystem/retts-triage";

    // ========== Profile URLs ==========

    /**
     * Swedish base patient profile (based on Swedish national guidelines).
     */
    public static final String SWEDISH_PATIENT_PROFILE = "http://curanexus.se/fhir/StructureDefinition/se-patient";

    /**
     * Swedish encounter profile.
     */
    public static final String SWEDISH_ENCOUNTER_PROFILE = "http://curanexus.se/fhir/StructureDefinition/se-encounter";

    /**
     * Swedish practitioner profile.
     */
    public static final String SWEDISH_PRACTITIONER_PROFILE = "http://curanexus.se/fhir/StructureDefinition/se-practitioner";

    /**
     * Swedish organization profile.
     */
    public static final String SWEDISH_ORGANIZATION_PROFILE = "http://curanexus.se/fhir/StructureDefinition/se-organization";
}
