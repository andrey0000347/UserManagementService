package com.app.service.impl;

import com.app.dto.request.UserCreateRequest;
import com.app.dto.response.UserResponse;
import com.app.entity.UserEntity;
import com.app.exception.UserNotFoundException;
import com.app.mapper.UserMapper;
import com.app.repository.UserRepository;
import com.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.debug("Creating user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("User with email {} already exists", request.email());
            throw new RuntimeException("User with email " + request.email() + " already exists");
        }

        UserEntity entity = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(entity);
        log.info("User created with id: {}", saved.getId());

        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);

        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });

        return userMapper.toResponse(entity);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<UserEntity> entities = userRepository.findAll();
        return entities.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserCreateRequest request) {
        log.debug("Updating user with id: {}", id);

        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });

        userMapper.updateEntity(entity, request);
        UserEntity updated = userRepository.save(entity);
        log.info("User updated with id: {}", updated.getId());

        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            log.error("User not found with id: {}", id);
            throw new UserNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }


    @Override
    public List<Map<String, Object>> getUserStatistics() {
        log.debug("Fetching user statistics with window functions");
        List<Object[]> results = userRepository.getUserStatisticsWithWindowFunctions();
        return convertToMap(results, new String[]{
                "id", "email", "firstName", "lastName", "age", "createdAt",
                "avgAge", "agePercentile", "ageQuartile", "userNumber"
        });
    }

    @Override
    public List<Map<String, Object>> getUsersGroupedByAge() {
        log.debug("Fetching users grouped by age");
        List<Object[]> results = userRepository.getUsersGroupedByAge();
        return convertToMap(results, new String[]{
                "ageGroup", "userCount", "avgAge",
                "firstRegistration", "lastRegistration"
        });
    }


    private List<Map<String, Object>> convertToMap(List<Object[]> results, String[] columnNames) {
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < columnNames.length && i < row.length; i++) {
                map.put(columnNames[i], row[i]);
            }
            mappedResults.add(map);
        }
        return mappedResults;
    }
}