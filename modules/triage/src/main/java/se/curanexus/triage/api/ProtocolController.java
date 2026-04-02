package se.curanexus.triage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.triage.api.dto.TriageProtocolResponse;
import se.curanexus.triage.service.TriageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/triage/protocols")
public class ProtocolController {

    private final TriageService triageService;

    public ProtocolController(TriageService triageService) {
        this.triageService = triageService;
    }

    @GetMapping
    public ResponseEntity<List<TriageProtocolResponse>> listProtocols(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "true") boolean active) {
        var protocols = triageService.listProtocols(category, active);
        return ResponseEntity.ok(protocols.stream().map(TriageProtocolResponse::fromEntity).toList());
    }

    @GetMapping("/{protocolId}")
    public ResponseEntity<TriageProtocolResponse> getProtocol(@PathVariable UUID protocolId) {
        return triageService.getProtocol(protocolId)
                .map(TriageProtocolResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
