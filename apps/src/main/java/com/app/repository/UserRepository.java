package com.app.repository;


import com.app.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmailAndPassword(String email, String password);

    void deleteByEmail(String email);

    // ============================================
    // 📊 ЗАПРОС №1: Статистика по возрасту с оконными функциями
    // ============================================
    @Query(value = """
        SELECT 
            u.id,
            u.email,
            u.first_name,
            u.last_name,
            u.age,
            u.created_at,
            AVG(u.age) OVER () as avg_age,
            PERCENT_RANK() OVER (ORDER BY u.age) as age_percentile,
            NTILE(4) OVER (ORDER BY u.age) as age_quartile,
            ROW_NUMBER() OVER (ORDER BY u.created_at) as user_number
        FROM users u
        WHERE u.age IS NOT NULL
        ORDER BY u.age DESC
        """, nativeQuery = true)
    List<Object[]> getUserStatisticsWithWindowFunctions();

    // ============================================
    // 📊 ЗАПРОС №5: Статистика по возрастным группам
    // ============================================
    @Query("""
        SELECT 
            CASE 
                WHEN u.age BETWEEN 0 AND 17 THEN 'Minor'
                WHEN u.age BETWEEN 18 AND 25 THEN 'Young Adult'
                WHEN u.age BETWEEN 26 AND 40 THEN 'Adult'
                WHEN u.age BETWEEN 41 AND 60 THEN 'Middle Age'
                ELSE 'Senior'
            END as age_group,
            COUNT(u) as user_count,
            AVG(u.age) as avg_age,
            MIN(u.createdAt) as first_registration,
            MAX(u.createdAt) as last_registration
        FROM UserEntity u
        WHERE u.age IS NOT NULL
        GROUP BY 
            CASE 
                WHEN u.age BETWEEN 0 AND 17 THEN 'Minor'
                WHEN u.age BETWEEN 18 AND 25 THEN 'Young Adult'
                WHEN u.age BETWEEN 26 AND 40 THEN 'Adult'
                WHEN u.age BETWEEN 41 AND 60 THEN 'Middle Age'
                ELSE 'Senior'
            END
        ORDER BY user_count DESC
        """)
    List<Object[]> getUsersGroupedByAge();
}
