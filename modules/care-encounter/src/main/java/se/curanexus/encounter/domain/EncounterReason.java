package se.curanexus.encounter.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "encounter_reasons")
public class EncounterReason {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private Encounter encounter;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ReasonType type;

    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "code_system", length = 50)
    private String codeSystem;

    @Column(name = "display_text", length = 500)
    private String displayText;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    protected EncounterReason() {
    }

    public EncounterReason(ReasonType type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public ReasonType getType() {
        return type;
    }

    public void setType(ReasonType type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
