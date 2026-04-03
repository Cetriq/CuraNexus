package se.curanexus.referral.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReferralResponse - Domänmodell")
class ReferralResponseTest {

    private Referral referral;
    private UUID responderId;

    @BeforeEach
    void setUp() {
        referral = new Referral(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ReferralType.SPECIALIST,
                "Bedömning av knäsmärta"
        );
        responderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Ska skapa acceptans-svar")
    void shouldCreateAcceptanceResponse() {
        LocalDate plannedDate = LocalDate.now().plusWeeks(2);

        ReferralResponse response = ReferralResponse.createAcceptance(
                referral, responderId, ReferralPriority.URGENT, plannedDate, "Accepterad för utredning");

        assertEquals(ReferralResponseType.ACCEPTANCE, response.getResponseType());
        assertEquals(responderId, response.getResponderId());
        assertEquals(ReferralPriority.URGENT, response.getAssessedPriority());
        assertEquals(plannedDate, response.getPlannedDate());
        assertEquals("Accepterad för utredning", response.getResponseText());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("Ska skapa avvisnings-svar")
    void shouldCreateRejectionResponse() {
        String rejectionReason = "Faller utanför vårt kompetensområde";

        ReferralResponse response = ReferralResponse.createRejection(
                referral, responderId, rejectionReason);

        assertEquals(ReferralResponseType.REJECTION, response.getResponseType());
        assertEquals(rejectionReason, response.getRejectionReason());
        assertEquals(rejectionReason, response.getResponseText());
    }

    @Test
    @DisplayName("Ska skapa begäran om komplettering")
    void shouldCreateInformationRequestResponse() {
        String requestedInfo = "Behöver resultat från tidigare röntgenundersökning";

        ReferralResponse response = ReferralResponse.createInformationRequest(
                referral, responderId, requestedInfo);

        assertEquals(ReferralResponseType.INFORMATION_REQUEST, response.getResponseType());
        assertEquals(requestedInfo, response.getRequestedInformation());
        assertEquals(requestedInfo, response.getResponseText());
    }

    @Test
    @DisplayName("Ska skapa vidareskicknings-svar")
    void shouldCreateForwardResponse() {
        UUID forwardedToUnitId = UUID.randomUUID();
        String forwardedToUnitName = "Reumatologkliniken";
        String forwardReason = "Misstänkt reumatisk sjukdom";

        ReferralResponse response = ReferralResponse.createForward(
                referral, responderId, forwardedToUnitId, forwardedToUnitName, forwardReason);

        assertEquals(ReferralResponseType.FORWARDED, response.getResponseType());
        assertEquals(forwardedToUnitId, response.getForwardedToUnitId());
        assertEquals(forwardedToUnitName, response.getForwardedToUnitName());
        assertEquals(forwardReason, response.getForwardReason());
        assertTrue(response.getResponseText().contains(forwardedToUnitName));
    }

    @Test
    @DisplayName("Ska skapa slutgiltigt svar")
    void shouldCreateFinalResponse() {
        String responseText = "Utredning genomförd. Patient diagnosticerad med X.";

        ReferralResponse response = ReferralResponse.createFinalResponse(
                referral, responderId, responseText);

        assertEquals(ReferralResponseType.FINAL_RESPONSE, response.getResponseType());
        assertEquals(responseText, response.getResponseText());
    }

    @Test
    @DisplayName("Ska kunna sätta responder-information")
    void shouldSetResponderInfo() {
        ReferralResponse response = ReferralResponse.createAcceptance(
                referral, responderId, ReferralPriority.ROUTINE,
                LocalDate.now().plusMonths(1), "Accepterad");

        response.setResponderUnitId(UUID.randomUUID());
        response.setResponderUnitName("Ortopedkliniken");
        response.setResponderHsaId("SE2321000016-1234");
        response.setResponderName("Dr. Andersson");

        assertEquals("Ortopedkliniken", response.getResponderUnitName());
        assertEquals("SE2321000016-1234", response.getResponderHsaId());
        assertEquals("Dr. Andersson", response.getResponderName());
    }
}
