package com.app.controller;

import com.app.dto.response.TaskResponse;
import com.app.dto.response.TaskStatusResponse;
import com.app.service.HeavyTaskService;
import com.app.service.impl.HeavyTaskServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncController {

    private final HeavyTaskService heavyTaskService;


    // 1. Обработка большого файла

    @PostMapping("/process-file")
    public ResponseEntity<TaskResponse> processFile(@RequestParam String filePath) {
        log.info("REST request to process file: {}", filePath);

        // Запускаем асинхронно
        heavyTaskService.processLargeDataFile(filePath);

        // Возвращаем мгновенный ответ с 202 Accepted
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TaskResponse.accepted(
                        "File processing started: " + filePath,
                        "/api/async/process-file"
                ));
    }


    // 2. Генерация отчета

    @PostMapping("/generate-report")
    public ResponseEntity<TaskResponse> generateReport(
            @RequestParam Long userId,
            @RequestParam String reportType) {
        log.info("REST request to generate report: userId={}, reportType={}", userId, reportType);

        heavyTaskService.generateReport(userId, reportType);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TaskResponse.accepted(
                        "Report generation started for user: " + userId,
                        "/api/async/generate-report"
                ));
    }


    // 3. Массовая рассылка email

    @PostMapping("/send-bulk-emails")
    public ResponseEntity<TaskResponse> sendBulkEmails(
            @RequestParam List<String> emails,
            @RequestParam String subject,
            @RequestParam String content) {
        log.info("REST request to send bulk emails: count={}", emails.size());

        heavyTaskService.sendBulkEmails(emails, subject, content);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TaskResponse.accepted(
                        "Bulk email sending started: " + emails.size() + " emails",
                        "/api/async/send-bulk-emails"
                ));
    }


    // 4. Получение статуса задачи

    @GetMapping("/task-status/{taskId}")
    public ResponseEntity<TaskStatusResponse> getTaskStatus(@PathVariable String taskId) {
        log.info("REST request to get task status: {}", taskId);
        TaskStatusResponse status = heavyTaskService.getTaskStatus(taskId);
        return ResponseEntity.ok(status);
    }


    // 5. Запуск статистики

    @PostMapping("/process-statistics")
    public ResponseEntity<TaskResponse> processStatistics() {
        log.info("REST request to process statistics");

        heavyTaskService.processUserStatistics();

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TaskResponse.accepted(
                        "Statistics processing started",
                        "/api/async/process-statistics"
                ));
    }


    @GetMapping("/tasks/all")
    public Map<String, Object> getAllTasks() {
        log.info("REST request to get all tasks");
        Map<String, Object> tasks = heavyTaskService.getAllTasks();
        log.info("Tasks in map: {}", tasks.keySet());
        return tasks;
    }

    @PostMapping("/generate-report-sync")
    public ResponseEntity<TaskResponse> generateReportSync(
            @RequestParam Long userId,
            @RequestParam String reportType) {
        log.info("REST request to generate report sync: userId={}, reportType={}", userId, reportType);

        String taskId = UUID.randomUUID().toString();

        // Запускаем в отдельном потоке
        new Thread(() -> {
            heavyTaskService.generateReportSync(userId, reportType, taskId);
        }).start();

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TaskResponse.accepted(
                        "Report generation started for user: " + userId,
                        "/api/async/generate-report-sync"
                ));
    }

    @PostMapping("/process-file-sync")
    public ResponseEntity<TaskResponse> processFileSync(@RequestParam String filePath) {
        log.info("REST request to process file sync: {}", filePath);

        String taskId = UUID.randomUUID().toString();

        new Thread(() -> {
            heavyTaskService.processFileSync(filePath, taskId);
        }).start();

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TaskResponse.accepted(
                        "File processing started: " + filePath,
                        "/api/async/process-file-sync"
                ));
    }


    @PostMapping("/process-file-direct")
    public ResponseEntity<TaskResponse> processFileDirect(@RequestParam String filePath) {
        log.info("REST request to process file direct: {}", filePath);

        String taskId = UUID.randomUUID().toString();

        //  Добавляем через сервис
        heavyTaskService.addTask(taskId, "PROCESSING", null, LocalDateTime.now(), null, null);

        new Thread(() -> {
            try {
                Thread.sleep(10000);
                String result = "File processed successfully: " + filePath + ", records: 15000";
                heavyTaskService.updateTask(taskId, "COMPLETED", result, LocalDateTime.now());
                log.info("Task completed: {}", taskId);
            } catch (Exception e) {
                log.error("Task failed: {}", taskId, e);
                heavyTaskService.updateTask(taskId, "FAILED", null, LocalDateTime.now());
            }
        }).start();

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TaskResponse.accepted(
                        "File processing started: " + filePath,
                        "/api/async/process-file-direct"
                ));
    }
}
