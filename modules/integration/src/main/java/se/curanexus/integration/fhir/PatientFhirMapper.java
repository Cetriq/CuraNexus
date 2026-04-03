package se.curanexus.integration.fhir;

import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static se.curanexus.integration.fhir.SwedishFhirExtensions.*;

/**
 * Maps Patient domain objects to FHIR R4 Patient resources.
 * Implements Swedish national requirements including:
 * - Personnummer as primary identifier
 * - Protected identity extension (sekretessmarkering)
 * - Swedish address format
 */
@Component
public class PatientFhirMapper {

    /**
     * Convert internal patient representation to FHIR Patient.
     */
    public Patient toFhir(PatientData patient) {
        Patient fhirPatient = new Patient();

        // Meta information
        fhirPatient.getMeta()
                .addProfile(SWEDISH_PATIENT_PROFILE)
                .setVersionId("1");

        // ID
        fhirPatient.setId(patient.id().toString());

        // Identifiers
        addPersonnummer(fhirPatient, patient.personalIdentityNumber());

        // Name
        addName(fhirPatient, patient.givenName(), patient.middleName(), patient.familyName());

        // Gender
        if (patient.gender() != null) {
            fhirPatient.setGender(mapGender(patient.gender()));
        }

        // Birth date
        if (patient.dateOfBirth() != null) {
            fhirPatient.setBirthDate(toDate(patient.dateOfBirth()));
        }

        // Deceased
        if (patient.deceased()) {
            if (patient.deceasedDate() != null) {
                fhirPatient.setDeceased(new DateTimeType(toDate(patient.deceasedDate())));
            } else {
                fhirPatient.setDeceased(new BooleanType(true));
            }
        }

        // Protected identity extension (sekretessmarkering)
        if (patient.protectedIdentity()) {
            fhirPatient.addExtension()
                    .setUrl(PROTECTED_IDENTITY_URL)
                    .setValue(new BooleanType(true));
        }

        // Active status
        fhirPatient.setActive(!patient.deceased());

        // Telecom (contact info)
        if (patient.phoneNumber() != null) {
            fhirPatient.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(patient.phoneNumber())
                    .setUse(ContactPoint.ContactPointUse.MOBILE);
        }
        if (patient.email() != null) {
            fhirPatient.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(patient.email())
                    .setUse(ContactPoint.ContactPointUse.HOME);
        }

        // Address
        if (patient.streetAddress() != null || patient.city() != null) {
            addAddress(fhirPatient, patient);
        }

        return fhirPatient;
    }

    private void addPersonnummer(Patient fhirPatient, String personnummer) {
        if (personnummer == null) return;

        // Determine if it's a personnummer or samordningsnummer
        String system = PERSONNUMMER_SYSTEM;
        if (personnummer.length() == 12) {
            int day = Integer.parseInt(personnummer.substring(6, 8));
            if (day > 60) {
                // Samordningsnummer has day + 60
                system = SAMORDNINGSNUMMER_SYSTEM;
            }
        }

        fhirPatient.addIdentifier()
                .setSystem(system)
                .setValue(personnummer)
                .setUse(Identifier.IdentifierUse.OFFICIAL)
                .setType(new CodeableConcept()
                        .addCoding(new Coding()
                                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                                .setCode("NI")
                                .setDisplay("National unique individual identifier")));
    }

    private void addName(Patient fhirPatient, String givenName, String middleName, String familyName) {
        HumanName name = fhirPatient.addName();
        name.setUse(HumanName.NameUse.OFFICIAL);

        if (givenName != null) {
            name.addGiven(givenName);
        }
        if (middleName != null) {
            name.addGiven(middleName);
        }
        if (familyName != null) {
            name.setFamily(familyName);
        }

        // Construct text representation
        StringBuilder text = new StringBuilder();
        if (givenName != null) text.append(givenName);
        if (middleName != null) text.append(" ").append(middleName);
        if (familyName != null) text.append(" ").append(familyName);
        name.setText(text.toString().trim());
    }

    private void addAddress(Patient fhirPatient, PatientData patient) {
        Address address = fhirPatient.addAddress();
        address.setUse(Address.AddressUse.HOME);
        address.setType(Address.AddressType.BOTH);

        if (patient.streetAddress() != null) {
            address.addLine(patient.streetAddress());
        }
        if (patient.city() != null) {
            address.setCity(patient.city());
        }
        if (patient.postalCode() != null) {
            address.setPostalCode(patient.postalCode());
        }
        if (patient.municipality() != null) {
            address.setDistrict(patient.municipality());
        }
        address.setCountry("SE");
    }

    private Enumerations.AdministrativeGender mapGender(String gender) {
        return switch (gender.toUpperCase()) {
            case "MALE" -> Enumerations.AdministrativeGender.MALE;
            case "FEMALE" -> Enumerations.AdministrativeGender.FEMALE;
            case "OTHER" -> Enumerations.AdministrativeGender.OTHER;
            default -> Enumerations.AdministrativeGender.UNKNOWN;
        };
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Data transfer record for patient data from internal systems.
     */
    public record PatientData(
            java.util.UUID id,
            String personalIdentityNumber,
            String givenName,
            String middleName,
            String familyName,
            String gender,
            LocalDate dateOfBirth,
            boolean protectedIdentity,
            boolean deceased,
            LocalDate deceasedDate,
            String phoneNumber,
            String email,
            String streetAddress,
            String city,
            String postalCode,
            String municipality
    ) {}
}
