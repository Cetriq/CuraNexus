package se.curanexus.booking.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.booking.api.dto.*;
import se.curanexus.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST API för bokningshantering.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bokningar", description = "API för att hantera patientbokningar")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Skapa ny bokning", description = "Skapar en ny patientbokning")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bokning skapad",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Ogiltig request"),
            @ApiResponse(responseCode = "409", description = "Bokningskonflikt")
    })
    public ResponseEntity<AppointmentDto> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        AppointmentDto appointment = bookingService.createAppointment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Hämta bokning", description = "Hämtar en bokning via ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bokning hittad"),
            @ApiResponse(responseCode = "404", description = "Bokning finns inte")
    })
    public ResponseEntity<AppointmentDto> getAppointment(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(bookingService.getAppointment(appointmentId));
    }

    @GetMapping("/reference/{bookingReference}")
    @Operation(summary = "Hämta bokning via referens", description = "Hämtar en bokning via bokningsreferens")
    public ResponseEntity<AppointmentDto> getAppointmentByReference(
            @Parameter(description = "Bokningsreferens") @PathVariable String bookingReference) {
        return ResponseEntity.ok(bookingService.getAppointmentByReference(bookingReference));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta patientens bokningar", description = "Hämtar alla bokningar för en patient")
    public ResponseEntity<List<AppointmentDto>> getPatientAppointments(
            @Parameter(description = "Patient-ID") @PathVariable UUID patientId) {
        return ResponseEntity.ok(bookingService.getPatientAppointments(patientId));
    }

    @GetMapping("/patient/{patientId}/upcoming")
    @Operation(summary = "Hämta kommande bokningar", description = "Hämtar patientens kommande bokningar")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointments(
            @Parameter(description = "Patient-ID") @PathVariable UUID patientId) {
        return ResponseEntity.ok(bookingService.getUpcomingAppointments(patientId));
    }

    @GetMapping("/practitioner/{practitionerId}")
    @Operation(summary = "Hämta vårdgivarens bokningar", description = "Hämtar bokningar för en vårdgivare på ett specifikt datum")
    public ResponseEntity<List<AppointmentDto>> getPractitionerAppointments(
            @Parameter(description = "Vårdgivar-ID") @PathVariable UUID practitionerId,
            @Parameter(description = "Datum (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(bookingService.getPractitionerAppointments(practitionerId, date));
    }

    @PostMapping("/{appointmentId}/cancel")
    @Operation(summary = "Avboka", description = "Avbokar en tid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bokning avbokad"),
            @ApiResponse(responseCode = "400", description = "Kan inte avboka"),
            @ApiResponse(responseCode = "404", description = "Bokning finns inte")
    })
    public ResponseEntity<AppointmentDto> cancelAppointment(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(bookingService.cancelAppointment(appointmentId, request, userId));
    }

    @PostMapping("/{appointmentId}/reschedule")
    @Operation(summary = "Omboka", description = "Ombokar till ny tid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bokning ombokad"),
            @ApiResponse(responseCode = "400", description = "Kan inte omboka"),
            @ApiResponse(responseCode = "409", description = "Tidkonflikt")
    })
    public ResponseEntity<AppointmentDto> rescheduleAppointment(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId,
            @Valid @RequestBody RescheduleRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(bookingService.rescheduleAppointment(appointmentId, request, userId));
    }

    @PostMapping("/{appointmentId}/check-in")
    @Operation(summary = "Checka in", description = "Checkar in patient för sitt besök")
    public ResponseEntity<AppointmentDto> checkIn(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(bookingService.checkIn(appointmentId));
    }

    @PostMapping("/{appointmentId}/start")
    @Operation(summary = "Starta besök", description = "Markerar besöket som påbörjat")
    public ResponseEntity<AppointmentDto> startVisit(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(bookingService.startVisit(appointmentId));
    }

    @PostMapping("/{appointmentId}/complete")
    @Operation(summary = "Avsluta besök", description = "Markerar besöket som avslutat")
    public ResponseEntity<AppointmentDto> completeVisit(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(bookingService.completeVisit(appointmentId));
    }

    @PostMapping("/{appointmentId}/no-show")
    @Operation(summary = "Markera utebliven", description = "Markerar att patienten uteblev")
    public ResponseEntity<AppointmentDto> markNoShow(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(bookingService.markNoShow(appointmentId));
    }

    @PostMapping("/{appointmentId}/link-encounter/{encounterId}")
    @Operation(summary = "Koppla vårdkontakt", description = "Kopplar bokningen till en vårdkontakt")
    public ResponseEntity<AppointmentDto> linkToEncounter(
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId,
            @Parameter(description = "Vårdkontakt-ID") @PathVariable UUID encounterId) {
        return ResponseEntity.ok(bookingService.linkToEncounter(appointmentId, encounterId));
    }

    @GetMapping("/search")
    @Operation(summary = "Sök bokningar", description = "Söker bokningar med olika filter")
    public ResponseEntity<Page<AppointmentDto>> searchAppointments(
            @Parameter(description = "Patient-ID") @RequestParam(required = false) UUID patientId,
            @Parameter(description = "Vårdgivar-ID") @RequestParam(required = false) UUID practitionerId,
            @Parameter(description = "Enhets-ID") @RequestParam(required = false) UUID unitId,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Typ") @RequestParam(required = false) String appointmentType,
            @Parameter(description = "Från datum")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "Till datum")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Pageable pageable) {

        AppointmentSearchRequest request = new AppointmentSearchRequest(
                patientId,
                practitionerId,
                unitId,
                status != null ? se.curanexus.booking.domain.AppointmentStatus.valueOf(status) : null,
                appointmentType != null ? se.curanexus.booking.domain.AppointmentType.valueOf(appointmentType) : null,
                fromDate,
                toDate
        );

        return ResponseEntity.ok(bookingService.searchAppointments(request, pageable));
    }
}
