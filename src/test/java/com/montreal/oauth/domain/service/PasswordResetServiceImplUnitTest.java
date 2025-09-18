package com.montreal.oauth.domain.service;

import com.montreal.core.domain.service.EmailService;
import com.montreal.oauth.domain.dto.response.ResetPasswordResult;
import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.PasswordResetToken;
import com.montreal.oauth.domain.entity.RefreshToken;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.domain.repository.IPasswordResetTokenRepository;
import com.montreal.oauth.domain.repository.IUserRepository;
import com.montreal.core.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplUnitTest {

    @Mock
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private UserInfo mockUser;
    private PasswordResetToken mockToken;
    private PasswordResetToken mockExpiredToken;
    private PasswordResetToken mockUsedToken;
    private RefreshToken testRefreshToken;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "tokenExpirationMinutes", 30);
        ReflectionTestUtils.setField(passwordResetService, "baseUrl", "https://localhost:5173");
        ReflectionTestUtils.setField(passwordResetService, "autoLoginAfterReset", true);

        mockUser = new UserInfo();
        mockUser.setId(1L);
        mockUser.setUsername("testuser@example.com");
        mockUser.setEmail("testuser@example.com");
        mockUser.setPassword("$2a$10$encoded.password.hash");
        mockUser.setEnabled(false);
        mockUser.setPasswordChangedByUser(false);

        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName(RoleEnum.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        mockUser.setRoles(roles);

        mockToken = PasswordResetToken.builder()
                .id(1L)
                .token("valid-token")
                .user(mockUser)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .isUsed(false)
                .build();

        mockExpiredToken = PasswordResetToken.builder()
                .id(2L)
                .token("expired-token")
                .user(mockUser)
                .createdAt(LocalDateTime.now().minusHours(2))
                .expiresAt(LocalDateTime.now().minusHours(1))
                .isUsed(false)
                .build();

        mockUsedToken = PasswordResetToken.builder()
                .id(3L)
                .token("used-token")
                .user(mockUser)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .isUsed(true)
                .usedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("refresh-token");

        testUserResponse = new UserResponse();
        testUserResponse.setId(1L);
        testUserResponse.setUsername("testuser@example.com");
        testUserResponse.setEmail("testuser@example.com");
        testUserResponse.setEnabled(true);
    }

    @Test
    void generatePasswordResetToken_ValidUser_ReturnsResetLink() {
        String login = "testuser@example.com";
        when(userRepository.findByUsername(login)).thenReturn(mockUser);
        when(passwordResetTokenRepository.findValidTokensByUserId(eq(1L), any(LocalDateTime.class))).thenReturn(java.util.List.of());
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(mockToken);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        String result = passwordResetService.generatePasswordResetToken(login);

        assertNotNull(result);
        assertTrue(result.startsWith("https://localhost:5173/reset-password?token="));
        verify(userRepository, times(2)).findByUsername(login);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendPasswordResetEmail(eq(mockUser.getEmail()), anyString());
    }

    @Test
    void generatePasswordResetToken_InvalidUser_ThrowsUserNotFoundException() {
        String login = "invaliduser";
        when(userRepository.findByUsername(login)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> {
            passwordResetService.generatePasswordResetToken(login);
        });

        verify(userRepository, times(1)).findByUsername(login);
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void validatePasswordResetToken_ValidToken_ReturnsTrue() {
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(mockToken));
        boolean result = passwordResetService.validatePasswordResetToken("valid-token");
        assertTrue(result);
        verify(passwordResetTokenRepository).findByToken("valid-token");
    }

    @Test
    void validatePasswordResetToken_ExpiredToken_ReturnsFalse() {
        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(mockExpiredToken));
        boolean result = passwordResetService.validatePasswordResetToken("expired-token");
        assertFalse(result);
        verify(passwordResetTokenRepository).findByToken("expired-token");
    }

    @Test
    void validatePasswordResetToken_UsedToken_ReturnsFalse() {
        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(mockUsedToken));
        boolean result = passwordResetService.validatePasswordResetToken("used-token");
        assertFalse(result);
        verify(passwordResetTokenRepository).findByToken("used-token");
    }

    @Test
    void validatePasswordResetToken_NonExistentToken_ReturnsFalse() {
        when(passwordResetTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());
        boolean result = passwordResetService.validatePasswordResetToken("non-existent-token");
        assertFalse(result);
        verify(passwordResetTokenRepository).findByToken("non-existent-token");
    }

    @Test
    void resetPasswordWithTokens_ValidRequest_ReturnsSuccess() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));
        when(userService.decryptSensitiveFields(mockUser)).thenReturn(mockUser);
        when(jwtService.GenerateToken("testuser@example.com")).thenReturn("access-token");
        when(refreshTokenService.getTokenByUserId(1L)).thenReturn("");
        when(refreshTokenService.createRefreshToken("testuser@example.com")).thenReturn(testRefreshToken);

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertTrue(result.isSuccess());
        assertEquals("Senha redefinida com sucesso", result.getMessage());
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void resetPasswordWithTokens_InvalidToken_ReturnsFailure() {
        String token = "invalid-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("Token inválido ou expirado", result.getMessage());
        assertNull(result.getAccessToken());
        assertNull(result.getRefreshToken());
        verify(passwordResetTokenRepository, times(1)).findByToken(token);
        verifyNoMoreInteractions(userService, jwtService, refreshTokenService);
    }

    @Test
    void resetPasswordWithTokens_ExpiredToken_ReturnsFailure() {
        String token = "expired-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockExpiredToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("Token inválido ou expirado", result.getMessage());
        verify(passwordResetTokenRepository, times(1)).findByToken(token);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void resetPasswordWithTokens_UsedToken_ReturnsFailure() {
        String token = "used-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockUsedToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("Token inválido ou expirado", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_PasswordMismatch_ReturnsFailure() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = "Different@123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("As senhas não coincidem", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_EmptyConfirmPassword_ReturnsFailure() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = "";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("Confirmação de senha é obrigatória", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_NullConfirmPassword_ReturnsFailure() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = null;

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword);

        assertFalse(result.isSuccess());
        assertEquals("Confirmação de senha é obrigatória", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_WeakPasswordTooShort_ReturnsFailure() {
        String token = "valid-token";
        String shortPassword = "ab@";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, shortPassword, shortPassword);

        assertFalse(result.isSuccess());
        assertEquals("A senha deve ter entre 4 e 8 caracteres", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_WeakPasswordTooLong_ReturnsFailure() {
        String token = "valid-token";
        String longPassword = "Test@123456";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, longPassword, longPassword);

        assertFalse(result.isSuccess());
        assertEquals("A senha deve ter entre 4 e 8 caracteres", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_WeakPasswordWithoutLowerCase_ReturnsFailure() {
        String token = "valid-token";
        String password = "TEST@123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, password, password);

        assertFalse(result.isSuccess());
        assertEquals("A senha deve conter pelo menos uma letra minúscula", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_WeakPasswordWithoutUpperCase_ReturnsFailure() {
        String token = "valid-token";
        String password = "test@123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, password, password);

        assertFalse(result.isSuccess());
        assertEquals("A senha deve conter pelo menos uma letra maiúscula", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_WeakPasswordWithoutNumber_ReturnsFailure() {
        String token = "valid-token";
        String password = "Test@abc";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, password, password);

        assertFalse(result.isSuccess());
        assertEquals("A senha deve conter pelo menos um número", result.getMessage());
    }

    @Test
    void resetPasswordWithTokens_WeakPasswordWithoutSpecialChar_ReturnsFailure() {
        String token = "valid-token";
        String password = "Test123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(token, password, password);

        assertFalse(result.isSuccess());
        assertEquals("A senha deve conter pelo menos um dos caracteres especiais: _ @ #", result.getMessage());
    }

    @Test
    void markTokenAsUsed_ExistingToken_MarksTokenAsUsed() {
        String token = "valid-token";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(mockToken);

        passwordResetService.markTokenAsUsed(token);

        verify(passwordResetTokenRepository).findByToken(token);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void markTokenAsUsed_NonExistentToken_DoesNothing() {
        String token = "non-existent-token";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        passwordResetService.markTokenAsUsed(token);

        verify(passwordResetTokenRepository).findByToken(token);
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void findByToken_ExistingToken_ReturnsToken() {
        String token = "valid-token";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        Optional<PasswordResetToken> result = passwordResetService.findByToken(token);

        assertTrue(result.isPresent());
        assertEquals(mockToken, result.get());
        verify(passwordResetTokenRepository).findByToken(token);
    }

    @Test
    void cleanupExpiredTokens_CallsRepository() {
        passwordResetService.cleanupExpiredTokens();
        verify(passwordResetTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}