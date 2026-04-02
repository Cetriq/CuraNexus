package se.curanexus.journal.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.curanexus.journal.service.exception.*;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoteNotFoundException.class)
    public ProblemDetail handleNoteNotFound(NoteNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Clinical Note Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/note-not-found"));
        problem.setProperty("noteId", ex.getNoteId());
        return problem;
    }

    @ExceptionHandler(DiagnosisNotFoundException.class)
    public ProblemDetail handleDiagnosisNotFound(DiagnosisNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Diagnosis Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/diagnosis-not-found"));
        problem.setProperty("diagnosisId", ex.getDiagnosisId());
        return problem;
    }

    @ExceptionHandler(ProcedureNotFoundException.class)
    public ProblemDetail handleProcedureNotFound(ProcedureNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Procedure Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/procedure-not-found"));
        problem.setProperty("procedureId", ex.getProcedureId());
        return problem;
    }

    @ExceptionHandler(ObservationNotFoundException.class)
    public ProblemDetail handleObservationNotFound(ObservationNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Observation Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/observation-not-found"));
        problem.setProperty("observationId", ex.getObservationId());
        return problem;
    }

    @ExceptionHandler(InvalidNoteStateException.class)
    public ProblemDetail handleInvalidNoteState(InvalidNoteStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid Note State");
        problem.setType(URI.create("https://curanexus.se/errors/invalid-note-state"));
        problem.setProperty("noteId", ex.getNoteId());
        problem.setProperty("currentStatus", ex.getCurrentStatus());
        problem.setProperty("attemptedAction", ex.getAttemptedAction());
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid State Transition");
        problem.setType(URI.create("https://curanexus.se/errors/invalid-state"));
        return problem;
    }
}
