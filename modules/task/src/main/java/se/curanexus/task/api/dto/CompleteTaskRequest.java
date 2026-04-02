package se.curanexus.task.api.dto;

public record CompleteTaskRequest(
        String completionNote,
        String outcome
) {}
