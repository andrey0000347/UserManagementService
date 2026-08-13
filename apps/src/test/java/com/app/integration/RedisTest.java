package com.app.integration;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class RedisTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void shouldSaveAndRetrieveFromRedis() {
        String key = "test:key";
        String value = "Hello, Redis!";

        redisTemplate.opsForValue().set(key, value);
        String retrieved = (String) redisTemplate.opsForValue().get(key);

        assertThat(retrieved).isEqualTo(value);
    }
}