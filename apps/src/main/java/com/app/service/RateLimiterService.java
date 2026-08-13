package com.app.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    //  Проверка лимита запросов
    public boolean allowRequest(String clientId, String endpoint, int limit, Duration window) {
        String key = "rate:limit:" + endpoint + ":" + clientId;

        Integer count = (Integer) redisTemplate.opsForValue().get(key);

        if (count == null) {
            // Первый запрос
            redisTemplate.opsForValue().set(key, 1, window);
            return true;
        }

        if (count < limit) {
            // Увеличиваем счетчик
            redisTemplate.opsForValue().increment(key);
            return true;
        }

        // Превышен лимит
        log.warn(" Rate limit exceeded for client: {}, endpoint: {}", clientId, endpoint);
        return false;
    }

    //  Сброс лимита для клиента
    public void resetLimit(String clientId, String endpoint) {
        String key = "rate:limit:" + endpoint + ":" + clientId;
        redisTemplate.delete(key);
        log.info("🔄 Rate limit reset for client: {}, endpoint: {}", clientId, endpoint);
    }

    //  Получение текущего количества запросов
    public Integer getRequestCount(String clientId, String endpoint) {
        String key = "rate:limit:" + endpoint + ":" + clientId;
        return (Integer) redisTemplate.opsForValue().get(key);
    }
}
