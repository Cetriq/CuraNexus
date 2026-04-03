package se.curanexus.lab.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabResult - Domänmodell")
class LabResultTest {

    private LabOrderItem orderItem;

    @BeforeEach
    void setUp() {
        orderItem = new LabOrderItem("NPU01685", "NPU", "Hemoglobin", SpecimenType.BLOOD_VENOUS);
    }

    @Nested
    @DisplayName("Registrera numeriskt resultat")
    class NumericResult {

        @Test
        @DisplayName("Ska registrera normalt resultat")
        void shouldRegisterNormalResult() {
            LabResult result = new LabResult(orderItem);

            result.registerNumericResult(
                    new BigDecimal("145"),
                    "g/L",
                    new BigDecimal("120"),
                    new BigDecimal("160")
            );

            assertEquals(ResultStatus.PRELIMINARY, result.getStatus());
            assertEquals(new BigDecimal("145"), result.getValueNumeric());
            assertEquals("g/L", result.getUnit());
            assertEquals(AbnormalFlag.NORMAL, result.getAbnormalFlag());
            assertFalse(result.getIsCritical());
        }

        @Test
        @DisplayName("Ska flagga lågt värde")
        void shouldFlagLowValue() {
            LabResult result = new LabResult(orderItem);

            result.registerNumericResult(
                    new BigDecimal("110"),
                    "g/L",
                    new BigDecimal("120"),
                    new BigDecimal("160")
            );

            assertEquals(AbnormalFlag.LOW, result.getAbnormalFlag());
            assertFalse(result.getIsCritical());
        }

        @Test
        @DisplayName("Ska flagga högt värde")
        void shouldFlagHighValue() {
            LabResult result = new LabResult(orderItem);

            result.registerNumericResult(
                    new BigDecimal("175"),
                    "g/L",
                    new BigDecimal("120"),
                    new BigDecimal("160")
            );

            assertEquals(AbnormalFlag.HIGH, result.getAbnormalFlag());
            assertFalse(result.getIsCritical());
        }

        @Test
        @DisplayName("Ska flagga kritiskt lågt värde")
        void shouldFlagCriticallyLowValue() {
            LabResult result = new LabResult(orderItem);

            result.registerNumericResult(
                    new BigDecimal("50"),
                    "g/L",
                    new BigDecimal("120"),
                    new BigDecimal("160")
            );

            assertEquals(AbnormalFlag.CRITICAL_LOW, result.getAbnormalFlag());
            assertTrue(result.getIsCritical());
        }

        @Test
        @DisplayName("Ska flagga kritiskt högt värde")
        void shouldFlagCriticallyHighValue() {
            LabResult result = new LabResult(orderItem);

            result.registerNumericResult(
                    new BigDecimal("250"),
                    "g/L",
                    new BigDecimal("120"),
                    new BigDecimal("160")
            );

            assertEquals(AbnormalFlag.CRITICAL_HIGH, result.getAbnormalFlag());
            assertTrue(result.getIsCritical());
        }
    }

    @Nested
    @DisplayName("Registrera textresultat")
    class TextResult {

        @Test
        @DisplayName("Ska registrera textresultat")
        void shouldRegisterTextResult() {
            LabResult result = new LabResult(orderItem);

            result.registerTextResult("Positiv");

            assertEquals(ResultStatus.PRELIMINARY, result.getStatus());
            assertEquals("Positiv", result.getValueText());
        }

        @Test
        @DisplayName("Ska kunna sätta avvikelseflagga manuellt")
        void shouldSetAbnormalFlagManually() {
            LabResult result = new LabResult(orderItem);
            result.registerTextResult("Positiv");

            result.setAbnormalFlagManually(AbnormalFlag.POSITIVE, false);

            assertEquals(AbnormalFlag.POSITIVE, result.getAbnormalFlag());
            assertFalse(result.getIsCritical());
        }
    }

    @Nested
    @DisplayName("Granskning")
    class Review {

        @Test
        @DisplayName("Ska kunna granska preliminärt resultat")
        void shouldReviewPreliminaryResult() {
            LabResult result = new LabResult(orderItem);
            result.registerNumericResult(new BigDecimal("145"), "g/L",
                    new BigDecimal("120"), new BigDecimal("160"));
            UUID reviewerId = UUID.randomUUID();

            result.review(reviewerId, "Dr. Svensson");

            assertEquals(ResultStatus.FINAL, result.getStatus());
            assertEquals(reviewerId, result.getReviewerId());
            assertEquals("Dr. Svensson", result.getReviewerName());
            assertNotNull(result.getReviewedAt());
            assertNotNull(result.getResultedAt());
        }

        @Test
        @DisplayName("Ska ej kunna granska pending resultat")
        void shouldNotReviewPendingResult() {
            LabResult result = new LabResult(orderItem);
            // Inget värde registrerat, fortfarande PENDING

            assertThrows(IllegalStateException.class,
                    () -> result.review(UUID.randomUUID(), "Dr. Test"));
        }
    }

    @Nested
    @DisplayName("Korrigering")
    class Correction {

        @Test
        @DisplayName("Ska kunna korrigera slutgiltigt resultat")
        void shouldCorrectFinalResult() {
            LabResult result = new LabResult(orderItem);
            result.registerNumericResult(new BigDecimal("145"), "g/L",
                    new BigDecimal("120"), new BigDecimal("160"));
            result.review(UUID.randomUUID(), "Dr. Svensson");
            UUID correctorId = UUID.randomUUID();

            result.correct(new BigDecimal("150"), "Omanalys genomförd", correctorId, "Dr. Andersson");

            assertEquals(ResultStatus.CORRECTED, result.getStatus());
            assertEquals(new BigDecimal("150"), result.getValueNumeric());
            assertTrue(result.getLabComment().contains("Korrigerat"));
        }

        @Test
        @DisplayName("Ska ej kunna korrigera preliminärt resultat")
        void shouldNotCorrectPreliminaryResult() {
            LabResult result = new LabResult(orderItem);
            result.registerNumericResult(new BigDecimal("145"), "g/L",
                    new BigDecimal("120"), new BigDecimal("160"));
            // Ej granskat, fortfarande PRELIMINARY

            assertThrows(IllegalStateException.class,
                    () -> result.correct(new BigDecimal("150"), "Test", UUID.randomUUID(), "Test"));
        }
    }

    @Nested
    @DisplayName("Makulering")
    class Cancellation {

        @Test
        @DisplayName("Ska kunna makulera resultat")
        void shouldCancelResult() {
            LabResult result = new LabResult(orderItem);
            result.registerNumericResult(new BigDecimal("145"), "g/L",
                    new BigDecimal("120"), new BigDecimal("160"));

            result.cancel("Fel prov");

            assertEquals(ResultStatus.CANCELLED, result.getStatus());
            assertTrue(result.getLabComment().contains("Makulerat"));
        }
    }
}
