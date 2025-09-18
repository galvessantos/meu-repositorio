package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.dto.response.LoginResponseDTO;
import com.montreal.oauth.domain.dto.response.ResetPasswordResult;
import com.montreal.oauth.domain.entity.PasswordResetToken;
import com.montreal.oauth.domain.entity.RefreshToken;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.core.domain.exception.UserNotFoundException;
import com.montreal.oauth.domain.repository.IPasswordResetTokenRepository;
import com.montreal.oauth.domain.repository.IUserRepository;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.msiav_bh.entity.Company;
import com.montreal.msiav_bh.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.montreal.core.domain.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements IPasswordResetService {

    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IUserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CompanyRepository companyRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired(required = false)
    private PasswordHistoryService passwordHistoryService;

    @Value("${app.password-reset.token.expiration-minutes:30}")
    private int tokenExpirationMinutes;

    @Value("${app.password-reset.base-url:https://localhost}")
    private String baseUrl;

    @Value("${app.password-reset.auto-login:true}")
    private boolean autoLoginAfterReset;

    @Override
    public String generatePasswordResetToken(String login) {
        log.info("Generating password reset token for login: {}", login);

        String tokenValue = createTokenInTransaction(login);

        try {
            UserInfo user = userRepository.findByUsername(login);
            if (user != null && user.getEmail() != null) {
                emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
                log.info("Password reset email sent successfully for user: {}", user.getUsername());
            }
        } catch (Exception e) {
            log.warn("Failed to send password reset email for user: {}, but token was created successfully. Error: {}",
                    login, e.getMessage(), e);
        }

        String resetLink = generateResetLink(tokenValue);
        log.info("Password reset token generated successfully for user: {}", login);
        return resetLink;
    }

    @Transactional
    private String createTokenInTransaction(String login) {
        log.debug("Creating password reset token in transaction for login: {}", login);

        UserInfo user = userRepository.findByUsername(login);
        if (user == null) {
            log.warn("Login not found: {}", login);
            throw new UserNotFoundException("Login informado inválido");
        }

        invalidateExistingTokens(user.getId());

        PasswordResetToken token = createNewToken(user);
        passwordResetTokenRepository.save(token);

        log.debug("Password reset token created in database for user: {}", user.getUsername());
        return token.getToken();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        log.debug("Validating password reset token: {}", token);

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            log.warn("Password reset token not found: {}", token);
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isExpired()) {
            log.warn("Password reset token expired: {}", token);
            return false;
        }

        if (resetToken.isUsed()) {
            log.warn("Password reset token already used: {}", token);
            return false;
        }

        log.debug("Password reset token is valid: {}", token);
        return true;
    }

    @Override
    @Transactional
    public void markTokenAsUsed(String token) {
        log.info("Marking password reset token as used: {}", token);

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            resetToken.setUsed(true);
            resetToken.setUsedAt(LocalDateTime.now());
            passwordResetTokenRepository.save(resetToken);
            log.info("Password reset token marked as used: {}", token);
        } else {
            log.warn("Password reset token not found for marking as used: {}", token);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");

        LocalDateTime now = LocalDateTime.now();
        passwordResetTokenRepository.deleteByExpiresAtBefore(now);

        log.info("Cleanup of expired password reset tokens completed");
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword, String confirmPassword) {
        ResetPasswordResult result = resetPasswordWithTokens(token, newPassword, confirmPassword);
        return result.isSuccess();
    }

    @Override
    @Transactional
    public ResetPasswordResult resetPasswordWithTokens(String token, String newPassword, String confirmPassword) {
        log.info("Attempting to reset password with token: {}", token);

        if (!validatePasswordResetToken(token)) {
            log.warn("Invalid or expired token for password reset: {}", token);
            return ResetPasswordResult.builder()
                    .success(false)
                    .message("Token inválido ou expirado")
                    .build();
        }

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            log.warn("Token not found for password reset: {}", token);
            return ResetPasswordResult.builder()
                    .success(false)
                    .message("Token inválido ou expirado")
                    .build();
        }

        PasswordResetToken resetToken = tokenOpt.get();
        UserInfo user = resetToken.getUser();

        try {
            validatePassword(newPassword);
            validatePasswordConfirmation(newPassword, confirmPassword);
            validatePasswordNotSameAsCurrent(user, newPassword);

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encodedPassword = encoder.encode(newPassword);

            if (passwordHistoryService != null) {
                passwordHistoryService.savePasswordToHistory(user, encodedPassword);
            }

            user.setPassword(encodedPassword);
            user.setPasswordChangedByUser(true);
            user.setEnabled(true);

            userRepository.save(user);
            markTokenAsUsed(token);

            log.info("Password reset successfully for user: {}", user.getUsername());

            if (autoLoginAfterReset) {
                return generateAutoLoginTokens(user);
            } else {
                return ResetPasswordResult.builder()
                        .success(true)
                        .message("Senha redefinida com sucesso")
                        .build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("Password validation failed: {}", e.getMessage());
            return ResetPasswordResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error during password reset", e);
            return ResetPasswordResult.builder()
                    .success(false)
                    .message("Erro interno do servidor")
                    .build();
        }
    }

    private void invalidateExistingTokens(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        var validTokens = passwordResetTokenRepository.findValidTokensByUserId(userId, now);

        for (PasswordResetToken token : validTokens) {
            token.setUsed(true);
            token.setUsedAt(now);
            passwordResetTokenRepository.save(token);
        }

        if (!validTokens.isEmpty()) {
            log.debug("Invalidated {} existing valid tokens for user: {}", validTokens.size(), userId);
        }
    }

    private PasswordResetToken createNewToken(UserInfo user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(tokenExpirationMinutes);

        return PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
    }

    private String generateResetLink(String token) {
        return String.format("%s/reset-password?token=%s", baseUrl, token);
    }

    private ResetPasswordResult generateAutoLoginTokens(UserInfo user) {
        try {
            log.info("Generating auto-login tokens for user: {}", user.getUsername());

            user = userService.decryptSensitiveFields(user);

            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> RoleEnum.ROLE_ADMIN.equals(role.getName()));

            if (!isAdmin && user.getCompanyId() != null) {
                Company company = companyRepository.findById(Long.valueOf(user.getCompanyId()))
                        .orElse(null);

                if (company == null || !company.getIsActive()) {
                    log.warn("Company is inactive for user: {}", user.getUsername());
                    return ResetPasswordResult.builder()
                            .success(true)
                            .message("Senha redefinida com sucesso, mas a empresa está inativa. Contate o administrador.")
                            .build();
                }
            }

            String accessToken = jwtService.GenerateToken(user.getUsername());

            String refreshTokenValue = refreshTokenService.getTokenByUserId(user.getId());
            if (refreshTokenValue.isEmpty()) {
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
                refreshTokenValue = refreshToken.getToken();
            }

            LoginResponseDTO userDetails = buildUserDetails(user, isAdmin);

            log.info("Auto-login tokens generated successfully for user: {}", user.getUsername());

            return ResetPasswordResult.builder()
                    .success(true)
                    .message("Senha redefinida com sucesso")
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenValue)
                    .userDetails(userDetails)
                    .build();

        } catch (Exception e) {
            log.error("Error generating auto-login tokens for user: {}", user.getUsername(), e);
            return ResetPasswordResult.builder()
                    .success(true)
                    .message("Senha redefinida com sucesso, mas houve erro no login automático. Faça login manualmente.")
                    .build();
        }
    }

    private LoginResponseDTO buildUserDetails(UserInfo user, boolean isAdmin) {
        LoginResponseDTO.LoginUserDTO userDetails = LoginResponseDTO.LoginUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
                .companyId(user.getCompanyId())
                .enabled(user.isEnabled())
                .build();

        List<LoginResponseDTO.LoginPermissionDTO> permissions = isAdmin ?
                List.of(LoginResponseDTO.LoginPermissionDTO.builder()
                        .action("manage")
                        .subject("all")
                        .build()) :
                user.getRoles().stream()
                        .flatMap(role -> role.getRolePermissions().stream())
                        .map(rp -> LoginResponseDTO.LoginPermissionDTO.builder()
                                .action(rp.getPermission().getAction())
                                .subject(rp.getPermission().getSubject())
                                .build())
                        .distinct()
                        .toList();

        List<LoginResponseDTO.LoginFunctionalityDTO> functionalities = isAdmin ?
                List.of(LoginResponseDTO.LoginFunctionalityDTO.builder()
                        .name("manageAll")
                        .build()) :
                user.getRoles().stream()
                        .flatMap(role -> role.getRoleFunctionalities().stream())
                        .map(rf -> LoginResponseDTO.LoginFunctionalityDTO.builder()
                                .name(rf.getFunctionality().getName())
                                .build())
                        .distinct()
                        .toList();

        return LoginResponseDTO.builder()
                .user(userDetails)
                .permissions(permissions)
                .functionalities(functionalities)
                .build();
    }

    private void validatePassword(String password) {
        log.debug("Validating password criteria");

        if (password == null || password.length() < 4 || password.length() > 8) {
            throw new IllegalArgumentException("A senha deve ter entre 4 e 8 caracteres");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra minúscula");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra maiúscula");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos um número");
        }

        if (!password.matches(".*[_@#].*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos um dos caracteres especiais: _ @ #");
        }

        log.debug("Password validation passed");
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        log.debug("Validating password confirmation");

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirmação de senha é obrigatória");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        log.debug("Password confirmation validation passed");
    }

    private void validatePasswordNotSameAsCurrent(UserInfo user, String newPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (encoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("A nova senha não pode ser igual à senha atual");
        }

        if (passwordHistoryService != null) {
            passwordHistoryService.validatePasswordHistory(user, newPassword);
        }
    }
}