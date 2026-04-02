package se.curanexus.patient.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_personal_identity_number", columnList = "personal_identity_number", unique = true)
})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "personal_identity_number", nullable = false, unique = true, length = 12)
    private String personalIdentityNumber;

    @Column(name = "given_name", nullable = false, length = 100)
    private String givenName;

    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "protected_identity", nullable = false)
    private boolean protectedIdentity = false;

    @Column(name = "deceased", nullable = false)
    private boolean deceased = false;

    @Column(name = "deceased_date")
    private LocalDate deceasedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactInfo> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelatedPerson> relatedPersons = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consent> consents = new ArrayList<>();

    protected Patient() {
    }

    public Patient(String personalIdentityNumber, String givenName, String familyName) {
        this.personalIdentityNumber = personalIdentityNumber;
        this.givenName = givenName;
        this.familyName = familyName;
        this.dateOfBirth = extractDateOfBirth(personalIdentityNumber);
        this.gender = extractGender(personalIdentityNumber);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private static LocalDate extractDateOfBirth(String personnummer) {
        if (personnummer == null || personnummer.length() != 12) {
            return null;
        }
        int year = Integer.parseInt(personnummer.substring(0, 4));
        int month = Integer.parseInt(personnummer.substring(4, 6));
        int day = Integer.parseInt(personnummer.substring(6, 8));
        return LocalDate.of(year, month, day);
    }

    private static Gender extractGender(String personnummer) {
        if (personnummer == null || personnummer.length() != 12) {
            return Gender.UNKNOWN;
        }
        int genderDigit = Character.getNumericValue(personnummer.charAt(10));
        return (genderDigit % 2 == 0) ? Gender.FEMALE : Gender.MALE;
    }

    public void addContact(ContactInfo contact) {
        contacts.add(contact);
        contact.setPatient(this);
    }

    public void removeContact(ContactInfo contact) {
        contacts.remove(contact);
        contact.setPatient(null);
    }

    public void addRelatedPerson(RelatedPerson relatedPerson) {
        relatedPersons.add(relatedPerson);
        relatedPerson.setPatient(this);
    }

    public void removeRelatedPerson(RelatedPerson relatedPerson) {
        relatedPersons.remove(relatedPerson);
        relatedPerson.setPatient(null);
    }

    public void addConsent(Consent consent) {
        consents.add(consent);
        consent.setPatient(this);
    }

    public void removeConsent(Consent consent) {
        consents.remove(consent);
        consent.setPatient(null);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getPersonalIdentityNumber() {
        return personalIdentityNumber;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean isProtectedIdentity() {
        return protectedIdentity;
    }

    public void setProtectedIdentity(boolean protectedIdentity) {
        this.protectedIdentity = protectedIdentity;
    }

    public boolean isDeceased() {
        return deceased;
    }

    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
    }

    public LocalDate getDeceasedDate() {
        return deceasedDate;
    }

    public void setDeceasedDate(LocalDate deceasedDate) {
        this.deceasedDate = deceasedDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<ContactInfo> getContacts() {
        return contacts;
    }

    public List<RelatedPerson> getRelatedPersons() {
        return relatedPersons;
    }

    public List<Consent> getConsents() {
        return consents;
    }
}
