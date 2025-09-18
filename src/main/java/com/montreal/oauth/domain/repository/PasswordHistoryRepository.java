package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.PasswordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user.id = :userId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findLastPasswordsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM password_history 
        WHERE user_id = :userId 
        AND id NOT IN (
            SELECT id FROM (
                SELECT id FROM password_history 
                WHERE user_id = :userId 
                ORDER BY created_at DESC 
                LIMIT :maxHistory
            ) AS recent_passwords
        )
        """, nativeQuery = true)
    void cleanupOldPasswordHistory(@Param("userId") Long userId, @Param("maxHistory") int maxHistory);
}