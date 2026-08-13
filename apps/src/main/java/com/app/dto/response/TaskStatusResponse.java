package com.app.dto.response;



import java.time.LocalDateTime;

public record TaskStatusResponse(
        String taskId,
        String status,
        String result,
        LocalDateTime submittedAt,
        LocalDateTime completedAt,
        String error,
        Integer position
) {
    // Конструктор для обратной совместимости
    public TaskStatusResponse(String taskId, String status, String result,
                              LocalDateTime submittedAt, LocalDateTime completedAt, String error) {
        this(taskId, status, result, submittedAt, completedAt, error, null);
    }
}
