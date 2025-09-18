package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.RefreshToken;
import com.montreal.oauth.domain.repository.infrastructure.CustomJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRefreshTokenRepository extends CustomJpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

}
