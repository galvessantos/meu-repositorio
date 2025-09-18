package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.entity.PasswordHistory;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "security.password-history.enabled", havingValue = "true", matchIfMissing = false)
public class PasswordHistoryService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${security.password-history.max-history:3}")
    private int maxPasswordHistory;

    public void validatePasswordHistory(UserInfo user, String newPassword) {
        log.debug("Validando histórico de senhas para usuário: {}", user.getUsername());

        List<PasswordHistory> lastPasswords = passwordHistoryRepository
                .findLastPasswordsByUserId(user.getId(), PageRequest.of(0, maxPasswordHistory));

        for (PasswordHistory passwordHistory : lastPasswords) {
            if (passwordEncoder.matches(newPassword, passwordHistory.getPasswordHash())) {
                log.warn("Usuário {} tentou reutilizar uma senha anterior", user.getUsername());
                throw new IllegalArgumentException("Por questões de segurança, você não pode reutilizar uma senha já utilizada anteriormente. Por favor, escolha uma nova senha.");
            }
        }

        log.debug("Validação de histórico de senhas aprovada para usuário: {}", user.getUsername());
    }

    @Transactional
    public void savePasswordToHistory(UserInfo user, String passwordHash) {
        log.info("Salvando nova senha no histórico para usuário: {}", user.getUsername());

        PasswordHistory passwordHistory = PasswordHistory.builder()
                .user(user)
                .passwordHash(passwordHash)
                .build();

        passwordHistoryRepository.save(passwordHistory);

        passwordHistoryRepository.cleanupOldPasswordHistory(user.getId(), maxPasswordHistory);

        log.info("Senha salva no histórico e senhas antigas removidas para usuário: {}", user.getUsername());
    }
}