package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.entity.FirstAccessToken;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.IFirstAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirstAccessService {

    private final IFirstAccessTokenRepository firstAccessTokenRepository;

    @Value("${app.first-access.token.expiration-minutes:60}")
    private int tokenExpirationMinutes;

    @Transactional
    public String generateFirstAccessToken(UserInfo user) {
        firstAccessTokenRepository.invalidateAllByUserId(user.getId(), LocalDateTime.now());
        String token = UUID.randomUUID().toString();
        FirstAccessToken t = FirstAccessToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(tokenExpirationMinutes))
                .isUsed(false)
                .build();
        firstAccessTokenRepository.save(t);
        return token;
    }

    @Transactional(readOnly = true)
    public Optional<FirstAccessToken> findByToken(String token) {
        return firstAccessTokenRepository.findByToken(token);
    }

    @Transactional
    public boolean consumeFirstAccessToken(String token) {
        Optional<FirstAccessToken> opt = firstAccessTokenRepository.findByToken(token);
        if (opt.isEmpty()) return false;
        FirstAccessToken t = opt.get();
        if (t.isExpired() || t.isUsed()) return false;
        t.setUsed(true);
        t.setUsedAt(LocalDateTime.now());
        firstAccessTokenRepository.save(t);
        return true;
    }
}

