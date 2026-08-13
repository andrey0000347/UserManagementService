package com.app.service;

import com.app.dto.request.UserCreateRequest;
import com.app.dto.response.UserResponse;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserCreateRequest request);

    void deleteUser(Long id);

    List<Map<String, Object>> getUserStatistics();

    List<Map<String, Object>> getUsersGroupedByAge();
}