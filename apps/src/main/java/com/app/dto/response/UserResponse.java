package com.app.dto.response;


import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Integer age,
        String fullName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // Фабричный метод для создания из Entity
    public static UserResponse from(
            Long id,
            String email,
            String firstName,
            String lastName,
            Integer age,
            String fullName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new UserResponse(
                id,
                email,
                firstName,
                lastName,
                age,
                fullName,
                createdAt,
                updatedAt
        );
    }
}
