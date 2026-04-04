package se.curanexus.coding.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * ATC (Anatomical Therapeutic Chemical) medication classification code.
 * WHO standard for classifying medications.
 */
@Entity
@Table(name = "medication_codes", indexes = {
    @Index(name = "idx_medication_code", columnList = "code"),
    @Index(name = "idx_medication_level", columnList = "level"),
    @Index(name = "idx_medication_search", columnList = "search_text")
})
public class MedicationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "display_name", nullable = false, length = 500)
    private String displayName;

    @Column(name = "swedish_name", nullable = false, length = 500)
    private String swedishName;

    @Column(name = "level", nullable = false)
    private Integer level; // 1-5 in ATC hierarchy

    @Column(name = "parent_code", length = 10)
    private String parentCode;

    @Column(name = "anatomical_group", length = 1)
    private String anatomicalGroup; // First level: A, B, C, etc.

    @Column(name = "therapeutic_group", length = 3)
    private String therapeuticGroup; // Second level

    @Column(name = "pharmacological_group", length = 4)
    private String pharmacologicalGroup; // Third level

    @Column(name = "chemical_group", length = 5)
    private String chemicalGroup; // Fourth level

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "search_text", length = 1000)
    private String searchText;

    @Column(name = "ddd_value")
    private Double dddValue; // Defined Daily Dose

    @Column(name = "ddd_unit", length = 20)
    private String dddUnit;

    @Column(name = "administration_route", length = 10)
    private String administrationRoute; // O (oral), P (parenteral), etc.

    protected MedicationCode() {
    }

    public MedicationCode(String code, String displayName, String swedishName, Integer level) {
        this.code = code;
        this.displayName = displayName;
        this.swedishName = swedishName;
        this.level = level;
        this.searchText = (code + " " + swedishName + " " + displayName).toLowerCase();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSwedishName() {
        return swedishName;
    }

    public void setSwedishName(String swedishName) {
        this.swedishName = swedishName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getAnatomicalGroup() {
        return anatomicalGroup;
    }

    public void setAnatomicalGroup(String anatomicalGroup) {
        this.anatomicalGroup = anatomicalGroup;
    }

    public String getTherapeuticGroup() {
        return therapeuticGroup;
    }

    public void setTherapeuticGroup(String therapeuticGroup) {
        this.therapeuticGroup = therapeuticGroup;
    }

    public String getPharmacologicalGroup() {
        return pharmacologicalGroup;
    }

    public void setPharmacologicalGroup(String pharmacologicalGroup) {
        this.pharmacologicalGroup = pharmacologicalGroup;
    }

    public String getChemicalGroup() {
        return chemicalGroup;
    }

    public void setChemicalGroup(String chemicalGroup) {
        this.chemicalGroup = chemicalGroup;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Double getDddValue() {
        return dddValue;
    }

    public void setDddValue(Double dddValue) {
        this.dddValue = dddValue;
    }

    public String getDddUnit() {
        return dddUnit;
    }

    public void setDddUnit(String dddUnit) {
        this.dddUnit = dddUnit;
    }

    public String getAdministrationRoute() {
        return administrationRoute;
    }

    public void setAdministrationRoute(String administrationRoute) {
        this.administrationRoute = administrationRoute;
    }
}
