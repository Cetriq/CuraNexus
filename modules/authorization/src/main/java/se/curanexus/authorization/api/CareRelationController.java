package se.curanexus.authorization.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.authorization.api.dto.CareRelationResponse;
import se.curanexus.authorization.api.dto.CreateCareRelationRequest;
import se.curanexus.authorization.domain.CareRelation;
import se.curanexus.authorization.service.AuthorizationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/care-relations")
public class CareRelationController {

    private final AuthorizationService authorizationService;

    public CareRelationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<CareRelationResponse> createCareRelation(
            @Valid @RequestBody CreateCareRelationRequest request) {

        CareRelation relation;
        if (request.encounterId() != null) {
            relation = authorizationService.createCareRelationForEncounter(
                    request.userId(),
                    request.patientId(),
                    request.encounterId(),
                    request.relationType()
            );
        } else {
            relation = authorizationService.createCareRelation(
                    request.userId(),
                    request.patientId(),
                    request.relationType(),
                    request.reason()
            );
        }

        if (request.validFrom() != null || request.validUntil() != null) {
            relation = authorizationService.updateCareRelationValidity(
                    relation.getId(),
                    request.validFrom(),
                    request.validUntil()
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(CareRelationResponse.from(relation));
    }

    @GetMapping("/{relationId}")
    public ResponseEntity<CareRelationResponse> getCareRelation(@PathVariable UUID relationId) {
        CareRelation relation = authorizationService.getCareRelation(relationId);
        return ResponseEntity.ok(CareRelationResponse.from(relation));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CareRelationResponse>> getCareRelationsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        List<CareRelation> relations;
        if (activeOnly) {
            relations = authorizationService.getActiveCareRelationsByUser(userId);
        } else {
            relations = authorizationService.getCareRelationsByUser(userId);
        }

        List<CareRelationResponse> responses = relations.stream()
                .map(CareRelationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<CareRelationResponse>> getCareRelationsByPatient(
            @PathVariable UUID patientId) {

        List<CareRelation> relations = authorizationService.getCareRelationsByPatient(patientId);
        List<CareRelationResponse> responses = relations.stream()
                .map(CareRelationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{relationId}/validity")
    public ResponseEntity<CareRelationResponse> updateValidity(
            @PathVariable UUID relationId,
            @RequestParam(required = false) LocalDateTime validFrom,
            @RequestParam(required = false) LocalDateTime validUntil) {

        CareRelation relation = authorizationService.updateCareRelationValidity(
                relationId, validFrom, validUntil);
        return ResponseEntity.ok(CareRelationResponse.from(relation));
    }

    @PostMapping("/{relationId}/end")
    public ResponseEntity<Void> endCareRelation(
            @PathVariable UUID relationId,
            @RequestParam UUID endedById) {

        authorizationService.endCareRelation(relationId, endedById);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/patients")
    public ResponseEntity<List<UUID>> getAccessiblePatients(@PathVariable UUID userId) {
        List<UUID> patientIds = authorizationService.getAccessiblePatients(userId);
        return ResponseEntity.ok(patientIds);
    }

    @GetMapping("/patient/{patientId}/providers")
    public ResponseEntity<List<UUID>> getCareProviders(@PathVariable UUID patientId) {
        List<UUID> userIds = authorizationService.getCareProvidersForPatient(patientId);
        return ResponseEntity.ok(userIds);
    }
}
