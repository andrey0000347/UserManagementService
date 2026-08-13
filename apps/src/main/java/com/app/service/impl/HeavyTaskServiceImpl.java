package com.app.service.impl;

import com.app.dto.response.TaskStatusResponse;
import com.app.service.HeavyTaskService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeavyTaskServiceImpl implements HeavyTaskService {

    private static final Map<String, TaskStatus> taskStatusMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("=== INITIALIZING TASK MAP ===");
        log.info("Map size: {}", taskStatusMap.size());
    }

    @Override
    @Async("heavyTaskExecutor")
    public CompletableFuture<String> processLargeDataFile(String filePath) {
        String taskId = UUID.randomUUID().toString();
        log.info("=== STARTING: processLargeDataFile ===");
        log.info("Task ID: {}", taskId);

        taskStatusMap.put(taskId, new TaskStatus("PROCESSING", null, LocalDateTime.now(), null, null));

        try {
            Thread.sleep(10000);
            String result = "File processed successfully: " + filePath + ", records: 15000";

            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("COMPLETED", result,
                    status.getSubmittedAt(), LocalDateTime.now(), null));

            log.info("Task completed: {}", taskId);
            return CompletableFuture.completedFuture(result);

        } catch (InterruptedException e) {
            log.error("Task failed: {}", taskId, e);
            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("FAILED", null,
                    status.getSubmittedAt(), LocalDateTime.now(), e.getMessage()));
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async("heavyTaskExecutor")
    public CompletableFuture<String> generateReport(Long userId, String reportType) {
        String taskId = UUID.randomUUID().toString();
        log.info("=== STARTING: generateReport ===");
        log.info("Task ID: {}", taskId);

        taskStatusMap.put(taskId, new TaskStatus("PROCESSING", null, LocalDateTime.now(), null, null));

        try {
            Thread.sleep(8000);
            String result = String.format("Report generated for user %d, type: %s, size: 2.5MB",
                    userId, reportType);

            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("COMPLETED", result,
                    status.getSubmittedAt(), LocalDateTime.now(), null));

            log.info("Task completed: {}", taskId);
            return CompletableFuture.completedFuture(result);

        } catch (InterruptedException e) {
            log.error("Task failed: {}", taskId, e);
            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("FAILED", null,
                    status.getSubmittedAt(), LocalDateTime.now(), e.getMessage()));
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async("heavyTaskExecutor")
    public CompletableFuture<String> sendBulkEmails(List<String> emails, String subject, String content) {
        String taskId = UUID.randomUUID().toString();
        log.info("=== STARTING: sendBulkEmails ===");
        log.info("Task ID: {}", taskId);

        taskStatusMap.put(taskId, new TaskStatus("PROCESSING", null, LocalDateTime.now(), null, null));

        try {
            Thread.sleep(5000);
            String result = String.format("Bulk emails sent: %d emails, subject: %s", emails.size(), subject);

            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("COMPLETED", result,
                    status.getSubmittedAt(), LocalDateTime.now(), null));

            log.info("Task completed: {}", taskId);
            return CompletableFuture.completedFuture(result);

        } catch (InterruptedException e) {
            log.error("Task failed: {}", taskId, e);
            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("FAILED", null,
                    status.getSubmittedAt(), LocalDateTime.now(), e.getMessage()));
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<String> processUserStatistics() {
        String taskId = UUID.randomUUID().toString();
        log.info("=== STARTING: processUserStatistics ===");
        log.info("Task ID: {}", taskId);

        taskStatusMap.put(taskId, new TaskStatus("PROCESSING", null, LocalDateTime.now(), null, null));

        try {
            Thread.sleep(3000);
            String result = "User statistics updated: totalUsers=150, avgAge=35.5";

            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("COMPLETED", result,
                    status.getSubmittedAt(), LocalDateTime.now(), null));

            log.info("Task completed: {}", taskId);
            return CompletableFuture.completedFuture(result);

        } catch (InterruptedException e) {
            log.error("Task failed: {}", taskId, e);
            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("FAILED", null,
                    status.getSubmittedAt(), LocalDateTime.now(), e.getMessage()));
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public TaskStatusResponse getTaskStatus(String taskId) {
        log.info("Getting status for task: {}", taskId);
        log.info("All tasks in map: {}", taskStatusMap.keySet());

        TaskStatus status = taskStatusMap.get(taskId);

        if (status == null) {
            log.warn("Task not found: {}", taskId);
            return new TaskStatusResponse(taskId, "NOT_FOUND", null, null, null, "Task not found");
        }

        return new TaskStatusResponse(
                taskId,
                status.getStatus(),
                status.getResult(),
                status.getSubmittedAt(),
                status.getCompletedAt(),
                status.getError()
        );
    }

    @Override
    public void addTask(String taskId, String status, String result, LocalDateTime submittedAt, LocalDateTime completedAt, String error) {
        taskStatusMap.put(taskId, new TaskStatus(status, result, submittedAt, completedAt, error));
    }

    @Override
    public void updateTask(String taskId, String status, String result, LocalDateTime completedAt) {
        TaskStatus existing = taskStatusMap.get(taskId);
        if (existing != null) {
            taskStatusMap.put(taskId, new TaskStatus(status, result, existing.getSubmittedAt(), completedAt, null));
        }
    }

    @Override
    public Map<String, Object> getAllTasks() {
        return new HashMap<>(taskStatusMap);
    }

    // ✅ СИНХРОННЫЕ МЕТОДЫ
    @Override
    public void processFileSync(String filePath, String taskId) {
        log.info("=== SYNC: processFile ===");
        log.info("Task ID: {}", taskId);
        log.info("File path: {}", filePath);

        taskStatusMap.put(taskId, new TaskStatus("PROCESSING", null, LocalDateTime.now(), null, null));
        log.info("Task added to map. Map size: {}", taskStatusMap.size());
        log.info("All tasks: {}", taskStatusMap.keySet());

        try {
            Thread.sleep(10000);
            String result = "File processed successfully: " + filePath + ", records: 15000";

            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("COMPLETED", result,
                    status.getSubmittedAt(), LocalDateTime.now(), null));

            log.info("Task completed: {}", taskId);
        } catch (InterruptedException e) {
            log.error("Task failed: {}", taskId, e);
            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("FAILED", null,
                    status.getSubmittedAt(), LocalDateTime.now(), e.getMessage()));
        }
    }

    @Override
    public void generateReportSync(Long userId, String reportType, String taskId) {
        log.info("=== SYNC: generateReport ===");
        log.info("Task ID: {}", taskId);
        log.info("User ID: {}, Report Type: {}", userId, reportType);

        taskStatusMap.put(taskId, new TaskStatus("PROCESSING", null, LocalDateTime.now(), null, null));
        log.info("Task added to map. Map size: {}", taskStatusMap.size());

        try {
            Thread.sleep(8000);
            String result = String.format("Report generated for user %d, type: %s, size: 2.5MB",
                    userId, reportType);

            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("COMPLETED", result,
                    status.getSubmittedAt(), LocalDateTime.now(), null));

            log.info("Task completed: {}", taskId);
        } catch (InterruptedException e) {
            log.error("Task failed: {}", taskId, e);
            TaskStatus status = taskStatusMap.get(taskId);
            taskStatusMap.put(taskId, new TaskStatus("FAILED", null,
                    status.getSubmittedAt(), LocalDateTime.now(), e.getMessage()));
        }
    }

    @lombok.AllArgsConstructor
    @lombok.Data
    private static class TaskStatus {
        private String status;
        private String result;
        private LocalDateTime submittedAt;
        private LocalDateTime completedAt;
        private String error;
    }
}
