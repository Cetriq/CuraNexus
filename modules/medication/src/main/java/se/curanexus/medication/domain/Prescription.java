package se.curanexus.medication.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ordination/Recept - en ordination av läkemedel till patient.
 */
@Entity
@Table(name = "prescriptions", indexes = {
    @Index(name = "idx_prescription_patient", columnList = "patient_id"),
    @Index(name = "idx_prescription_prescriber", columnList = "prescriber_id"),
    @Index(name = "idx_prescription_encounter", columnList = "encounter_id"),
    @Index(name = "idx_prescription_status", columnList = "status"),
    @Index(name = "idx_prescription_medication", columnList = "medication_id")
})
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Patient som ordinationen gäller */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** Vårdkontakt där ordinationen gjordes */
    @Column(name = "encounter_id")
    private UUID encounterId;

    /** Läkemedel */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;

    /** Fritext läkemedelsnamn om ej från register */
    @Column(name = "medication_text", length = 500)
    private String medicationText;

    /** ATC-kod (kan anges utan koppling till Medication) */
    @Column(name = "atc_code", length = 10)
    private String atcCode;

    /** Status */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PrescriptionStatus status = PrescriptionStatus.DRAFT;

    /** Ordinationsorsak/indikation */
    @Column(name = "indication", length = 500)
    private String indication;

    /** Administreringsväg */
    @Enumerated(EnumType.STRING)
    @Column(name = "route", length = 30)
    private RouteOfAdministration route;

    /** Doseringstext (fritext, t.ex. "1 tablett morgon och kväll") */
    @Column(name = "dosage_instruction", length = 1000)
    private String dosageInstruction;

    /** Dos per tillfälle */
    @Column(name = "dose_quantity", precision = 18, scale = 4)
    private BigDecimal doseQuantity;

    /** Dosenhet (tablett, ml, mg, etc.) */
    @Column(name = "dose_unit", length = 30)
    private String doseUnit;

    /** Frekvens (antal gånger per period) */
    @Column(name = "frequency")
    private Integer frequency;

    /** Frekvensperiod i timmar (24 = dagligen, 168 = veckovis) */
    @Column(name = "frequency_period_hours")
    private Integer frequencyPeriodHours;

    /** Vid behov (PRN - pro re nata) */
    @Column(name = "as_needed")
    private boolean asNeeded = false;

    /** Max dos per dygn */
    @Column(name = "max_dose_per_day", precision = 18, scale = 4)
    private BigDecimal maxDosePerDay;

    /** Startdatum */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Slutdatum (null = tillsvidare) */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Behandlingslängd i dagar (alternativ till slutdatum) */
    @Column(name = "duration_days")
    private Integer durationDays;

    /** Antal uttag på recept */
    @Column(name = "dispense_quantity")
    private Integer dispenseQuantity;

    /** Antal iterationer (förnyelser) */
    @Column(name = "number_of_repeats")
    private Integer numberOfRepeats;

    /** Får ej bytas ut (generika) */
    @Column(name = "substitution_not_allowed")
    private boolean substitutionNotAllowed = false;

    /** Orsak till att utbyte ej tillåts */
    @Column(name = "substitution_reason", length = 500)
    private String substitutionReason;

    /** Förskrivare ID */
    @Column(name = "prescriber_id", nullable = false)
    private UUID prescriberId;

    /** Förskrivare HSA-ID */
    @Column(name = "prescriber_hsa_id", length = 50)
    private String prescriberHsaId;

    /** Förskrivare namn */
    @Column(name = "prescriber_name", length = 200)
    private String prescriberName;

    /** Förskrivarkod */
    @Column(name = "prescriber_code", length = 20)
    private String prescriberCode;

    /** Enhet där ordinationen gjordes */
    @Column(name = "unit_id")
    private UUID unitId;

    /** Enhet HSA-ID */
    @Column(name = "unit_hsa_id", length = 50)
    private String unitHsaId;

    /** Kommentar till apoteket */
    @Column(name = "pharmacy_note", length = 500)
    private String pharmacyNote;

    /** Intern kommentar (syns ej på recept) */
    @Column(name = "internal_note", length = 1000)
    private String internalNote;

    /** Föregående ordination (vid ersättning) */
    @Column(name = "superseded_prescription_id")
    private UUID supersededPrescriptionId;

    /** Orsak till avslut/avbrytande */
    @Column(name = "discontinuation_reason", length = 500)
    private String discontinuationReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "discontinued_at")
    private Instant discontinuedAt;

    /** Administrationer kopplade till denna ordination */
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicationAdministration> administrations = new ArrayList<>();

    protected Prescription() {
    }

    public Prescription(UUID patientId, UUID prescriberId) {
        this.patientId = patientId;
        this.prescriberId = prescriberId;
        this.status = PrescriptionStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    // Affärslogik

    /**
     * Aktivera ordinationen.
     */
    public void activate() {
        if (status != PrescriptionStatus.DRAFT && status != PrescriptionStatus.ON_HOLD) {
            throw new IllegalStateException("Kan endast aktivera ordination i status DRAFT eller ON_HOLD");
        }
        this.status = PrescriptionStatus.ACTIVE;
        this.activatedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Pausa ordinationen tillfälligt.
     */
    public void putOnHold(String reason) {
        if (status != PrescriptionStatus.ACTIVE) {
            throw new IllegalStateException("Kan endast pausa aktiv ordination");
        }
        this.status = PrescriptionStatus.ON_HOLD;
        this.internalNote = (internalNote != null ? internalNote + "\n" : "") + "Pausad: " + reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Avsluta ordinationen normalt (behandling klar).
     */
    public void complete() {
        if (status != PrescriptionStatus.ACTIVE && status != PrescriptionStatus.ON_HOLD) {
            throw new IllegalStateException("Kan endast avsluta aktiv eller pausad ordination");
        }
        this.status = PrescriptionStatus.COMPLETED;
        this.discontinuedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Avbryt ordination i förtid.
     */
    public void cancel(String reason) {
        if (status == PrescriptionStatus.COMPLETED || status == PrescriptionStatus.CANCELLED) {
            throw new IllegalStateException("Kan ej avbryta redan avslutad ordination");
        }
        this.status = PrescriptionStatus.CANCELLED;
        this.discontinuationReason = reason;
        this.discontinuedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Ersätt med ny ordination.
     */
    public void supersede(UUID newPrescriptionId) {
        if (status != PrescriptionStatus.ACTIVE && status != PrescriptionStatus.ON_HOLD) {
            throw new IllegalStateException("Kan endast ersätta aktiv eller pausad ordination");
        }
        this.status = PrescriptionStatus.SUPERSEDED;
        this.discontinuedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Markera som felaktigt inlagd.
     */
    public void markAsEnteredInError(String reason) {
        this.status = PrescriptionStatus.ENTERED_IN_ERROR;
        this.discontinuationReason = reason;
        this.discontinuedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Kontrollera om ordinationen är aktiv och giltig för given datum.
     */
    public boolean isActiveOnDate(LocalDate date) {
        if (status != PrescriptionStatus.ACTIVE) {
            return false;
        }
        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    /**
     * Kontrollera om ordinationen kan ändras.
     */
    public boolean isModifiable() {
        return status == PrescriptionStatus.DRAFT || status == PrescriptionStatus.ON_HOLD;
    }

    /**
     * Hämta effektivt slutdatum (beräknat från startdatum + duration om endDate ej angivet).
     */
    public LocalDate getEffectiveEndDate() {
        if (endDate != null) {
            return endDate;
        }
        if (startDate != null && durationDays != null) {
            return startDate.plusDays(durationDays);
        }
        return null; // Tillsvidare
    }

    // Getters och setters

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public String getMedicationText() {
        return medicationText;
    }

    public void setMedicationText(String medicationText) {
        this.medicationText = medicationText;
    }

    public String getAtcCode() {
        return atcCode;
    }

    public void setAtcCode(String atcCode) {
        this.atcCode = atcCode;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public String getIndication() {
        return indication;
    }

    public void setIndication(String indication) {
        this.indication = indication;
    }

    public RouteOfAdministration getRoute() {
        return route;
    }

    public void setRoute(RouteOfAdministration route) {
        this.route = route;
    }

    public String getDosageInstruction() {
        return dosageInstruction;
    }

    public void setDosageInstruction(String dosageInstruction) {
        this.dosageInstruction = dosageInstruction;
    }

    public BigDecimal getDoseQuantity() {
        return doseQuantity;
    }

    public void setDoseQuantity(BigDecimal doseQuantity) {
        this.doseQuantity = doseQuantity;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getFrequencyPeriodHours() {
        return frequencyPeriodHours;
    }

    public void setFrequencyPeriodHours(Integer frequencyPeriodHours) {
        this.frequencyPeriodHours = frequencyPeriodHours;
    }

    public boolean isAsNeeded() {
        return asNeeded;
    }

    public void setAsNeeded(boolean asNeeded) {
        this.asNeeded = asNeeded;
    }

    public BigDecimal getMaxDosePerDay() {
        return maxDosePerDay;
    }

    public void setMaxDosePerDay(BigDecimal maxDosePerDay) {
        this.maxDosePerDay = maxDosePerDay;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getDispenseQuantity() {
        return dispenseQuantity;
    }

    public void setDispenseQuantity(Integer dispenseQuantity) {
        this.dispenseQuantity = dispenseQuantity;
    }

    public Integer getNumberOfRepeats() {
        return numberOfRepeats;
    }

    public void setNumberOfRepeats(Integer numberOfRepeats) {
        this.numberOfRepeats = numberOfRepeats;
    }

    public boolean isSubstitutionNotAllowed() {
        return substitutionNotAllowed;
    }

    public void setSubstitutionNotAllowed(boolean substitutionNotAllowed) {
        this.substitutionNotAllowed = substitutionNotAllowed;
    }

    public String getSubstitutionReason() {
        return substitutionReason;
    }

    public void setSubstitutionReason(String substitutionReason) {
        this.substitutionReason = substitutionReason;
    }

    public UUID getPrescriberId() {
        return prescriberId;
    }

    public String getPrescriberHsaId() {
        return prescriberHsaId;
    }

    public void setPrescriberHsaId(String prescriberHsaId) {
        this.prescriberHsaId = prescriberHsaId;
    }

    public String getPrescriberName() {
        return prescriberName;
    }

    public void setPrescriberName(String prescriberName) {
        this.prescriberName = prescriberName;
    }

    public String getPrescriberCode() {
        return prescriberCode;
    }

    public void setPrescriberCode(String prescriberCode) {
        this.prescriberCode = prescriberCode;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
    }

    public String getUnitHsaId() {
        return unitHsaId;
    }

    public void setUnitHsaId(String unitHsaId) {
        this.unitHsaId = unitHsaId;
    }

    public String getPharmacyNote() {
        return pharmacyNote;
    }

    public void setPharmacyNote(String pharmacyNote) {
        this.pharmacyNote = pharmacyNote;
    }

    public String getInternalNote() {
        return internalNote;
    }

    public void setInternalNote(String internalNote) {
        this.internalNote = internalNote;
    }

    public UUID getSupersededPrescriptionId() {
        return supersededPrescriptionId;
    }

    public void setSupersededPrescriptionId(UUID supersededPrescriptionId) {
        this.supersededPrescriptionId = supersededPrescriptionId;
    }

    public String getDiscontinuationReason() {
        return discontinuationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getDiscontinuedAt() {
        return discontinuedAt;
    }

    public List<MedicationAdministration> getAdministrations() {
        return administrations;
    }

    /** Returnerar läkemedelsnamnet (från Medication eller fritext) */
    public String getMedicationName() {
        if (medication != null) {
            return medication.getName();
        }
        return medicationText;
    }
}
