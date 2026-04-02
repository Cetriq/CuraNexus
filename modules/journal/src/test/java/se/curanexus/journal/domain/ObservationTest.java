package se.curanexus.journal.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ObservationTest {

    @Test
    void shouldCreateObservation() {
        UUID patientId = UUID.randomUUID();
        LocalDateTime observedAt = LocalDateTime.now();

        Observation observation = new Observation(patientId, "8867-4", ObservationCategory.VITAL_SIGNS, observedAt);

        assertEquals(patientId, observation.getPatientId());
        assertEquals("8867-4", observation.getCode());
        assertEquals(ObservationCategory.VITAL_SIGNS, observation.getCategory());
        assertEquals(observedAt, observation.getObservedAt());
        assertNotNull(observation.getRecordedAt());
    }

    @Test
    void shouldSetNumericValue() {
        Observation observation = createTestObservation();

        observation.setNumericValue(new BigDecimal("120"), "mmHg");

        assertEquals(new BigDecimal("120"), observation.getValueNumeric());
        assertEquals("mmHg", observation.getUnit());
        assertNull(observation.getValueString());
        assertNull(observation.getValueBoolean());
    }

    @Test
    void shouldSetStringValue() {
        Observation observation = createTestObservation();

        observation.setStringValue("Positive");

        assertEquals("Positive", observation.getValueString());
        assertNull(observation.getValueNumeric());
        assertNull(observation.getValueBoolean());
        assertNull(observation.getUnit());
    }

    @Test
    void shouldSetBooleanValue() {
        Observation observation = createTestObservation();

        observation.setBooleanValue(true);

        assertTrue(observation.getValueBoolean());
        assertNull(observation.getValueNumeric());
        assertNull(observation.getValueString());
        assertNull(observation.getUnit());
    }

    @Test
    void shouldBeWithinReferenceRange() {
        Observation observation = createTestObservation();
        observation.setNumericValue(new BigDecimal("100"), "mg/dL");
        observation.setReferenceRange(new BigDecimal("70"), new BigDecimal("110"));

        assertTrue(observation.isWithinReferenceRange());
    }

    @Test
    void shouldBeBelowReferenceRange() {
        Observation observation = createTestObservation();
        observation.setNumericValue(new BigDecimal("50"), "mg/dL");
        observation.setReferenceRange(new BigDecimal("70"), new BigDecimal("110"));

        assertFalse(observation.isWithinReferenceRange());
    }

    @Test
    void shouldBeAboveReferenceRange() {
        Observation observation = createTestObservation();
        observation.setNumericValue(new BigDecimal("150"), "mg/dL");
        observation.setReferenceRange(new BigDecimal("70"), new BigDecimal("110"));

        assertFalse(observation.isWithinReferenceRange());
    }

    @Test
    void shouldReturnTrueWhenNoReferenceRange() {
        Observation observation = createTestObservation();
        observation.setNumericValue(new BigDecimal("150"), "mg/dL");

        assertTrue(observation.isWithinReferenceRange());
    }

    @Test
    void shouldReturnTrueWhenNoNumericValue() {
        Observation observation = createTestObservation();
        observation.setStringValue("Normal");
        observation.setReferenceRange(new BigDecimal("70"), new BigDecimal("110"));

        assertTrue(observation.isWithinReferenceRange());
    }

    @Test
    void shouldHandleLowerBoundOnly() {
        Observation observation = createTestObservation();
        observation.setNumericValue(new BigDecimal("100"), "mg/dL");
        observation.setReferenceRange(new BigDecimal("70"), null);

        assertTrue(observation.isWithinReferenceRange());
    }

    @Test
    void shouldHandleUpperBoundOnly() {
        Observation observation = createTestObservation();
        observation.setNumericValue(new BigDecimal("100"), "mg/dL");
        observation.setReferenceRange(null, new BigDecimal("110"));

        assertTrue(observation.isWithinReferenceRange());
    }

    private Observation createTestObservation() {
        return new Observation(
                UUID.randomUUID(),
                "TEST-CODE",
                ObservationCategory.LABORATORY,
                LocalDateTime.now()
        );
    }
}
