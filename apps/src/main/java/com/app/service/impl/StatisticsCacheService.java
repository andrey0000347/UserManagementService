package com.app.service.impl;



import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsCacheService {

    private final UserRepository userRepository;

    @Cacheable(value = "statistics", key = "'userStats'", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getUserStatistics() {
        log.info(" Calculating statistics (not cached)");

        List<Object[]> rawResults = userRepository.getUserStatisticsWithWindowFunctions();
        return convertToMap(rawResults, new String[]{
                "id", "email", "firstName", "lastName", "age", "createdAt",
                "avgAge", "agePercentile", "ageQuartile", "userNumber"
        });
    }

    @Cacheable(value = "statistics", key = "'ageGroups'", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getUsersGroupedByAge() {
        log.info(" Calculating age groups (not cached)");

        List<Object[]> rawResults = userRepository.getUsersGroupedByAge();
        return convertToMap(rawResults, new String[]{
                "ageGroup", "userCount", "avgAge", "firstRegistration", "lastRegistration"
        });
    }

    @CacheEvict(value = "statistics", allEntries = true)
    public void clearStatisticsCache() {
        log.info("🗑 All statistics cache cleared");
    }

    //  Вспомогательный метод для конвертации
    private List<Map<String, Object>> convertToMap(List<Object[]> results, String[] columnNames) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < columnNames.length && i < row.length; i++) {
                        Object value = row[i];
                        // Конвертируем LocalDateTime если нужно
                        if (value instanceof LocalDateTime) {
                            map.put(columnNames[i], value);
                        } else {
                            map.put(columnNames[i], value);
                        }
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }
}