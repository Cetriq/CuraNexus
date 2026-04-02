package se.curanexus.patient.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PatientTest {

    @Test
    void constructor_shouldExtractDateOfBirthFromPersonnummer() {
        // Given & When
        Patient patient = new Patient("199001151234", "Test", "Testsson");

        // Then
        assertThat(patient.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 15));
    }

    @Test
    void constructor_shouldExtractMaleGenderFromOddDigit() {
        // Given & When (digit at position 10 is 3 - odd = male)
        Patient patient = new Patient("199001151234", "Test", "Testsson");

        // Then
        assertThat(patient.getGender()).isEqualTo(Gender.MALE);
    }

    @Test
    void constructor_shouldExtractFemaleGenderFromEvenDigit() {
        // Given & When (digit at position 10 is 2 - even = female)
        Patient patient = new Patient("199001151224", "Test", "Testsson");

        // Then
        assertThat(patient.getGender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    void addContact_shouldAddContactToList() {
        // Given
        Patient patient = new Patient("199001151234", "Test", "Testsson");
        ContactInfo contact = new ContactInfo(ContactType.EMAIL, "test@example.com");

        // When
        patient.addContact(contact);

        // Then
        assertThat(patient.getContacts()).contains(contact);
        assertThat(contact.getPatient()).isEqualTo(patient);
    }

    @Test
    void removeContact_shouldRemoveContactFromList() {
        // Given
        Patient patient = new Patient("199001151234", "Test", "Testsson");
        ContactInfo contact = new ContactInfo(ContactType.EMAIL, "test@example.com");
        patient.addContact(contact);

        // When
        patient.removeContact(contact);

        // Then
        assertThat(patient.getContacts()).doesNotContain(contact);
        assertThat(contact.getPatient()).isNull();
    }

    @Test
    void addRelatedPerson_shouldAddRelatedPersonToList() {
        // Given
        Patient patient = new Patient("199001151234", "Test", "Testsson");
        RelatedPerson relatedPerson = new RelatedPerson(RelationshipType.SPOUSE, "Anna", "Testsson");

        // When
        patient.addRelatedPerson(relatedPerson);

        // Then
        assertThat(patient.getRelatedPersons()).contains(relatedPerson);
        assertThat(relatedPerson.getPatient()).isEqualTo(patient);
    }

    @Test
    void addConsent_shouldAddConsentToList() {
        // Given
        Patient patient = new Patient("199001151234", "Test", "Testsson");
        Consent consent = new Consent(ConsentType.DATA_SHARING);

        // When
        patient.addConsent(consent);

        // Then
        assertThat(patient.getConsents()).contains(consent);
        assertThat(consent.getPatient()).isEqualTo(patient);
    }

    @Test
    void setProtectedIdentity_shouldUpdateFlag() {
        // Given
        Patient patient = new Patient("199001151234", "Test", "Testsson");

        // When
        patient.setProtectedIdentity(true);

        // Then
        assertThat(patient.isProtectedIdentity()).isTrue();
    }

    @Test
    void setDeceased_shouldUpdateFlagAndDate() {
        // Given
        Patient patient = new Patient("199001151234", "Test", "Testsson");
        LocalDate deceasedDate = LocalDate.of(2024, 3, 15);

        // When
        patient.setDeceased(true);
        patient.setDeceasedDate(deceasedDate);

        // Then
        assertThat(patient.isDeceased()).isTrue();
        assertThat(patient.getDeceasedDate()).isEqualTo(deceasedDate);
    }
}
