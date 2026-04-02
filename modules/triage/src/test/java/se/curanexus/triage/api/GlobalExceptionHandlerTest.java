package se.curanexus.triage.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.curanexus.triage.service.exception.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("should handle AssessmentNotFoundException")
    void shouldHandleAssessmentNotFoundException() {
        UUID assessmentId = UUID.randomUUID();
        AssessmentNotFoundException ex = new AssessmentNotFoundException(assessmentId);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAssessmentNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertTrue(response.getBody().message().contains(assessmentId.toString()));
    }

    @Test
    @DisplayName("should handle AssessmentAlreadyExistsException")
    void shouldHandleAssessmentAlreadyExistsException() {
        UUID encounterId = UUID.randomUUID();
        AssessmentAlreadyExistsException ex = new AssessmentAlreadyExistsException(encounterId);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAssessmentAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
    }

    @Test
    @DisplayName("should handle AssessmentAlreadyCompletedException")
    void shouldHandleAssessmentAlreadyCompletedException() {
        UUID assessmentId = UUID.randomUUID();
        AssessmentAlreadyCompletedException ex = new AssessmentAlreadyCompletedException(assessmentId);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAssessmentAlreadyCompleted(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
    }

    @Test
    @DisplayName("should handle IllegalArgumentException")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid priority value");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Invalid priority value", response.getBody().message());
    }

    @Test
    @DisplayName("should handle generic exception")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    @DisplayName("ErrorResponse should have timestamp")
    void errorResponseShouldHaveTimestamp() {
        GlobalExceptionHandler.ErrorResponse response = new GlobalExceptionHandler.ErrorResponse(
                400, "Bad Request", "Test message"
        );

        assertNotNull(response.timestamp());
        assertEquals(400, response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Test message", response.message());
        assertNull(response.details());
    }
}
