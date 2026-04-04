package se.curanexus.forms.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.curanexus.forms.service.FormTemplateNotFoundException;
import se.curanexus.forms.service.FormSubmissionNotFoundException;
import se.curanexus.forms.service.FormValidationException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FormTemplateNotFoundException.class)
    public ProblemDetail handleFormTemplateNotFound(FormTemplateNotFoundException ex) {
        log.warn("Form template not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setType(URI.create("urn:curanexus:forms:template-not-found"));
        problem.setTitle("Formulärmall hittades inte");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(FormSubmissionNotFoundException.class)
    public ProblemDetail handleFormSubmissionNotFound(FormSubmissionNotFoundException ex) {
        log.warn("Form submission not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setType(URI.create("urn:curanexus:forms:submission-not-found"));
        problem.setTitle("Formulärinlämning hittades inte");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(FormValidationException.class)
    public ProblemDetail handleFormValidation(FormValidationException ex) {
        log.warn("Form validation error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setType(URI.create("urn:curanexus:forms:validation-error"));
        problem.setTitle("Valideringsfel");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Valideringsfel i indata"
        );
        problem.setType(URI.create("urn:curanexus:forms:validation-error"));
        problem.setTitle("Valideringsfel");
        problem.setProperty("timestamp", Instant.now());

        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setType(URI.create("urn:curanexus:forms:illegal-state"));
        problem.setTitle("Ogiltig operation");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ett oväntat fel uppstod"
        );
        problem.setType(URI.create("urn:curanexus:forms:internal-error"));
        problem.setTitle("Internt fel");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
