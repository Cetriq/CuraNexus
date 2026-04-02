package se.curanexus.task.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.task.api.dto.*;
import se.curanexus.task.domain.*;
import se.curanexus.task.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // === Tasks ===

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(
                request.title(),
                request.category(),
                request.priority(),
                request.createdById(),
                request.patientId(),
                request.encounterId(),
                request.assigneeId(),
                request.dueAt(),
                request.sourceType(),
                request.sourceId()
        );
        if (request.description() != null) {
            task = taskService.updateTask(task.getId(), null, request.description(), null, null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID taskId) {
        Task task = taskService.getTask(taskId);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> listTasks(
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID encounterId,
            @RequestParam(required = false) UUID patientId) {
        List<Task> tasks;
        if (assigneeId != null) {
            tasks = taskService.getActiveTasksByAssignee(assigneeId);
        } else if (encounterId != null) {
            tasks = taskService.getTasksByEncounter(encounterId);
        } else if (patientId != null) {
            tasks = taskService.getTasksByPatient(patientId);
        } else {
            tasks = taskService.getUnassignedTasks();
        }
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).toList());
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID taskId,
                                                    @Valid @RequestBody UpdateTaskRequest request) {
        Task task = taskService.updateTask(taskId, request.title(), request.description(),
                request.priority(), request.dueAt());
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable UUID taskId,
                                                    @Valid @RequestBody AssignTaskRequest request) {
        Task task = taskService.assignTask(taskId, request.assigneeId());
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/start")
    public ResponseEntity<TaskResponse> startTask(@PathVariable UUID taskId) {
        Task task = taskService.startTask(taskId);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable UUID taskId,
                                                      @RequestBody(required = false) CompleteTaskRequest request) {
        String completionNote = request != null ? request.completionNote() : null;
        String outcome = request != null ? request.outcome() : null;
        Task task = taskService.completeTask(taskId, completionNote, outcome);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/cancel")
    public ResponseEntity<TaskResponse> cancelTask(@PathVariable UUID taskId,
                                                    @RequestBody(required = false) CancelTaskRequest request) {
        String reason = request != null ? request.reason() : null;
        Task task = taskService.cancelTask(taskId, reason);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @GetMapping("/users/{userId}/tasks")
    public ResponseEntity<List<TaskResponse>> getUserTasks(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "false") boolean includeCompleted) {
        List<Task> tasks = includeCompleted
                ? taskService.getTasksByAssignee(userId)
                : taskService.getActiveTasksByAssignee(userId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).toList());
    }

    @GetMapping("/encounters/{encounterId}/tasks")
    public ResponseEntity<List<TaskResponse>> getEncounterTasks(@PathVariable UUID encounterId) {
        List<Task> tasks = taskService.getTasksByEncounter(encounterId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).toList());
    }

    @GetMapping("/patients/{patientId}/tasks")
    public ResponseEntity<List<TaskResponse>> getPatientTasks(@PathVariable UUID patientId) {
        List<Task> tasks = taskService.getTasksByPatient(patientId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).toList());
    }

    // === Reminders ===

    @PostMapping("/reminders")
    public ResponseEntity<ReminderResponse> createReminder(@Valid @RequestBody CreateReminderRequest request) {
        Reminder reminder = taskService.createReminder(
                request.userId(),
                request.message(),
                request.remindAt(),
                request.patientId(),
                request.encounterId(),
                request.taskId(),
                request.recurring(),
                request.recurrencePattern()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ReminderResponse.from(reminder));
    }

    @GetMapping("/reminders/{reminderId}")
    public ResponseEntity<ReminderResponse> getReminder(@PathVariable UUID reminderId) {
        Reminder reminder = taskService.getReminder(reminderId);
        return ResponseEntity.ok(ReminderResponse.from(reminder));
    }

    @DeleteMapping("/reminders/{reminderId}")
    public ResponseEntity<Void> deleteReminder(@PathVariable UUID reminderId) {
        taskService.deleteReminder(reminderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reminders/{reminderId}/acknowledge")
    public ResponseEntity<ReminderResponse> acknowledgeReminder(@PathVariable UUID reminderId) {
        Reminder reminder = taskService.acknowledgeReminder(reminderId);
        return ResponseEntity.ok(ReminderResponse.from(reminder));
    }

    @PostMapping("/reminders/{reminderId}/snooze")
    public ResponseEntity<ReminderResponse> snoozeReminder(@PathVariable UUID reminderId,
                                                            @Valid @RequestBody SnoozeReminderRequest request) {
        Reminder reminder = taskService.snoozeReminder(reminderId, request.snoozeUntil());
        return ResponseEntity.ok(ReminderResponse.from(reminder));
    }

    @GetMapping("/users/{userId}/reminders")
    public ResponseEntity<List<ReminderResponse>> getUserReminders(
            @PathVariable UUID userId,
            @RequestParam(required = false) ReminderStatus status) {
        List<Reminder> reminders = status != null
                ? taskService.getRemindersByUser(userId).stream()
                    .filter(r -> r.getStatus() == status)
                    .toList()
                : taskService.getActiveRemindersByUser(userId);
        return ResponseEntity.ok(reminders.stream().map(ReminderResponse::from).toList());
    }

    // === Delegations ===

    @PostMapping("/delegations")
    public ResponseEntity<DelegationResponse> createDelegation(@Valid @RequestBody CreateDelegationRequest request) {
        Delegation delegation = taskService.createDelegation(
                request.fromUserId(),
                request.toUserId(),
                request.validFrom(),
                request.validUntil(),
                request.scope(),
                request.note()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(DelegationResponse.from(delegation));
    }

    @GetMapping("/delegations/{delegationId}")
    public ResponseEntity<DelegationResponse> getDelegation(@PathVariable UUID delegationId) {
        Delegation delegation = taskService.getDelegation(delegationId);
        return ResponseEntity.ok(DelegationResponse.from(delegation));
    }

    @PostMapping("/delegations/{delegationId}/revoke")
    public ResponseEntity<DelegationResponse> revokeDelegation(
            @PathVariable UUID delegationId,
            @RequestParam UUID revokedById) {
        Delegation delegation = taskService.revokeDelegation(delegationId, revokedById);
        return ResponseEntity.ok(DelegationResponse.from(delegation));
    }

    // === Watches ===

    @PostMapping("/watches")
    public ResponseEntity<WatchResponse> createWatch(@Valid @RequestBody CreateWatchRequest request) {
        Watch watch = taskService.createWatch(
                request.userId(),
                request.watchType(),
                request.targetId(),
                request.notifyOnChange(),
                request.note()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(WatchResponse.from(watch));
    }

    @GetMapping("/watches/{watchId}")
    public ResponseEntity<WatchResponse> getWatch(@PathVariable UUID watchId) {
        Watch watch = taskService.getWatch(watchId);
        return ResponseEntity.ok(WatchResponse.from(watch));
    }

    @DeleteMapping("/watches/{watchId}")
    public ResponseEntity<Void> deleteWatch(@PathVariable UUID watchId) {
        taskService.deleteWatch(watchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/watches")
    public ResponseEntity<List<WatchResponse>> getUserWatches(@PathVariable UUID userId) {
        List<Watch> watches = taskService.getWatchesByUser(userId);
        return ResponseEntity.ok(watches.stream().map(WatchResponse::from).toList());
    }
}
