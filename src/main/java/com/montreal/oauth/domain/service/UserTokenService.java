package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.entity.UserToken;
import com.montreal.oauth.domain.repository.IUserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTokenService {

    private final IUserTokenRepository userTokenRepository;

    @Value("${security.login-token.length:5}")
    private int tokenLength;

    @Value("${security.login-token.ttl-minutes:5}")
    private int tokenTtlMinutes;

    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRandomToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RANDOM.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString();
    }

    @Transactional
    public UserToken generateAndPersist(UserInfo user) {
        log.info("Generating login token for user {}", user.getUsername());
        userTokenRepository.invalidateAllByUserId(user.getId());

        String token = generateRandomToken(tokenLength);
        LocalDateTime now = LocalDateTime.now();
        UserToken ut = UserToken.builder()
                .user(user)
                .token(token)
                .createdAt(now)
                .expiresAt(now.plusMinutes(tokenTtlMinutes))
                .isValid(true)
                .build();
        return userTokenRepository.save(ut);
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> getActiveToken(Long userId) {
        return userTokenRepository.findActiveByUserId(userId, LocalDateTime.now());
    }

    @Transactional
    public void invalidateAll(Long userId) {
        userTokenRepository.invalidateAllByUserId(userId);
    }

    @Transactional
    public ValidationResult validateToken(Long userId, String token) {
        Optional<UserToken> latest = userTokenRepository.findLatestByUserIdAndToken(userId, token);
        if (latest.isEmpty()) {
            return ValidationResult.INVALID;
        }
        UserToken ut = latest.get();
        if (!Boolean.TRUE.equals(ut.getIsValid())) {
            return ValidationResult.INVALID;
        }
        if (LocalDateTime.now().isAfter(ut.getExpiresAt())) {
            return ValidationResult.EXPIRED;
        }
        // one-time use: invalidate upon successful validation
        ut.setIsValid(false);
        userTokenRepository.save(ut);
        return ValidationResult.OK;
    }

    public enum ValidationResult {
        OK, INVALID, EXPIRED
    }
}

