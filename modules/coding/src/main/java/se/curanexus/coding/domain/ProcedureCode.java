package se.curanexus.coding.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * KVÅ (Klassifikation av vårdåtgärder) procedure code.
 * Swedish classification of healthcare procedures.
 */
@Entity
@Table(name = "procedure_codes", indexes = {
    @Index(name = "idx_procedure_code", columnList = "code"),
    @Index(name = "idx_procedure_category", columnList = "category"),
    @Index(name = "idx_procedure_search", columnList = "search_text")
})
public class ProcedureCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "display_name", nullable = false, length = 500)
    private String displayName;

    @Column(name = "swedish_name", nullable = false, length = 500)
    private String swedishName;

    @Column(name = "category", length = 5)
    private String category;

    @Column(name = "category_name", length = 200)
    private String categoryName;

    @Column(name = "parent_code", length = 10)
    private String parentCode;

    @Column(name = "level")
    private Integer level;

    @Column(name = "is_leaf", nullable = false)
    private boolean leaf = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "search_text", length = 1000)
    private String searchText;

    @Enumerated(EnumType.STRING)
    @Column(name = "performer_type", length = 20)
    private PerformerType performerType;

    @Column(name = "requires_laterality", nullable = false)
    private boolean requiresLaterality = false;

    protected ProcedureCode() {
    }

    public ProcedureCode(String code, String displayName, String swedishName) {
        this.code = code;
        this.displayName = displayName;
        this.swedishName = swedishName;
        this.searchText = (code + " " + swedishName + " " + displayName).toLowerCase();
    }

    public enum PerformerType {
        PHYSICIAN,      // Läkare
        NURSE,          // Sjuksköterska
        PHYSIOTHERAPIST,// Fysioterapeut
        PSYCHOLOGIST,   // Psykolog
        ANY             // Alla
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
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

    public PerformerType getPerformerType() {
        return performerType;
    }

    public void setPerformerType(PerformerType performerType) {
        this.performerType = performerType;
    }

    public boolean isRequiresLaterality() {
        return requiresLaterality;
    }

    public void setRequiresLaterality(boolean requiresLaterality) {
        this.requiresLaterality = requiresLaterality;
    }
}
