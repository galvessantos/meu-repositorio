package com.montreal.oauth.domain.repository;


import com.montreal.oauth.domain.entity.PasswordResetToken;
import com.montreal.oauth.domain.repository.infrastructure.CustomJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface IPasswordResetTokenRepository extends CustomJpaRepository<PasswordResetToken, Long> {


    Optional<PasswordResetToken> findByToken(String token);


    List<PasswordResetToken> findByUser_Id(Long userId);


    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.id = :userId AND prt.isUsed = false AND prt.expiresAt > :now")
    List<PasswordResetToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);


    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.expiresAt < :now AND prt.isUsed = false")
    List<PasswordResetToken> findExpiredUnusedTokens(@Param("now") LocalDateTime now);


    @Query("SELECT COUNT(prt) > 0 FROM PasswordResetToken prt WHERE prt.user.id = :userId AND prt.isUsed = false AND prt.expiresAt > :now")
    boolean existsValidTokenByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);


    void deleteByUser_Id(Long userId);


    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}