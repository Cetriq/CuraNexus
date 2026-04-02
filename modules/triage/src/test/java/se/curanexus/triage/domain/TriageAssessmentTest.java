package se.curanexus.triage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TriageAssessment")
class TriageAssessmentTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("should create assessment with valid parameters")
        void shouldCreateAssessmentWithValidParameters() {
            UUID patientId = UUID.randomUUID();
            UUID encounterId = UUID.randomUUID();
            UUID nurseId = UUID.randomUUID();
            String complaint = "Chest pain";

            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, complaint);

            assertEquals(patientId, assessment.getPatientId());
            assertEquals(encounterId, assessment.getEncounterId());
            assertEquals(nurseId, assessment.getTriageNurseId());
            assertEquals(complaint, assessment.getChiefComplaint());
            assertEquals(AssessmentStatus.IN_PROGRESS, assessment.getStatus());
            assertNotNull(assessment.getArrivalTime());
            assertNotNull(assessment.getTriageStartTime());
            assertNotNull(assessment.getCreatedAt());
            assertNotNull(assessment.getUpdatedAt());
        }

        @Test
        @DisplayName("should throw exception when patientId is null")
        void shouldThrowExceptionWhenPatientIdIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TriageAssessment(null, UUID.randomUUID(), UUID.randomUUID(), "Chest pain"));
        }

        @Test
        @DisplayName("should throw exception when encounterId is null")
        void shouldThrowExceptionWhenEncounterIdIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TriageAssessment(UUID.randomUUID(), null, UUID.randomUUID(), "Chest pain"));
        }

        @Test
        @DisplayName("should throw exception when triageNurseId is null")
        void shouldThrowExceptionWhenTriageNurseIdIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TriageAssessment(UUID.randomUUID(), UUID.randomUUID(), null, "Chest pain"));
        }

        @Test
        @DisplayName("should throw exception when chiefComplaint is null")
        void shouldThrowExceptionWhenChiefComplaintIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TriageAssessment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null));
        }

        @Test
        @DisplayName("should throw exception when chiefComplaint is blank")
        void shouldThrowExceptionWhenChiefComplaintIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TriageAssessment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "   "));
        }
    }

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("should complete assessment with all required fields")
        void shouldCompleteAssessmentWithAllRequiredFields() {
            TriageAssessment assessment = createValidAssessment();

            assessment.complete(TriagePriority.URGENT, CareLevel.EMERGENCY_CARE, Disposition.ADMIT);

            assertEquals(TriagePriority.URGENT, assessment.getPriority());
            assertEquals(CareLevel.EMERGENCY_CARE, assessment.getCareLevel());
            assertEquals(Disposition.ADMIT, assessment.getDisposition());
            assertEquals(AssessmentStatus.COMPLETED, assessment.getStatus());
            assertNotNull(assessment.getTriageEndTime());
        }

        @Test
        @DisplayName("should throw exception when priority is null")
        void shouldThrowExceptionWhenPriorityIsNull() {
            TriageAssessment assessment = createValidAssessment();

            assertThrows(IllegalArgumentException.class, () ->
                    assessment.complete(null, CareLevel.EMERGENCY_CARE, Disposition.ADMIT));
        }

        @Test
        @DisplayName("should throw exception when careLevel is null")
        void shouldThrowExceptionWhenCareLevelIsNull() {
            TriageAssessment assessment = createValidAssessment();

            assertThrows(IllegalArgumentException.class, () ->
                    assessment.complete(TriagePriority.URGENT, null, Disposition.ADMIT));
        }

        @Test
        @DisplayName("should throw exception when disposition is null")
        void shouldThrowExceptionWhenDispositionIsNull() {
            TriageAssessment assessment = createValidAssessment();

            assertThrows(IllegalArgumentException.class, () ->
                    assessment.complete(TriagePriority.URGENT, CareLevel.EMERGENCY_CARE, null));
        }
    }

    @Nested
    @DisplayName("escalate")
    class Escalate {

        @Test
        @DisplayName("should escalate to higher priority")
        void shouldEscalateToHigherPriority() {
            TriageAssessment assessment = createValidAssessment();
            assessment.setPriority(TriagePriority.URGENT);
            UUID escalatedBy = UUID.randomUUID();

            assessment.escalate(TriagePriority.EMERGENT, "Worsening symptoms", escalatedBy);

            assertEquals(TriagePriority.EMERGENT, assessment.getPriority());
            assertEquals(1, assessment.getEscalationHistory().size());
            EscalationRecord record = assessment.getEscalationHistory().get(0);
            assertEquals(TriagePriority.URGENT, record.getPreviousPriority());
            assertEquals(TriagePriority.EMERGENT, record.getNewPriority());
            assertEquals("Worsening symptoms", record.getReason());
        }

        @Test
        @DisplayName("should throw exception when new priority is null")
        void shouldThrowExceptionWhenNewPriorityIsNull() {
            TriageAssessment assessment = createValidAssessment();
            assessment.setPriority(TriagePriority.URGENT);

            assertThrows(IllegalArgumentException.class, () ->
                    assessment.escalate(null, "Reason", UUID.randomUUID()));
        }

        @Test
        @DisplayName("should throw exception when escalating to lower priority")
        void shouldThrowExceptionWhenEscalatingToLowerPriority() {
            TriageAssessment assessment = createValidAssessment();
            assessment.setPriority(TriagePriority.EMERGENT);

            assertThrows(IllegalArgumentException.class, () ->
                    assessment.escalate(TriagePriority.URGENT, "Reason", UUID.randomUUID()));
        }

        @Test
        @DisplayName("should allow first escalation when priority is null")
        void shouldAllowFirstEscalationWhenPriorityIsNull() {
            TriageAssessment assessment = createValidAssessment();
            UUID escalatedBy = UUID.randomUUID();

            assessment.escalate(TriagePriority.IMMEDIATE, "Initial triage", escalatedBy);

            assertEquals(TriagePriority.IMMEDIATE, assessment.getPriority());
        }
    }

    @Nested
    @DisplayName("addSymptom")
    class AddSymptom {

        @Test
        @DisplayName("should add symptom to assessment")
        void shouldAddSymptomToAssessment() {
            TriageAssessment assessment = createValidAssessment();
            Symptom symptom = new Symptom("CHEST_PAIN", "Sharp pain in chest");

            assessment.addSymptom(symptom);

            assertEquals(1, assessment.getSymptoms().size());
            assertSame(symptom, assessment.getSymptoms().get(0));
        }
    }

    @Nested
    @DisplayName("waitTime and overdue")
    class WaitTimeAndOverdue {

        @Test
        @DisplayName("should calculate wait time")
        void shouldCalculateWaitTime() {
            TriageAssessment assessment = createValidAssessment();

            int waitTime = assessment.getWaitTimeMinutes();

            assertTrue(waitTime >= 0);
        }

        @Test
        @DisplayName("should not be overdue when no priority set")
        void shouldNotBeOverdueWhenNoPrioritySet() {
            TriageAssessment assessment = createValidAssessment();

            assertFalse(assessment.isOverdue());
        }

        @Test
        @DisplayName("should not be overdue when completed")
        void shouldNotBeOverdueWhenCompleted() {
            TriageAssessment assessment = createValidAssessment();
            assessment.complete(TriagePriority.NON_URGENT, CareLevel.PRIMARY_CARE, Disposition.DISCHARGE);

            assertFalse(assessment.isOverdue());
        }
    }

    private TriageAssessment createValidAssessment() {
        return new TriageAssessment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Chest pain"
        );
    }
}
