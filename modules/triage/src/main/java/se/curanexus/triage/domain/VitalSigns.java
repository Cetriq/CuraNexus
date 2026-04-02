package se.curanexus.triage.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vital_signs", indexes = {
    @Index(name = "idx_vital_signs_assessment", columnList = "assessment_id")
})
public class VitalSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private TriageAssessment assessment;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;

    @Column(name = "pain_level")
    private Integer painLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "consciousness_level", length = 15)
    private ConsciousnessLevel consciousnessLevel;

    @Column(name = "glucose_level")
    private Double glucoseLevel;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "recorded_by")
    private UUID recordedBy;

    protected VitalSigns() {
    }

    public VitalSigns(UUID recordedBy) {
        this.recordedBy = recordedBy;
        this.recordedAt = Instant.now();
    }

    void setAssessment(TriageAssessment assessment) {
        this.assessment = assessment;
    }

    public List<VitalSignWarning> checkWarnings() {
        List<VitalSignWarning> warnings = new ArrayList<>();

        // Blood pressure checks
        if (bloodPressureSystolic != null) {
            if (bloodPressureSystolic >= 180) {
                warnings.add(new VitalSignWarning("bloodPressureSystolic", "CRITICAL", "Hypertensive crisis (>= 180 mmHg)"));
            } else if (bloodPressureSystolic <= 90) {
                warnings.add(new VitalSignWarning("bloodPressureSystolic", "HIGH", "Hypotension (<= 90 mmHg)"));
            }
        }

        // Heart rate checks
        if (heartRate != null) {
            if (heartRate >= 150) {
                warnings.add(new VitalSignWarning("heartRate", "CRITICAL", "Severe tachycardia (>= 150 bpm)"));
            } else if (heartRate >= 120) {
                warnings.add(new VitalSignWarning("heartRate", "HIGH", "Tachycardia (>= 120 bpm)"));
            } else if (heartRate <= 40) {
                warnings.add(new VitalSignWarning("heartRate", "CRITICAL", "Severe bradycardia (<= 40 bpm)"));
            } else if (heartRate <= 50) {
                warnings.add(new VitalSignWarning("heartRate", "MEDIUM", "Bradycardia (<= 50 bpm)"));
            }
        }

        // Respiratory rate checks
        if (respiratoryRate != null) {
            if (respiratoryRate >= 30) {
                warnings.add(new VitalSignWarning("respiratoryRate", "CRITICAL", "Severe tachypnea (>= 30/min)"));
            } else if (respiratoryRate >= 24) {
                warnings.add(new VitalSignWarning("respiratoryRate", "HIGH", "Tachypnea (>= 24/min)"));
            } else if (respiratoryRate <= 8) {
                warnings.add(new VitalSignWarning("respiratoryRate", "CRITICAL", "Bradypnea (<= 8/min)"));
            }
        }

        // Temperature checks
        if (temperature != null) {
            if (temperature >= 40.0) {
                warnings.add(new VitalSignWarning("temperature", "CRITICAL", "Hyperpyrexia (>= 40°C)"));
            } else if (temperature >= 38.5) {
                warnings.add(new VitalSignWarning("temperature", "HIGH", "High fever (>= 38.5°C)"));
            } else if (temperature <= 35.0) {
                warnings.add(new VitalSignWarning("temperature", "CRITICAL", "Hypothermia (<= 35°C)"));
            }
        }

        // Oxygen saturation checks
        if (oxygenSaturation != null) {
            if (oxygenSaturation <= 88) {
                warnings.add(new VitalSignWarning("oxygenSaturation", "CRITICAL", "Severe hypoxia (<= 88%)"));
            } else if (oxygenSaturation <= 92) {
                warnings.add(new VitalSignWarning("oxygenSaturation", "HIGH", "Hypoxia (<= 92%)"));
            }
        }

        // Consciousness level checks
        if (consciousnessLevel != null && consciousnessLevel != ConsciousnessLevel.ALERT) {
            String severity = consciousnessLevel == ConsciousnessLevel.UNRESPONSIVE ? "CRITICAL" :
                              consciousnessLevel == ConsciousnessLevel.PAIN ? "HIGH" : "MEDIUM";
            warnings.add(new VitalSignWarning("consciousnessLevel", severity, "Altered consciousness: " + consciousnessLevel));
        }

        // Glucose checks
        if (glucoseLevel != null) {
            if (glucoseLevel <= 3.0) {
                warnings.add(new VitalSignWarning("glucoseLevel", "CRITICAL", "Severe hypoglycemia (<= 3.0 mmol/L)"));
            } else if (glucoseLevel <= 4.0) {
                warnings.add(new VitalSignWarning("glucoseLevel", "HIGH", "Hypoglycemia (<= 4.0 mmol/L)"));
            } else if (glucoseLevel >= 20.0) {
                warnings.add(new VitalSignWarning("glucoseLevel", "CRITICAL", "Severe hyperglycemia (>= 20 mmol/L)"));
            }
        }

        return warnings;
    }

    public record VitalSignWarning(String parameter, String severity, String message) {}

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public TriageAssessment getAssessment() {
        return assessment;
    }

    public Integer getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public void setBloodPressureSystolic(Integer bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
    }

    public Integer getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getOxygenSaturation() {
        return oxygenSaturation;
    }

    public void setOxygenSaturation(Integer oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public Integer getPainLevel() {
        return painLevel;
    }

    public void setPainLevel(Integer painLevel) {
        this.painLevel = painLevel;
    }

    public ConsciousnessLevel getConsciousnessLevel() {
        return consciousnessLevel;
    }

    public void setConsciousnessLevel(ConsciousnessLevel consciousnessLevel) {
        this.consciousnessLevel = consciousnessLevel;
    }

    public Double getGlucoseLevel() {
        return glucoseLevel;
    }

    public void setGlucoseLevel(Double glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public UUID getRecordedBy() {
        return recordedBy;
    }
}
