package com.app.service.impl;


import com.app.dto.response.UserResponse;
import com.app.entity.UserEntity;
import com.app.mapper.UserMapper;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    //  Сохраняем в кэш при получении
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        log.info(" Loading user from DB (not cache): {}", id);
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(entity);
    }

    //  Обновляем кэш при обновлении пользователя
    @CachePut(value = "users", key = "#result.id")
    public UserResponse updateUser(UserEntity entity) {
        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    //  Удаляем из кэша при удалении
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    //  Удаляем все кэши
    @CacheEvict(value = "users", allEntries = true)
    public void clearCache() {
        log.info("🗑️ All user cache cleared");
    }
}
