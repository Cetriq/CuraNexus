package se.curanexus.triage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Symptom")
class SymptomTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("should create symptom with valid parameters")
        void shouldCreateSymptomWithValidParameters() {
            Symptom symptom = new Symptom("CHEST_PAIN", "Sharp pain in chest");

            assertEquals("CHEST_PAIN", symptom.getSymptomCode());
            assertEquals("Sharp pain in chest", symptom.getDescription());
            assertFalse(symptom.isChiefComplaint());
            assertNotNull(symptom.getRecordedAt());
        }

        @Test
        @DisplayName("should throw exception when symptomCode is null")
        void shouldThrowExceptionWhenSymptomCodeIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Symptom(null, "Description"));
        }

        @Test
        @DisplayName("should throw exception when symptomCode is blank")
        void shouldThrowExceptionWhenSymptomCodeIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Symptom("   ", "Description"));
        }

        @Test
        @DisplayName("should throw exception when description is null")
        void shouldThrowExceptionWhenDescriptionIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Symptom("CHEST_PAIN", null));
        }

        @Test
        @DisplayName("should throw exception when description is blank")
        void shouldThrowExceptionWhenDescriptionIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Symptom("CHEST_PAIN", "   "));
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("should set optional fields")
        void shouldSetOptionalFields() {
            Symptom symptom = new Symptom("CHEST_PAIN", "Sharp pain");

            symptom.setSeverity(Severity.SEVERE);
            symptom.setBodyLocation("Left chest");
            symptom.setDuration("2 hours");
            symptom.setChiefComplaint(true);

            assertEquals(Severity.SEVERE, symptom.getSeverity());
            assertEquals("Left chest", symptom.getBodyLocation());
            assertEquals("2 hours", symptom.getDuration());
            assertTrue(symptom.isChiefComplaint());
        }
    }
}
