package se.curanexus.referral.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Remissvar - svar eller meddelande kopplat till en remiss.
 */
@Entity
@Table(name = "referral_responses", indexes = {
    @Index(name = "idx_response_referral", columnList = "referral_id"),
    @Index(name = "idx_response_type", columnList = "response_type"),
    @Index(name = "idx_response_created", columnList = "created_at")
})
public class ReferralResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Remiss som svaret tillhör */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_id", nullable = false)
    private Referral referral;

    /** Typ av svar */
    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", nullable = false, length = 30)
    private ReferralResponseType responseType;

    /** Svarandes enhet ID */
    @Column(name = "responder_unit_id")
    private UUID responderUnitId;

    /** Svarandes enhet namn */
    @Column(name = "responder_unit_name", length = 200)
    private String responderUnitName;

    /** Svarandes ID */
    @Column(name = "responder_id", nullable = false)
    private UUID responderId;

    /** Svarandes HSA-ID */
    @Column(name = "responder_hsa_id", length = 50)
    private String responderHsaId;

    /** Svarandes namn */
    @Column(name = "responder_name", length = 200)
    private String responderName;

    /** Svarstext */
    @Column(name = "response_text", length = 4000)
    private String responseText;

    /** Bedömd prioritet (vid acceptans) */
    @Enumerated(EnumType.STRING)
    @Column(name = "assessed_priority", length = 20)
    private ReferralPriority assessedPriority;

    /** Planerat besöksdatum (vid acceptans) */
    @Column(name = "planned_date")
    private LocalDate plannedDate;

    /** Avvisningsorsak (vid rejektion) */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    /** Begärd information (vid kompletteringsbegäran) */
    @Column(name = "requested_information", length = 2000)
    private String requestedInformation;

    /** Vidareskickad till enhet ID */
    @Column(name = "forwarded_to_unit_id")
    private UUID forwardedToUnitId;

    /** Vidareskickad till enhet namn */
    @Column(name = "forwarded_to_unit_name", length = 200)
    private String forwardedToUnitName;

    /** Orsak till vidareskickning */
    @Column(name = "forward_reason", length = 1000)
    private String forwardReason;

    /** Bifogade dokument-ID:n */
    @Column(name = "attachment_ids", length = 1000)
    private String attachmentIds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReferralResponse() {
    }

    public ReferralResponse(Referral referral, ReferralResponseType responseType, UUID responderId) {
        this.referral = referral;
        this.responseType = responseType;
        this.responderId = responderId;
        this.createdAt = Instant.now();
    }

    // === Factory methods för olika svarstyper ===

    public static ReferralResponse createAcceptance(Referral referral, UUID responderId,
                                                     ReferralPriority assessedPriority,
                                                     LocalDate plannedDate, String responseText) {
        ReferralResponse response = new ReferralResponse(referral, ReferralResponseType.ACCEPTANCE, responderId);
        response.setAssessedPriority(assessedPriority);
        response.setPlannedDate(plannedDate);
        response.setResponseText(responseText);
        return response;
    }

    public static ReferralResponse createRejection(Referral referral, UUID responderId,
                                                    String rejectionReason) {
        ReferralResponse response = new ReferralResponse(referral, ReferralResponseType.REJECTION, responderId);
        response.setRejectionReason(rejectionReason);
        response.setResponseText(rejectionReason);
        return response;
    }

    public static ReferralResponse createInformationRequest(Referral referral, UUID responderId,
                                                             String requestedInfo) {
        ReferralResponse response = new ReferralResponse(referral, ReferralResponseType.INFORMATION_REQUEST, responderId);
        response.setRequestedInformation(requestedInfo);
        response.setResponseText(requestedInfo);
        return response;
    }

    public static ReferralResponse createForward(Referral referral, UUID responderId,
                                                  UUID forwardedToUnitId, String forwardedToUnitName,
                                                  String forwardReason) {
        ReferralResponse response = new ReferralResponse(referral, ReferralResponseType.FORWARDED, responderId);
        response.setForwardedToUnitId(forwardedToUnitId);
        response.setForwardedToUnitName(forwardedToUnitName);
        response.setForwardReason(forwardReason);
        response.setResponseText("Vidareskickad till " + forwardedToUnitName + ": " + forwardReason);
        return response;
    }

    public static ReferralResponse createFinalResponse(Referral referral, UUID responderId,
                                                        String responseText) {
        ReferralResponse response = new ReferralResponse(referral, ReferralResponseType.FINAL_RESPONSE, responderId);
        response.setResponseText(responseText);
        return response;
    }

    // === Getters och setters ===

    public UUID getId() {
        return id;
    }

    public Referral getReferral() {
        return referral;
    }

    public void setReferral(Referral referral) {
        this.referral = referral;
    }

    public ReferralResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ReferralResponseType responseType) {
        this.responseType = responseType;
    }

    public UUID getResponderUnitId() {
        return responderUnitId;
    }

    public void setResponderUnitId(UUID responderUnitId) {
        this.responderUnitId = responderUnitId;
    }

    public String getResponderUnitName() {
        return responderUnitName;
    }

    public void setResponderUnitName(String responderUnitName) {
        this.responderUnitName = responderUnitName;
    }

    public UUID getResponderId() {
        return responderId;
    }

    public String getResponderHsaId() {
        return responderHsaId;
    }

    public void setResponderHsaId(String responderHsaId) {
        this.responderHsaId = responderHsaId;
    }

    public String getResponderName() {
        return responderName;
    }

    public void setResponderName(String responderName) {
        this.responderName = responderName;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public ReferralPriority getAssessedPriority() {
        return assessedPriority;
    }

    public void setAssessedPriority(ReferralPriority assessedPriority) {
        this.assessedPriority = assessedPriority;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDate plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRequestedInformation() {
        return requestedInformation;
    }

    public void setRequestedInformation(String requestedInformation) {
        this.requestedInformation = requestedInformation;
    }

    public UUID getForwardedToUnitId() {
        return forwardedToUnitId;
    }

    public void setForwardedToUnitId(UUID forwardedToUnitId) {
        this.forwardedToUnitId = forwardedToUnitId;
    }

    public String getForwardedToUnitName() {
        return forwardedToUnitName;
    }

    public void setForwardedToUnitName(String forwardedToUnitName) {
        this.forwardedToUnitName = forwardedToUnitName;
    }

    public String getForwardReason() {
        return forwardReason;
    }

    public void setForwardReason(String forwardReason) {
        this.forwardReason = forwardReason;
    }

    public String getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(String attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
