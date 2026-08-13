package com.app.service.impl;

import com.app.dto.response.TaskStatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TASK_QUEUE = "async:tasks";
    private static final String TASK_RESULT_PREFIX = "async:result:";

    //  Отправка задачи в очередь
    public String submitTask(String taskType, Object payload) {
        String taskId = UUID.randomUUID().toString();

        TaskMessage message = TaskMessage.builder()
                .taskId(taskId)
                .taskType(taskType)
                .payload(payload)
                .submittedAt(LocalDateTime.now())
                .build();

        // Сохраняем в Redis с TTL 1 час
        redisTemplate.opsForList().rightPush(TASK_QUEUE, message);
        redisTemplate.expire(TASK_QUEUE, Duration.ofHours(1));

        log.info(" Task submitted: {} - {}", taskId, taskType);
        return taskId;
    }

    //  Получение статуса задачи
    public TaskStatusResponse getTaskStatus(String taskId) {
        String key = TASK_RESULT_PREFIX + taskId;
        TaskResult result = (TaskResult) redisTemplate.opsForValue().get(key);

        if (result == null) {
            // Проверяем, может задача еще в очереди
            Long size = redisTemplate.opsForList().size(TASK_QUEUE);
            if (size != null && size > 0) {
                for (int i = 0; i < size; i++) {
                    Object item = redisTemplate.opsForList().index(TASK_QUEUE, i);
                    if (item instanceof TaskMessage) {
                        TaskMessage msg = (TaskMessage) item;
                        if (msg.getTaskId().equals(taskId)) {
                            // ✅ Используем Record с position
                            return new TaskStatusResponse(
                                    taskId,
                                    "QUEUED",
                                    null,
                                    msg.getSubmittedAt(),
                                    null,
                                    null,
                                    i + 1  // position в очереди
                            );
                        }
                    }
                }
            }

            //  Задача не найдена
            return new TaskStatusResponse(
                    taskId,
                    "NOT_FOUND",
                    null,
                    null,
                    null,
                    "Task not found",
                    null
            );
        }

        //  Задача завершена
        return new TaskStatusResponse(
                taskId,
                result.getStatus(),
                result.getResult(),
                result.getSubmittedAt(),
                result.getCompletedAt(),
                result.getError(),
                null
        );
    }

    //  Обработчик задач (воркер)
    @Async
    public void processTasks() {
        while (true) {
            try {
                // Берем задачу из очереди (блокирующий pop)
                Object item = redisTemplate.opsForList()
                        .leftPop(TASK_QUEUE, 5, TimeUnit.SECONDS);

                if (item == null) continue;

                if (!(item instanceof TaskMessage)) {
                    log.warn("⚠️ Unknown item in queue: {}", item.getClass());
                    continue;
                }

                TaskMessage message = (TaskMessage) item;
                log.info("🔄 Processing task: {} - {}", message.getTaskId(), message.getTaskType());

                // Обработка задачи
                String result = processTask(message);

                // Сохраняем результат в Redis
                TaskResult taskResult = TaskResult.builder()
                        .taskId(message.getTaskId())
                        .status("COMPLETED")
                        .result(result)
                        .submittedAt(message.getSubmittedAt())
                        .completedAt(LocalDateTime.now())
                        .build();

                String resultKey = TASK_RESULT_PREFIX + message.getTaskId();
                redisTemplate.opsForValue().set(resultKey, taskResult);
                redisTemplate.expire(resultKey, Duration.ofHours(1));

                log.info(" Task completed: {}", message.getTaskId());

            } catch (Exception e) {
                log.error(" Worker error: {}", e.getMessage());
            }
        }
    }

    private String processTask(TaskMessage message) {
        // Здесь ваша бизнес-логика
        switch (message.getTaskType()) {
            case "PROCESS_FILE":
                return "File processed: " + message.getPayload();
            case "GENERATE_REPORT":
                return "Report generated for user: " + message.getPayload();
            case "SEND_EMAILS":
                return "Emails sent: " + message.getPayload();
            default:
                throw new RuntimeException("Unknown task type: " + message.getTaskType());
        }
    }

    //  Внутренние классы для Redis
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class TaskMessage {
        private String taskId;
        private String taskType;
        private Object payload;
        private LocalDateTime submittedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class TaskResult {
        private String taskId;
        private String status;
        private String result;
        private String error;
        private LocalDateTime submittedAt;
        private LocalDateTime completedAt;
    }
}
