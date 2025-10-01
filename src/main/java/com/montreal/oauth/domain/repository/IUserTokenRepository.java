package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IUserTokenRepository extends JpaRepository<UserToken, Long> {

    @Query(value = "select * from user_token ut where ut.user_id = :userId and ut.is_valid = true and ut.expires_at > :now order by ut.created_at desc limit 1", nativeQuery = true)
    Optional<UserToken> findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("update UserToken ut set ut.isValid = false where ut.user.id = :userId and ut.isValid = true")
    int invalidateAllByUserId(@Param("userId") Long userId);

    @Query(value = "select * from user_token ut where ut.user_id = :userId and ut.token = :token order by ut.created_at desc limit 1", nativeQuery = true)
    Optional<UserToken> findLatestByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);
}
