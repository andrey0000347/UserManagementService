package com.app.service;

import com.app.dto.response.TaskStatusResponse;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface HeavyTaskService {

    @Async("heavyTaskExecutor")
    CompletableFuture<String> processLargeDataFile(String filePath);

    @Async("heavyTaskExecutor")
    CompletableFuture<String> generateReport(Long userId, String reportType);

    @Async("heavyTaskExecutor")
    CompletableFuture<String> sendBulkEmails(List<String> emails, String subject, String content);

    @Async("taskExecutor")
    CompletableFuture<String> processUserStatistics();

    TaskStatusResponse getTaskStatus(String taskId);

    Map<String, Object> getAllTasks();
    void generateReportSync(Long userId, String reportType, String taskId);
    void processFileSync(String filePath, String taskId);

    void addTask(String taskId, String status, String result, LocalDateTime submittedAt, LocalDateTime completedAt, String error);
    void updateTask(String taskId, String status, String result, LocalDateTime completedAt);
}
