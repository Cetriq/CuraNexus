package se.curanexus.referral.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Referral - Domänmodell")
class ReferralTest {

    private UUID patientId;
    private UUID senderUnitId;
    private UUID senderPractitionerId;
    private UUID receiverUnitId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        senderUnitId = UUID.randomUUID();
        senderPractitionerId = UUID.randomUUID();
        receiverUnitId = UUID.randomUUID();
    }

    private Referral createReferral() {
        Referral referral = new Referral(patientId, senderUnitId, senderPractitionerId,
                ReferralType.SPECIALIST, "Bedömning av knäsmärta");
        referral.setReceiverUnitId(receiverUnitId);
        referral.setReceiverUnitName("Ortopedkliniken");
        return referral;
    }

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa remiss med DRAFT-status")
        void shouldCreateReferralWithDraftStatus() {
            Referral referral = createReferral();

            assertEquals(ReferralStatus.DRAFT, referral.getStatus());
            assertEquals(ReferralPriority.ROUTINE, referral.getPriority());
            assertNotNull(referral.getReferralReference());
            assertTrue(referral.getReferralReference().startsWith("REM-"));
            assertNotNull(referral.getCreatedAt());
        }

        @Test
        @DisplayName("Ska generera unik remissreferens")
        void shouldGenerateUniqueReferralReference() {
            Referral referral1 = createReferral();
            Referral referral2 = createReferral();

            assertNotEquals(referral1.getReferralReference(), referral2.getReferralReference());
        }
    }

    @Nested
    @DisplayName("Skicka remiss")
    class SendReferral {

        @Test
        @DisplayName("Ska kunna skicka remiss från DRAFT")
        void shouldSendReferralFromDraft() {
            Referral referral = createReferral();

            referral.send();

            assertEquals(ReferralStatus.SENT, referral.getStatus());
            assertNotNull(referral.getSentAt());
        }

        @Test
        @DisplayName("Ska kunna skicka remiss från PENDING_INFORMATION")
        void shouldSendReferralFromPendingInformation() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.requestMoreInformation("Behöver röntgenbilder");

            referral.send();

            assertEquals(ReferralStatus.SENT, referral.getStatus());
        }

        @Test
        @DisplayName("Ska ej kunna skicka redan skickad remiss")
        void shouldNotSendAlreadySentReferral() {
            Referral referral = createReferral();
            referral.send();

            assertThrows(IllegalStateException.class, referral::send);
        }

        @Test
        @DisplayName("Ska kräva mottagare eller specialitet")
        void shouldRequireReceiverOrSpecialty() {
            Referral referral = new Referral(patientId, senderUnitId, senderPractitionerId,
                    ReferralType.SPECIALIST, "Fråga");
            // Ingen mottagare satt

            assertThrows(IllegalStateException.class, referral::send);
        }

        @Test
        @DisplayName("Ska acceptera specialitet istället för specifik mottagare")
        void shouldAcceptSpecialtyInsteadOfReceiver() {
            Referral referral = new Referral(patientId, senderUnitId, senderPractitionerId,
                    ReferralType.SPECIALIST, "Fråga");
            referral.setRequestedSpecialty("Ortopedi");

            referral.send();

            assertEquals(ReferralStatus.SENT, referral.getStatus());
        }
    }

    @Nested
    @DisplayName("Mottagning")
    class MarkReceived {

        @Test
        @DisplayName("Ska kunna markera skickad remiss som mottagen")
        void shouldMarkSentReferralAsReceived() {
            Referral referral = createReferral();
            referral.send();

            referral.markReceived();

            assertEquals(ReferralStatus.RECEIVED, referral.getStatus());
            assertNotNull(referral.getReceivedAt());
        }

        @Test
        @DisplayName("Ska kunna markera vidareskickad remiss som mottagen")
        void shouldMarkForwardedReferralAsReceived() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.forward(UUID.randomUUID(), "Annan klinik", "Bättre expertis");

            referral.markReceived();

            assertEquals(ReferralStatus.RECEIVED, referral.getStatus());
        }

        @Test
        @DisplayName("Ska ej kunna markera draft som mottagen")
        void shouldNotMarkDraftAsReceived() {
            Referral referral = createReferral();

            assertThrows(IllegalStateException.class, referral::markReceived);
        }
    }

    @Nested
    @DisplayName("Bedömning")
    class Assessment {

        @Test
        @DisplayName("Ska kunna starta bedömning av mottagen remiss")
        void shouldStartAssessmentOfReceivedReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            UUID assessorId = UUID.randomUUID();

            referral.startAssessment(assessorId, "Dr. Andersson");

            assertEquals(ReferralStatus.UNDER_ASSESSMENT, referral.getStatus());
            assertEquals(assessorId, referral.getAssessorId());
            assertEquals("Dr. Andersson", referral.getAssessorName());
        }

        @Test
        @DisplayName("Ska ej kunna starta bedömning av draft")
        void shouldNotStartAssessmentOfDraft() {
            Referral referral = createReferral();

            assertThrows(IllegalStateException.class,
                    () -> referral.startAssessment(UUID.randomUUID(), "Dr. Test"));
        }
    }

    @Nested
    @DisplayName("Acceptera")
    class Accept {

        @Test
        @DisplayName("Ska kunna acceptera remiss under bedömning")
        void shouldAcceptReferralUnderAssessment() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.startAssessment(UUID.randomUUID(), "Dr. Test");
            LocalDate plannedDate = LocalDate.now().plusWeeks(2);

            referral.accept(ReferralPriority.URGENT, plannedDate);

            assertEquals(ReferralStatus.ACCEPTED, referral.getStatus());
            assertEquals(ReferralPriority.URGENT, referral.getPriority());
            assertEquals(plannedDate, referral.getRequestedDate());
            assertNotNull(referral.getAssessedAt());
        }

        @Test
        @DisplayName("Ska kunna acceptera mottagen remiss direkt")
        void shouldAcceptReceivedReferralDirectly() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();

            referral.accept(ReferralPriority.ROUTINE, LocalDate.now().plusMonths(1));

            assertEquals(ReferralStatus.ACCEPTED, referral.getStatus());
        }

        @Test
        @DisplayName("Ska ej kunna acceptera redan accepterad remiss")
        void shouldNotAcceptAlreadyAcceptedReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.accept(ReferralPriority.ROUTINE, LocalDate.now().plusWeeks(2));

            assertThrows(IllegalStateException.class,
                    () -> referral.accept(ReferralPriority.URGENT, LocalDate.now()));
        }
    }

    @Nested
    @DisplayName("Avvisa")
    class Reject {

        @Test
        @DisplayName("Ska kunna avvisa remiss under bedömning")
        void shouldRejectReferralUnderAssessment() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.startAssessment(UUID.randomUUID(), "Dr. Test");

            referral.reject("Faller utanför vårt kompetensområde");

            assertEquals(ReferralStatus.REJECTED, referral.getStatus());
            assertNotNull(referral.getAssessedAt());
        }

        @Test
        @DisplayName("Ska kunna avvisa mottagen remiss direkt")
        void shouldRejectReceivedReferralDirectly() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();

            referral.reject("Ofullständig information");

            assertEquals(ReferralStatus.REJECTED, referral.getStatus());
        }
    }

    @Nested
    @DisplayName("Begär komplettering")
    class RequestInformation {

        @Test
        @DisplayName("Ska kunna begära komplettering")
        void shouldRequestMoreInformation() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();

            referral.requestMoreInformation("Behöver resultat från tidigare röntgenundersökning");

            assertEquals(ReferralStatus.PENDING_INFORMATION, referral.getStatus());
        }

        @Test
        @DisplayName("Ska ej kunna begära komplettering av draft")
        void shouldNotRequestInfoFromDraft() {
            Referral referral = createReferral();

            assertThrows(IllegalStateException.class,
                    () -> referral.requestMoreInformation("Info"));
        }
    }

    @Nested
    @DisplayName("Vidareskicka")
    class Forward {

        @Test
        @DisplayName("Ska kunna vidareskicka mottagen remiss")
        void shouldForwardReceivedReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            UUID newReceiverId = UUID.randomUUID();

            referral.forward(newReceiverId, "Reumatologkliniken", "Misstänkt reumatisk sjukdom");

            assertEquals(ReferralStatus.FORWARDED, referral.getStatus());
            assertEquals(newReceiverId, referral.getReceiverUnitId());
            assertEquals("Reumatologkliniken", referral.getReceiverUnitName());
        }

        @Test
        @DisplayName("Ska ej kunna vidareskicka accepterad remiss")
        void shouldNotForwardAcceptedReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.accept(ReferralPriority.ROUTINE, LocalDate.now().plusWeeks(2));

            assertThrows(IllegalStateException.class,
                    () -> referral.forward(UUID.randomUUID(), "Annan", "Orsak"));
        }
    }

    @Nested
    @DisplayName("Slutföra")
    class Complete {

        @Test
        @DisplayName("Ska kunna slutföra accepterad remiss")
        void shouldCompleteAcceptedReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.accept(ReferralPriority.ROUTINE, LocalDate.now().plusWeeks(2));
            UUID encounterId = UUID.randomUUID();

            referral.complete(encounterId);

            assertEquals(ReferralStatus.COMPLETED, referral.getStatus());
            assertEquals(encounterId, referral.getResultingEncounterId());
            assertNotNull(referral.getCompletedAt());
        }

        @Test
        @DisplayName("Ska ej kunna slutföra icke-accepterad remiss")
        void shouldNotCompleteNonAcceptedReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();

            assertThrows(IllegalStateException.class,
                    () -> referral.complete(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("Makulera")
    class Cancel {

        @Test
        @DisplayName("Ska kunna makulera draft")
        void shouldCancelDraft() {
            Referral referral = createReferral();

            referral.cancel("Felaktigt skapad");

            assertEquals(ReferralStatus.CANCELLED, referral.getStatus());
        }

        @Test
        @DisplayName("Ska kunna makulera skickad remiss")
        void shouldCancelSentReferral() {
            Referral referral = createReferral();
            referral.send();

            referral.cancel("Patient avbokad");

            assertEquals(ReferralStatus.CANCELLED, referral.getStatus());
        }

        @Test
        @DisplayName("Ska ej kunna makulera slutförd remiss")
        void shouldNotCancelCompletedReferral() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.accept(ReferralPriority.ROUTINE, LocalDate.now().plusWeeks(2));
            referral.complete(UUID.randomUUID());

            assertThrows(IllegalStateException.class,
                    () -> referral.cancel("Försök"));
        }
    }

    @Nested
    @DisplayName("Hjälpmetoder")
    class HelperMethods {

        @Test
        @DisplayName("Ska returnera true för redigerbar draft")
        void shouldReturnTrueForEditableDraft() {
            Referral referral = createReferral();

            assertTrue(referral.isEditable());
        }

        @Test
        @DisplayName("Ska returnera true för redigerbar pending_information")
        void shouldReturnTrueForEditablePendingInfo() {
            Referral referral = createReferral();
            referral.send();
            referral.markReceived();
            referral.requestMoreInformation("Info");

            assertTrue(referral.isEditable());
        }

        @Test
        @DisplayName("Ska returnera false för icke-redigerbar status")
        void shouldReturnFalseForNonEditableStatus() {
            Referral referral = createReferral();
            referral.send();

            assertFalse(referral.isEditable());
        }

        @Test
        @DisplayName("Ska returnera true för väntande på svar")
        void shouldReturnTrueForAwaitingResponse() {
            Referral referral = createReferral();
            referral.send();

            assertTrue(referral.isAwaitingResponse());
        }
    }

    @Nested
    @DisplayName("Response-hantering")
    class ResponseHandling {

        @Test
        @DisplayName("Ska kunna lägga till response")
        void shouldAddResponse() {
            Referral referral = createReferral();
            ReferralResponse response = ReferralResponse.createAcceptance(
                    referral, UUID.randomUUID(), ReferralPriority.URGENT,
                    LocalDate.now().plusWeeks(1), "Accepterad");

            referral.addResponse(response);

            assertEquals(1, referral.getResponses().size());
            assertEquals(referral, response.getReferral());
        }
    }
}
