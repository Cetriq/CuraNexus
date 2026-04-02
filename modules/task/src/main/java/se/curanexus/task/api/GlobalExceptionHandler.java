package se.curanexus.task.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.curanexus.task.service.exception.*;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFound(TaskNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Task Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/task-not-found"));
        problem.setProperty("taskId", ex.getTaskId());
        return problem;
    }

    @ExceptionHandler(ReminderNotFoundException.class)
    public ProblemDetail handleReminderNotFound(ReminderNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Reminder Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/reminder-not-found"));
        problem.setProperty("reminderId", ex.getReminderId());
        return problem;
    }

    @ExceptionHandler(DelegationNotFoundException.class)
    public ProblemDetail handleDelegationNotFound(DelegationNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Delegation Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/delegation-not-found"));
        problem.setProperty("delegationId", ex.getDelegationId());
        return problem;
    }

    @ExceptionHandler(WatchNotFoundException.class)
    public ProblemDetail handleWatchNotFound(WatchNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Watch Not Found");
        problem.setType(URI.create("https://curanexus.se/errors/watch-not-found"));
        problem.setProperty("watchId", ex.getWatchId());
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
