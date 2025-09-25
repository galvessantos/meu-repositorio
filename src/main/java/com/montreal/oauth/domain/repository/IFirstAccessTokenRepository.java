package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.FirstAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IFirstAccessTokenRepository extends JpaRepository<FirstAccessToken, Long> {

    @Query("select t from FirstAccessToken t where t.token = :token")
    Optional<FirstAccessToken> findByToken(@Param("token") String token);

    @Query("select t from FirstAccessToken t where t.user.id = :userId and t.isUsed = false and t.expiresAt > :now order by t.createdAt desc")
    Optional<FirstAccessToken> findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("update FirstAccessToken t set t.isUsed = true, t.usedAt = :now where t.user.id = :userId and t.isUsed = false")
    int invalidateAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

