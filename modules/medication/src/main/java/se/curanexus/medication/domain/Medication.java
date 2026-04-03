package se.curanexus.medication.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Läkemedel - referensdata för läkemedel.
 * Baserat på NPL (Nationellt Produkt Register för Läkemedel) / FASS.
 */
@Entity
@Table(name = "medications", indexes = {
    @Index(name = "idx_medication_npl_id", columnList = "npl_id"),
    @Index(name = "idx_medication_atc_code", columnList = "atc_code"),
    @Index(name = "idx_medication_name", columnList = "name")
})
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** NPL-ID (Nationellt Produkt Register) - unikt för svensk marknad */
    @Column(name = "npl_id", length = 50, unique = true)
    private String nplId;

    /** NPL Pack-ID för specifik förpackning */
    @Column(name = "npl_pack_id", length = 50)
    private String nplPackId;

    /** ATC-kod (Anatomical Therapeutic Chemical) */
    @Column(name = "atc_code", length = 10)
    private String atcCode;

    /** Läkemedelsnamn (handelsnamn) */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Generiskt namn (substansnamn) */
    @Column(name = "generic_name", length = 200)
    private String genericName;

    /** Tillverkare */
    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    /** Styrka som text (t.ex. "500 mg") */
    @Column(name = "strength", length = 100)
    private String strength;

    /** Styrka numeriskt värde */
    @Column(name = "strength_value", precision = 18, scale = 4)
    private BigDecimal strengthValue;

    /** Styrka enhet (mg, ml, IE, etc.) */
    @Column(name = "strength_unit", length = 20)
    private String strengthUnit;

    /** Läkemedelsform */
    @Enumerated(EnumType.STRING)
    @Column(name = "dosage_form", length = 30)
    private DosageForm dosageForm;

    /** Administreringsväg */
    @Enumerated(EnumType.STRING)
    @Column(name = "route", length = 30)
    private RouteOfAdministration route;

    /** Förpackningsstorlek */
    @Column(name = "package_size")
    private Integer packageSize;

    /** Förpackningsenhet (st, ml, g) */
    @Column(name = "package_unit", length = 20)
    private String packageUnit;

    /** Narkotikaklassad */
    @Column(name = "is_narcotic")
    private boolean narcotic = false;

    /** Narkotikaklass (II-V enligt svenska regler) */
    @Column(name = "narcotic_class", length = 10)
    private String narcoticClass;

    /** Receptbelagd */
    @Column(name = "prescription_required")
    private boolean prescriptionRequired = true;

    /** Utbytbar (generikautbyte tillåtet) */
    @Column(name = "is_substitutable")
    private boolean substitutable = true;

    /** Risk för beroende/missbruk */
    @Column(name = "abuse_potential")
    private boolean abusePotential = false;

    /** Aktiv (finns på marknaden) */
    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Medication() {
    }

    public Medication(String name, String genericName) {
        this.name = name;
        this.genericName = genericName;
        this.createdAt = Instant.now();
    }

    // Getters och setters

    public UUID getId() {
        return id;
    }

    public String getNplId() {
        return nplId;
    }

    public void setNplId(String nplId) {
        this.nplId = nplId;
    }

    public String getNplPackId() {
        return nplPackId;
    }

    public void setNplPackId(String nplPackId) {
        this.nplPackId = nplPackId;
    }

    public String getAtcCode() {
        return atcCode;
    }

    public void setAtcCode(String atcCode) {
        this.atcCode = atcCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public BigDecimal getStrengthValue() {
        return strengthValue;
    }

    public void setStrengthValue(BigDecimal strengthValue) {
        this.strengthValue = strengthValue;
    }

    public String getStrengthUnit() {
        return strengthUnit;
    }

    public void setStrengthUnit(String strengthUnit) {
        this.strengthUnit = strengthUnit;
    }

    public DosageForm getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(DosageForm dosageForm) {
        this.dosageForm = dosageForm;
    }

    public RouteOfAdministration getRoute() {
        return route;
    }

    public void setRoute(RouteOfAdministration route) {
        this.route = route;
    }

    public Integer getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(Integer packageSize) {
        this.packageSize = packageSize;
    }

    public String getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(String packageUnit) {
        this.packageUnit = packageUnit;
    }

    public boolean isNarcotic() {
        return narcotic;
    }

    public void setNarcotic(boolean narcotic) {
        this.narcotic = narcotic;
    }

    public String getNarcoticClass() {
        return narcoticClass;
    }

    public void setNarcoticClass(String narcoticClass) {
        this.narcoticClass = narcoticClass;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public boolean isSubstitutable() {
        return substitutable;
    }

    public void setSubstitutable(boolean substitutable) {
        this.substitutable = substitutable;
    }

    public boolean isAbusePotential() {
        return abusePotential;
    }

    public void setAbusePotential(boolean abusePotential) {
        this.abusePotential = abusePotential;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    /** Returnerar fullständig beskrivning: namn + styrka + form */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder(name);
        if (strength != null) {
            sb.append(" ").append(strength);
        }
        if (dosageForm != null) {
            sb.append(" ").append(dosageForm.name().toLowerCase().replace("_", " "));
        }
        return sb.toString();
    }
}
