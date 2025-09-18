package com.montreal.oauth.domain.service;

import com.montreal.config.TestConfig;
import com.montreal.oauth.domain.dto.response.ResetPasswordResult;
import com.montreal.oauth.domain.entity.PasswordResetToken;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.IPasswordResetTokenRepository;
import com.montreal.oauth.domain.repository.IUserRepository;
import com.montreal.core.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
class PasswordResetServiceImplIntegrationTest {

    @Autowired
    private PasswordResetServiceImpl passwordResetService;

    @Autowired
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private UserInfo testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserInfo();
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("$2a$10$test.hash.for.testing");
        testUser.setCpf("12345678900");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void generatePasswordResetToken_ValidLogin_ReturnsResetLink() {
        String login = "testuser@example.com";

        String result = passwordResetService.generatePasswordResetToken(login);

        assertNotNull(result, "Reset link should not be null");
        assertTrue(result.contains("token="), "Reset link should contain token parameter");
        assertTrue(result.contains("localhost") || result.contains("token="),
                "Reset link should contain localhost or token parameter");

        String token = extractTokenFromLink(result);
        assertNotNull(token, "Token should be extractable from link");
        assertFalse(token.isEmpty(), "Token should not be empty");

        Optional<PasswordResetToken> savedToken = passwordResetTokenRepository.findByToken(token);

        assertTrue(savedToken.isPresent(), "Token should be saved in database");
        assertEquals(testUser.getId(), savedToken.get().getUser().getId(), "Token should be associated with correct user");
        assertFalse(savedToken.get().isUsed(), "Token should not be marked as used initially");
        assertNotNull(savedToken.get().getCreatedAt(), "Token should have creation timestamp");
        assertNotNull(savedToken.get().getExpiresAt(), "Token should have expiration timestamp");
    }

    @Test
    void generatePasswordResetToken_InvalidLogin_ThrowsUserNotFoundException() {
        String login = "nonexistent@example.com";

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                passwordResetService.generatePasswordResetToken(login)
        );

        assertNotNull(exception.getMessage(), "Exception message should not be null");
        assertTrue(exception.getMessage().contains("Login informado inválido") ||
                        exception.getMessage().contains("não encontrado") ||
                        exception.getMessage().contains("invalid"),
                "Exception should indicate invalid login");
    }

    @Test
    void generatePasswordResetToken_InvalidatesExistingTokens() {
        String login = "testuser@example.com";

        String firstLink = passwordResetService.generatePasswordResetToken(login);
        String firstToken = extractTokenFromLink(firstLink);

        String secondLink = passwordResetService.generatePasswordResetToken(login);
        String secondToken = extractTokenFromLink(secondLink);

        assertNotEquals(firstToken, secondToken);

        Optional<PasswordResetToken> firstTokenEntity = passwordResetTokenRepository.findByToken(firstToken);
        assertTrue(firstTokenEntity.isPresent());
        assertTrue(firstTokenEntity.get().isUsed());

        Optional<PasswordResetToken> secondTokenEntity = passwordResetTokenRepository.findByToken(secondToken);
        assertTrue(secondTokenEntity.isPresent());
        assertFalse(secondTokenEntity.get().isUsed());
    }

    @Test
    void validatePasswordResetToken_ValidToken_ReturnsTrue() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);

        boolean result = passwordResetService.validatePasswordResetToken(token);

        assertTrue(result);
    }

    @Test
    void validatePasswordResetToken_InvalidToken_ReturnsFalse() {
        String token = "invalid-token-123";

        boolean result = passwordResetService.validatePasswordResetToken(token);

        assertFalse(result);
    }

    @Test
    void validatePasswordResetToken_ExpiredToken_ReturnsFalse() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);

        Optional<PasswordResetToken> tokenEntity = passwordResetTokenRepository.findByToken(token);
        assertTrue(tokenEntity.isPresent());

        PasswordResetToken expiredToken = tokenEntity.get();
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        passwordResetTokenRepository.save(expiredToken);

        boolean result = passwordResetService.validatePasswordResetToken(token);

        assertFalse(result);
    }

    @Test
    void validatePasswordResetToken_UsedToken_ReturnsFalse() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);

        passwordResetService.markTokenAsUsed(token);

        boolean result = passwordResetService.validatePasswordResetToken(token);

        assertFalse(result);
    }

    @Test
    void markTokenAsUsed_ValidToken_ShouldMarkAsUsed() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);

        passwordResetService.markTokenAsUsed(token);

        Optional<PasswordResetToken> usedToken = passwordResetTokenRepository.findByToken(token);
        assertTrue(usedToken.isPresent());
        assertTrue(usedToken.get().isUsed());
        assertNotNull(usedToken.get().getUsedAt());
    }

    @Test
    void cleanupExpiredTokens_ShouldRemoveExpiredTokens() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);

        Optional<PasswordResetToken> tokenEntity = passwordResetTokenRepository.findByToken(token);
        assertTrue(tokenEntity.isPresent());

        PasswordResetToken expiredToken = tokenEntity.get();
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        passwordResetTokenRepository.save(expiredToken);

        passwordResetService.cleanupExpiredTokens();

        Optional<PasswordResetToken> cleanedToken = passwordResetTokenRepository.findByToken(token);
        assertFalse(cleanedToken.isPresent());
    }

    @Test
    void resetPasswordWithTokens_ValidRequest_ReturnsSuccess() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);
        String newPassword = "P1@a";
        String confirmPassword = "P1@a";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertTrue(result.isSuccess(), "Expected success but got: " + result.getMessage());
        assertEquals("Senha redefinida com sucesso", result.getMessage());

        Optional<PasswordResetToken> usedToken = passwordResetTokenRepository.findByToken(token);
        assertTrue(usedToken.isPresent());
        assertTrue(usedToken.get().isUsed());
        assertNotNull(usedToken.get().getUsedAt());

        UserInfo updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    void resetPasswordWithTokens_InvalidToken_ReturnsFailure() {
        String token = "invalid-token-123";
        String newPassword = "P1@a";
        String confirmPassword = "P1@a";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("Token inválido ou expirado", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_PasswordMismatch_ReturnsFailure() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);
        String newPassword = "P1@a";
        String confirmPassword = "D1@b";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("As senhas não coincidem", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_WeakPassword_ReturnsFailure() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);
        String newPassword = "a";
        String confirmPassword = "a";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess(), "Expected failure for weak password but got success");
        assertNotNull(result.getMessage(), "Error message should not be null");
        assertFalse(result.getMessage().isEmpty(), "Error message should not be empty");
    }

    @Test
    void resetPasswordWithTokens_PasswordMissingUpperCase_ReturnsFailure() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);
        String newPassword = "pass1";
        String confirmPassword = "pass1";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess(), "Password without uppercase should fail");
        assertNotNull(result.getMessage(), "Error message should not be null");
        assertFalse(result.getMessage().isEmpty(), "Error message should not be empty");
    }

    @Test
    void resetPasswordWithTokens_PasswordMissingSpecialChar_ReturnsFailure() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);
        String newPassword = "Pass1";
        String confirmPassword = "Pass1";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess(), "Password without special character should fail");
        assertNotNull(result.getMessage(), "Error message should not be null");
        assertFalse(result.getMessage().isEmpty(), "Error message should not be empty");
    }

    @Test
    void resetPasswordWithTokens_PasswordMissingNumber_ReturnsFailure() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);
        String newPassword = "Pass@";
        String confirmPassword = "Pass@";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess(), "Password without number should fail");
        assertNotNull(result.getMessage(), "Error message should not be null");
        assertFalse(result.getMessage().isEmpty(), "Error message should not be empty");
    }

    @Test
    void resetPasswordWithTokens_PasswordTooShort_ReturnsFailure() {
        String login = "testuser@example.com";
        String resetLink = passwordResetService.generatePasswordResetToken(login);
        String token = extractTokenFromLink(resetLink);
        String newPassword = "P1@";
        String confirmPassword = "P1@";

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess(), "Password too short should fail");
        assertNotNull(result.getMessage(), "Error message should not be null");
        assertFalse(result.getMessage().isEmpty(), "Error message should not be empty");
    }

    private String extractTokenFromLink(String resetLink) {
        if (resetLink == null || !resetLink.contains("token=")) {
            throw new IllegalArgumentException("Invalid reset link format: " + resetLink);
        }
        return resetLink.substring(resetLink.indexOf("token=") + 6);
    }
}