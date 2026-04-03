package se.curanexus.referral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.referral.api.dto.AssessReferralRequest;
import se.curanexus.referral.api.dto.CreateReferralRequest;
import se.curanexus.referral.api.dto.ForwardReferralRequest;
import se.curanexus.referral.api.dto.ReferralDto;
import se.curanexus.referral.domain.*;
import se.curanexus.referral.repository.ReferralRepository;
import se.curanexus.referral.repository.ReferralResponseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReferralService")
class ReferralServiceTest {

    @Mock
    private ReferralRepository referralRepository;

    @Mock
    private ReferralResponseRepository responseRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ReferralService referralService;

    private UUID patientId;
    private UUID senderUnitId;
    private UUID senderPractitionerId;
    private UUID receiverUnitId;

    @BeforeEach
    void setUp() {
        referralService = new ReferralService(referralRepository, responseRepository, eventPublisher);
        patientId = UUID.randomUUID();
        senderUnitId = UUID.randomUUID();
        senderPractitionerId = UUID.randomUUID();
        receiverUnitId = UUID.randomUUID();
    }

    private CreateReferralRequest createRequest() {
        return new CreateReferralRequest(
                patientId,
                "199001011234",
                "Anna Andersson",
                ReferralType.SPECIALIST,
                ReferralPriority.URGENT,
                "SE2321000016-1234",
                "Vårdcentralen Centrum",
                "SE2321000016-5678",
                "Dr. Eriksson",
                receiverUnitId,
                "SE2321000016-9999",
                "Ortopedkliniken",
                null, // requestedSpecialty
                "Bedömning av knäsmärta vid gång",
                "M17.1",
                "Artros i knäled",
                "Patienten har haft knäbesvär i 6 månader",
                "Svullnad och smärta vid belastning",
                "Röntgen utan anmärkning",
                "Paracetamol 500mg vid behov",
                "Inga kända",
                null,
                null, // sourceEncounterId
                LocalDate.now().plusWeeks(2),
                LocalDate.now().plusMonths(6),
                false
        );
    }

    private Referral createReferral() {
        Referral referral = new Referral(patientId, senderUnitId, senderPractitionerId,
                ReferralType.SPECIALIST, "Bedömning av knäsmärta");
        referral.setReceiverUnitId(receiverUnitId);
        referral.setReceiverUnitName("Ortopedkliniken");
        return referral;
    }

    @Nested
    @DisplayName("createReferral")
    class CreateReferral {

        @Test
        @DisplayName("Ska skapa och spara remiss")
        void shouldCreateAndSaveReferral() {
            CreateReferralRequest request = createRequest();

            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.createReferral(request, senderUnitId, senderPractitionerId);

            assertNotNull(result);
            assertEquals(patientId, result.patientId());
            assertEquals(ReferralType.SPECIALIST, result.referralType());
            assertEquals(ReferralStatus.DRAFT, result.status());
            assertEquals(ReferralPriority.URGENT, result.priority());

            verify(referralRepository).save(any(Referral.class));
            verify(eventPublisher).publish(any(ReferralService.ReferralCreatedEvent.class));
        }

        @Test
        @DisplayName("Ska skicka remiss direkt om sendImmediately är true")
        void shouldSendImmediatelyWhenRequested() {
            CreateReferralRequest request = new CreateReferralRequest(
                    patientId, "199001011234", "Anna Andersson",
                    ReferralType.SPECIALIST, ReferralPriority.ROUTINE,
                    null, "Vårdcentral", null, "Dr. Test",
                    receiverUnitId, null, "Ortopedkliniken", null,
                    "Fråga", null, null, null, null, null, null, null, null,
                    null, null, null,
                    true // sendImmediately
            );

            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.createReferral(request, senderUnitId, senderPractitionerId);

            assertEquals(ReferralStatus.SENT, result.status());
        }
    }

    @Nested
    @DisplayName("getReferral")
    class GetReferral {

        @Test
        @DisplayName("Ska returnera remiss via ID")
        void shouldReturnReferralById() {
            Referral referral = createReferral();
            UUID referralId = UUID.randomUUID();
            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));

            ReferralDto result = referralService.getReferral(referralId);

            assertNotNull(result);
            assertEquals(patientId, result.patientId());
        }

        @Test
        @DisplayName("Ska kasta exception om remiss ej finns")
        void shouldThrowWhenNotFound() {
            UUID referralId = UUID.randomUUID();
            when(referralRepository.findById(referralId)).thenReturn(Optional.empty());

            assertThrows(ReferralNotFoundException.class,
                    () -> referralService.getReferral(referralId));
        }
    }

    @Nested
    @DisplayName("sendReferral")
    class SendReferral {

        @Test
        @DisplayName("Ska skicka remiss och publicera event")
        void shouldSendReferralAndPublishEvent() {
            Referral referral = createReferral();
            UUID referralId = UUID.randomUUID();

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.sendReferral(referralId);

            assertEquals(ReferralStatus.SENT, result.status());
            verify(eventPublisher).publish(any(ReferralService.ReferralSentEvent.class));
        }
    }

    @Nested
    @DisplayName("markReceived")
    class MarkReceived {

        @Test
        @DisplayName("Ska markera remiss som mottagen")
        void shouldMarkReferralAsReceived() {
            Referral referral = createReferral();
            referral.send();
            UUID referralId = UUID.randomUUID();

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.markReceived(referralId);

            assertEquals(ReferralStatus.RECEIVED, result.status());
            verify(eventPublisher).publish(any(ReferralService.ReferralReceivedEvent.class));
        }
    }

    @Nested
    @DisplayName("assessReferral")
    class AssessReferral {

        @Test
        @DisplayName("Ska acceptera remiss")
        void shouldAcceptReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            UUID referralId = UUID.randomUUID();
            UUID assessorId = UUID.randomUUID();
            LocalDate plannedDate = LocalDate.now().plusWeeks(2);

            AssessReferralRequest request = new AssessReferralRequest(
                    AssessReferralRequest.AssessmentDecision.ACCEPT,
                    ReferralPriority.SEMI_URGENT,
                    plannedDate,
                    null, null,
                    "Accepterad för utredning"
            );

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.assessReferral(referralId, request, assessorId, "Dr. Andersson");

            assertEquals(ReferralStatus.ACCEPTED, result.status());
            assertEquals(ReferralPriority.SEMI_URGENT, result.priority());
            assertEquals(1, result.responses().size());

            verify(eventPublisher).publish(any(ReferralService.ReferralAcceptedEvent.class));
        }

        @Test
        @DisplayName("Ska avvisa remiss")
        void shouldRejectReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            UUID referralId = UUID.randomUUID();
            UUID assessorId = UUID.randomUUID();

            AssessReferralRequest request = new AssessReferralRequest(
                    AssessReferralRequest.AssessmentDecision.REJECT,
                    null, null,
                    "Faller utanför vårt kompetensområde",
                    null, null
            );

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.assessReferral(referralId, request, assessorId, "Dr. Andersson");

            assertEquals(ReferralStatus.REJECTED, result.status());

            ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());
            assertTrue(eventCaptor.getValue() instanceof ReferralService.ReferralRejectedEvent);
        }

        @Test
        @DisplayName("Ska begära komplettering")
        void shouldRequestInformation() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            UUID referralId = UUID.randomUUID();
            UUID assessorId = UUID.randomUUID();

            AssessReferralRequest request = new AssessReferralRequest(
                    AssessReferralRequest.AssessmentDecision.REQUEST_INFORMATION,
                    null, null, null,
                    "Behöver röntgenbilder",
                    null
            );

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.assessReferral(referralId, request, assessorId, "Dr. Andersson");

            assertEquals(ReferralStatus.PENDING_INFORMATION, result.status());
            verify(eventPublisher).publish(any(ReferralService.ReferralInformationRequestedEvent.class));
        }
    }

    @Nested
    @DisplayName("forwardReferral")
    class ForwardReferral {

        @Test
        @DisplayName("Ska vidareskicka remiss")
        void shouldForwardReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            UUID referralId = UUID.randomUUID();
            UUID forwarderId = UUID.randomUUID();
            UUID targetUnitId = UUID.randomUUID();

            ForwardReferralRequest request = new ForwardReferralRequest(
                    targetUnitId,
                    "SE2321000016-7777",
                    "Reumatologkliniken",
                    "Misstänkt reumatisk sjukdom"
            );

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.forwardReferral(referralId, request, forwarderId, "Dr. Test");

            assertEquals(ReferralStatus.FORWARDED, result.status());
            assertEquals(targetUnitId, result.receiverUnitId());
            assertEquals("Reumatologkliniken", result.receiverUnitName());

            verify(eventPublisher).publish(any(ReferralService.ReferralForwardedEvent.class));
        }
    }

    @Nested
    @DisplayName("completeReferral")
    class CompleteReferral {

        @Test
        @DisplayName("Ska slutföra remiss med vårdkontakt")
        void shouldCompleteReferralWithEncounter() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.accept(ReferralPriority.ROUTINE, LocalDate.now().plusWeeks(2));
            UUID referralId = UUID.randomUUID();
            UUID encounterId = UUID.randomUUID();
            UUID responderId = UUID.randomUUID();

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.completeReferral(
                    referralId, encounterId, "Utredning genomförd", responderId, "Dr. Test");

            assertEquals(ReferralStatus.COMPLETED, result.status());
            assertEquals(encounterId, result.resultingEncounterId());

            verify(eventPublisher).publish(any(ReferralService.ReferralCompletedEvent.class));
        }

        @Test
        @DisplayName("Ska slutföra utan slutsvar om inget anges")
        void shouldCompleteWithoutFinalResponse() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.accept(ReferralPriority.ROUTINE, LocalDate.now().plusWeeks(2));
            UUID referralId = UUID.randomUUID();
            UUID encounterId = UUID.randomUUID();

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.completeReferral(
                    referralId, encounterId, null, null, null);

            assertEquals(ReferralStatus.COMPLETED, result.status());
            // Inget final response läggs till
            assertEquals(0, result.responses().size());
        }
    }

    @Nested
    @DisplayName("cancelReferral")
    class CancelReferral {

        @Test
        @DisplayName("Ska makulera remiss")
        void shouldCancelReferral() {
            Referral referral = createReferral();
            UUID referralId = UUID.randomUUID();

            when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
            when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> inv.getArgument(0));

            ReferralDto result = referralService.cancelReferral(referralId, "Felaktigt skapad");

            assertEquals(ReferralStatus.CANCELLED, result.status());
            verify(eventPublisher).publish(any(ReferralService.ReferralCancelledEvent.class));
        }
    }

    @Nested
    @DisplayName("Sökningar")
    class Searches {

        @Test
        @DisplayName("Ska hämta patientens remisser")
        void shouldGetPatientReferrals() {
            Referral referral1 = createReferral();
            Referral referral2 = createReferral();
            when(referralRepository.findByPatientIdOrderByCreatedAtDesc(patientId))
                    .thenReturn(List.of(referral1, referral2));

            List<ReferralDto> result = referralService.getPatientReferrals(patientId);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Ska hämta väntande bedömningar för enhet")
        void shouldGetPendingAssessments() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            when(referralRepository.findPendingAssessmentByUnit(receiverUnitId))
                    .thenReturn(List.of(referral));

            List<ReferralDto> result = referralService.getPendingAssessments(receiverUnitId);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Ska räkna väntande bedömningar")
        void shouldCountPendingAssessments() {
            when(referralRepository.countPendingByReceiverUnit(receiverUnitId)).thenReturn(5L);

            long count = referralService.countPendingAssessments(receiverUnitId);

            assertEquals(5L, count);
        }
    }
}
