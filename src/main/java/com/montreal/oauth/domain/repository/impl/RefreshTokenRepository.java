package com.montreal.oauth.domain.repository.impl;

import com.montreal.oauth.domain.entity.RefreshToken;
import com.montreal.oauth.domain.repository.IRefreshTokenRepository;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final EntityManager entityManager;
    private final IRefreshTokenRepository iRefreshTokenRepository;

    @Transactional
    public String getTokenByUserId(Long userId) {
        RefreshToken refreshToken = entityManager.createQuery("SELECT rt FROM RefreshToken rt JOIN FETCH rt.userInfo ui WHERE ui.id = :user_id", RefreshToken.class)
                .setParameter("user_id", userId)
                .getResultStream().findFirst().orElse(null);
        if (refreshToken == null) {
            return "";
        } else {
            return refreshToken.getToken();
        }
    }


}
