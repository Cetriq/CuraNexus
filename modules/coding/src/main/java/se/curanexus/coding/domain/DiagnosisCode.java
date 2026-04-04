package se.curanexus.coding.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * ICD-10-SE diagnosis code.
 * Swedish adaptation of the International Classification of Diseases.
 */
@Entity
@Table(name = "diagnosis_codes", indexes = {
    @Index(name = "idx_diagnosis_code", columnList = "code"),
    @Index(name = "idx_diagnosis_chapter", columnList = "chapter"),
    @Index(name = "idx_diagnosis_search", columnList = "search_text")
})
public class DiagnosisCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "display_name", nullable = false, length = 500)
    private String displayName;

    @Column(name = "swedish_name", nullable = false, length = 500)
    private String swedishName;

    @Column(name = "chapter", length = 5)
    private String chapter;

    @Column(name = "chapter_name", length = 200)
    private String chapterName;

    @Column(name = "block", length = 20)
    private String block;

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

    @Column(name = "gender_restriction", length = 1)
    private String genderRestriction; // M, F, or null

    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    protected DiagnosisCode() {
    }

    public DiagnosisCode(String code, String displayName, String swedishName) {
        this.code = code;
        this.displayName = displayName;
        this.swedishName = swedishName;
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

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
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

    public String getGenderRestriction() {
        return genderRestriction;
    }

    public void setGenderRestriction(String genderRestriction) {
        this.genderRestriction = genderRestriction;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }
}
