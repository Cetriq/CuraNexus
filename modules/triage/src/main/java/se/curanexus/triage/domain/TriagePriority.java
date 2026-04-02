package se.curanexus.triage.domain;

/**
 * Triage priority levels based on RETTS (Rapid Emergency Triage and Treatment System).
 * Swedish standard for emergency triage.
 */
public enum TriagePriority {
    IMMEDIATE("Red", 0),       // Immediate life threat
    EMERGENT("Orange", 15),    // Time-critical, < 15 min
    URGENT("Yellow", 60),      // Serious, < 60 min
    LESS_URGENT("Green", 120), // Standard, < 120 min
    NON_URGENT("Blue", 240);   // Minor, < 240 min

    private final String color;
    private final int maxWaitMinutes;

    TriagePriority(String color, int maxWaitMinutes) {
        this.color = color;
        this.maxWaitMinutes = maxWaitMinutes;
    }

    public String getColor() {
        return color;
    }

    public int getMaxWaitMinutes() {
        return maxWaitMinutes;
    }

    public boolean isHigherThan(TriagePriority other) {
        if (other == null) {
            return true;
        }
        return this.ordinal() < other.ordinal();
    }
}
