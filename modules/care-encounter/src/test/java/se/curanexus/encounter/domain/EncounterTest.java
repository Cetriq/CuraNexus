package se.curanexus.encounter.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class EncounterTest {

    @Test
    void constructor_shouldSetInitialStatus() {
        // Given & When
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);

        // Then
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.PLANNED);
    }

    @Test
    void canTransitionTo_fromPlanned_shouldAllowArrivedOrCancelled() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);

        // Then
        assertThat(encounter.canTransitionTo(EncounterStatus.ARRIVED)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.CANCELLED)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.IN_PROGRESS)).isFalse();
        assertThat(encounter.canTransitionTo(EncounterStatus.FINISHED)).isFalse();
    }

    @Test
    void canTransitionTo_fromArrived_shouldAllowTriagedOrInProgressOrCancelled() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        encounter.transitionTo(EncounterStatus.ARRIVED);

        // Then
        assertThat(encounter.canTransitionTo(EncounterStatus.TRIAGED)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.IN_PROGRESS)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.CANCELLED)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.FINISHED)).isFalse();
    }

    @Test
    void canTransitionTo_fromInProgress_shouldAllowOnHoldOrFinishedOrCancelled() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        encounter.transitionTo(EncounterStatus.ARRIVED);
        encounter.transitionTo(EncounterStatus.IN_PROGRESS);

        // Then
        assertThat(encounter.canTransitionTo(EncounterStatus.ON_HOLD)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.FINISHED)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.CANCELLED)).isTrue();
        assertThat(encounter.canTransitionTo(EncounterStatus.PLANNED)).isFalse();
    }

    @Test
    void canTransitionTo_fromFinished_shouldNotAllowAnyTransition() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        encounter.transitionTo(EncounterStatus.ARRIVED);
        encounter.transitionTo(EncounterStatus.IN_PROGRESS);
        encounter.transitionTo(EncounterStatus.FINISHED);

        // Then
        assertThat(encounter.canTransitionTo(EncounterStatus.PLANNED)).isFalse();
        assertThat(encounter.canTransitionTo(EncounterStatus.IN_PROGRESS)).isFalse();
        assertThat(encounter.canTransitionTo(EncounterStatus.CANCELLED)).isFalse();
    }

    @Test
    void transitionTo_shouldThrowForInvalidTransition() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);

        // When & Then
        assertThatThrownBy(() -> encounter.transitionTo(EncounterStatus.FINISHED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from PLANNED to FINISHED");
    }

    @Test
    void transitionTo_inProgress_shouldSetActualStartTime() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        encounter.transitionTo(EncounterStatus.ARRIVED);

        // When
        encounter.transitionTo(EncounterStatus.IN_PROGRESS);

        // Then
        assertThat(encounter.getActualStartTime()).isNotNull();
    }

    @Test
    void transitionTo_finished_shouldSetActualEndTime() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        encounter.transitionTo(EncounterStatus.ARRIVED);
        encounter.transitionTo(EncounterStatus.IN_PROGRESS);

        // When
        encounter.transitionTo(EncounterStatus.FINISHED);

        // Then
        assertThat(encounter.getActualEndTime()).isNotNull();
    }

    @Test
    void addParticipant_shouldAddParticipantToList() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        Participant participant = new Participant(ParticipantType.PRACTITIONER);

        // When
        encounter.addParticipant(participant);

        // Then
        assertThat(encounter.getParticipants()).contains(participant);
        assertThat(participant.getEncounter()).isEqualTo(encounter);
    }

    @Test
    void removeParticipant_shouldRemoveParticipantFromList() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        Participant participant = new Participant(ParticipantType.PRACTITIONER);
        encounter.addParticipant(participant);

        // When
        encounter.removeParticipant(participant);

        // Then
        assertThat(encounter.getParticipants()).doesNotContain(participant);
        assertThat(participant.getEncounter()).isNull();
    }

    @Test
    void addReason_shouldAddReasonToList() {
        // Given
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        EncounterReason reason = new EncounterReason(ReasonType.CHIEF_COMPLAINT);

        // When
        encounter.addReason(reason);

        // Then
        assertThat(encounter.getReasons()).contains(reason);
        assertThat(reason.getEncounter()).isEqualTo(encounter);
    }
}
