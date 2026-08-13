package com.app.integration;



import com.app.dto.request.UserCreateRequest;
import com.app.dto.response.UserResponse;
import com.app.entity.UserEntity;
import com.app.repository.UserRepository;
import com.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("Интеграционные тесты для UserService с Testcontainers")
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Должен создать пользователя с валидными данными")
    void shouldCreateUser() {
        UserCreateRequest request = new UserCreateRequest(
                "john@example.com",
                "password123",
                "John",
                "Doe",
                25
        );

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.age()).isEqualTo(25);
        assertThat(response.fullName()).isEqualTo("John Doe");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        UserEntity savedEntity = userRepository.findById(response.id()).orElseThrow();
        assertThat(savedEntity.getEmail()).isEqualTo("john@example.com");
        assertThat(savedEntity.getFirstName()).isEqualTo("John");
        assertThat(savedEntity.getLastName()).isEqualTo("Doe");
        assertThat(savedEntity.getAge()).isEqualTo(25);
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен получить пользователя по ID")
    void shouldGetUserById() {
        UserCreateRequest request = new UserCreateRequest(
                "jane@example.com",
                "password456",
                "Jane",
                "Smith",
                30
        );
        UserResponse created = userService.createUser(request);

        UserResponse found = userService.getUserById(created.id());

        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.email()).isEqualTo("jane@example.com");
        assertThat(found.firstName()).isEqualTo("Jane");
        assertThat(found.lastName()).isEqualTo("Smith");
        assertThat(found.age()).isEqualTo(30);
        assertThat(found.fullName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Должен получить всех пользователей")
    void shouldGetAllUsers() {
        createUser("user1@example.com", "User1", "Test1", 25);
        createUser("user2@example.com", "User2", "Test2", 30);
        createUser("user3@example.com", "User3", "Test3", 35);

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(3);
        assertThat(users)
                .extracting(UserResponse::email)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    @DisplayName("Должен обновить пользователя")
    void shouldUpdateUser() {
        // Given - создаем пользователя
        UserCreateRequest createRequest = new UserCreateRequest(
                "bob@example.com",
                "password789",
                "Bob",
                "Johnson",
                45
        );
        UserResponse created = userService.createUser(createRequest);

        // When - обновляем
        UserCreateRequest updateRequest = new UserCreateRequest(
                "bob.updated@example.com",
                "newpassword789",
                "Robert",
                "Johnson",
                46
        );
        UserResponse updated = userService.updateUser(created.id(), updateRequest);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.email()).isEqualTo("bob.updated@example.com");
        assertThat(updated.firstName()).isEqualTo("Robert");
        assertThat(updated.lastName()).isEqualTo("Johnson");
        assertThat(updated.age()).isEqualTo(46);
        assertThat(updated.fullName()).isEqualTo("Robert Johnson");

        //  Проверяем, что updatedAt изменился (с округлением до миллисекунд)
        LocalDateTime createdTime = created.updatedAt().truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime updatedTime = updated.updatedAt().truncatedTo(ChronoUnit.MILLIS);

        // updatedAt должен быть >= createdAt
        assertThat(updatedTime).isAfterOrEqualTo(createdTime);

        //  Дополнительная проверка: изменились ли поля
        assertThat(updated.email()).isNotEqualTo(createRequest.email());
        assertThat(updated.firstName()).isNotEqualTo(createRequest.firstName());
        assertThat(updated.age()).isNotEqualTo(createRequest.age());
    }

    @Test
    @DisplayName("Должен удалить пользователя")
    void shouldDeleteUser() {
        UserCreateRequest request = new UserCreateRequest(
                "alice@example.com",
                "password000",
                "Alice",
                "Williams",
                22
        );
        UserResponse created = userService.createUser(request);
        assertThat(userRepository.count()).isEqualTo(1);

        userService.deleteUser(created.id());

        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(userRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("Должен выбросить исключение при создании пользователя с существующим email")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserCreateRequest request = new UserCreateRequest(
                "duplicate@example.com",
                "password123",
                "John",
                "Doe",
                25
        );
        userService.createUser(request);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Должен вернуть статистику с оконными функциями")
    void shouldGetUserStatistics() {
        createUser("young@example.com", "Young", "User", 22);
        createUser("adult@example.com", "Adult", "User", 30);
        createUser("middle@example.com", "Middle", "User", 45);
        createUser("senior@example.com", "Senior", "User", 60);

        List<java.util.Map<String, Object>> statistics = userService.getUserStatistics();

        assertThat(statistics).isNotEmpty();
        assertThat(statistics).hasSize(4);

        java.util.Map<String, Object> firstStats = statistics.get(0);
        assertThat(firstStats).containsKeys("id", "email", "firstName", "lastName", "age", "createdAt");
        assertThat(firstStats).containsKeys("avgAge", "agePercentile", "ageQuartile", "userNumber");

        Number avgAge = (Number) firstStats.get("avgAge");
        assertThat(avgAge.doubleValue()).isEqualTo(39.25);

        List<Integer> ages = statistics.stream()
                .map(m -> (Integer) m.get("age"))
                .toList();
        assertThat(ages).containsExactlyInAnyOrder(60, 45, 30, 22);
    }

    @Test
    @DisplayName("Должен сгруппировать пользователей по возрастным группам")
    void shouldGetUsersGroupedByAge() {
        createUser("young@example.com", "Young", "User", 22);
        createUser("young2@example.com", "Young2", "User", 25);
        createUser("adult@example.com", "Adult", "User", 30);
        createUser("middle@example.com", "Middle", "User", 45);
        createUser("senior@example.com", "Senior", "User", 60);

        List<java.util.Map<String, Object>> ageGroups = userService.getUsersGroupedByAge();

        assertThat(ageGroups).isNotEmpty();

        java.util.Map<String, Object> youngAdultGroup = ageGroups.stream()
                .filter(group -> "Young Adult".equals(group.get("ageGroup")))
                .findFirst()
                .orElseThrow();

        assertThat(youngAdultGroup.get("userCount")).isEqualTo(2L);
        Number avgAge = (Number) youngAdultGroup.get("avgAge");
        assertThat(avgAge.doubleValue()).isBetween(23.0, 24.0);
    }

    private UserResponse createUser(String email, String firstName, String lastName, int age) {
        UserCreateRequest request = new UserCreateRequest(
                email,
                "password",
                firstName,
                lastName,
                age
        );
        return userService.createUser(request);
    }
}
