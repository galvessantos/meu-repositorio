package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IUserTokenRepository extends JpaRepository<UserToken, Long> {

    @Query("select ut from UserToken ut where ut.user.id = :userId and ut.isValid = true and ut.expiresAt > :now order by ut.createdAt desc")
    Optional<UserToken> findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update UserToken ut set ut.isValid = false where ut.user.id = :userId and ut.isValid = true")
    int invalidateAllByUserId(@Param("userId") Long userId);

    @Query("select ut from UserToken ut where ut.user.id = :userId and ut.token = :token order by ut.createdAt desc")
    Optional<UserToken> findLatestByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);

    @Query("select count(ut) from UserToken ut where ut.user.id = :userId and ut.isValid = true and ut.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("select ut from UserToken ut where ut.user.id = :userId and ut.isValid = true and ut.expiresAt > :now")
    List<UserToken> findAllActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}