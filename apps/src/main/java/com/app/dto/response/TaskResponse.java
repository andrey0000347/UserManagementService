package com.app.dto.response;


import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        String taskId,
        String status,
        String message,
        LocalDateTime submittedAt,
        String endpoint
) {
    public static TaskResponse accepted(String message, String endpoint) {
        return new TaskResponse(
                UUID.randomUUID().toString(),
                "ACCEPTED",
                message,
                LocalDateTime.now(),
                endpoint
        );
    }
}
