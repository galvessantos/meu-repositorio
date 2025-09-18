package com.montreal.oauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.montreal.oauth.domain.dto.request.PasswordResetGenerateRequest;
import com.montreal.oauth.domain.dto.request.PasswordResetRequest;
import com.montreal.oauth.domain.dto.response.PasswordResetGenerateResponse;
import com.montreal.oauth.domain.dto.response.PasswordResetResponse;
import com.montreal.oauth.domain.dto.response.PasswordResetValidateResponse;
import com.montreal.oauth.domain.dto.response.ResetPasswordResult;
import com.montreal.oauth.domain.dto.response.LoginResponseDTO;
import com.montreal.oauth.domain.service.IPasswordResetService;
import com.montreal.core.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerUnitTest {

    @Mock
    private IPasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        reset(passwordResetService);
    }

    @Test
    void generatePasswordResetToken_ValidLogin_ReturnsSuccess() {
        String login = "test@example.com";
        String resetLink = "https://example.com/reset-password?token=uuid-123";
        PasswordResetGenerateRequest request = new PasswordResetGenerateRequest();
        request.setLogin(login);

        when(passwordResetService.generatePasswordResetToken(login)).thenReturn(resetLink);

        ResponseEntity<PasswordResetGenerateResponse> response = passwordResetController.generatePasswordResetToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Password reset token generated successfully", response.getBody().getMessage());
        assertEquals(resetLink, response.getBody().getResetLink());

        verify(passwordResetService).generatePasswordResetToken(login);
    }

    @Test
    void generatePasswordResetToken_InvalidLogin_ThrowsUserNotFoundException() {
        String login = "invalid@example.com";
        PasswordResetGenerateRequest request = new PasswordResetGenerateRequest();
        request.setLogin(login);

        when(passwordResetService.generatePasswordResetToken(login))
                .thenThrow(new UserNotFoundException("Login informado inválido"));

        ResponseEntity<PasswordResetGenerateResponse> response = passwordResetController.generatePasswordResetToken(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login informado inválido", response.getBody().getMessage());
        assertNull(response.getBody().getResetLink());

        verify(passwordResetService).generatePasswordResetToken(login);
    }

    @Test
    void generatePasswordResetToken_GenericException_ThrowsException() {
        String login = "test@example.com";
        PasswordResetGenerateRequest request = new PasswordResetGenerateRequest();
        request.setLogin(login);

        when(passwordResetService.generatePasswordResetToken(login))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            passwordResetController.generatePasswordResetToken(request);
        });

        verify(passwordResetService).generatePasswordResetToken(login);
    }

    @Test
    void validatePasswordResetToken_ValidToken_ReturnsSuccess() {
        String token = "valid-token-123";
        when(passwordResetService.validatePasswordResetToken(token)).thenReturn(true);

        ResponseEntity<PasswordResetValidateResponse> response = passwordResetController.validatePasswordResetToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        assertEquals("Token is valid", response.getBody().getMessage());

        verify(passwordResetService).validatePasswordResetToken(token);
    }

    @Test
    void validatePasswordResetToken_InvalidToken_ReturnsFalse() {
        String token = "invalid-token-123";
        when(passwordResetService.validatePasswordResetToken(token)).thenReturn(false);

        ResponseEntity<PasswordResetValidateResponse> response = passwordResetController.validatePasswordResetToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertEquals("Token is invalid or expired", response.getBody().getMessage());

        verify(passwordResetService).validatePasswordResetToken(token);
    }

    @Test
    void cleanupExpiredTokens_Success() {
        ResponseEntity<String> response = passwordResetController.cleanupExpiredTokens();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cleanup completed successfully", response.getBody());

        verify(passwordResetService).cleanupExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_ThrowsException() {
        doThrow(new RuntimeException("Database error"))
                .when(passwordResetService).cleanupExpiredTokens();

        assertThrows(RuntimeException.class, () -> {
            passwordResetController.cleanupExpiredTokens();
        });

        verify(passwordResetService).cleanupExpiredTokens();
    }

    @Test
    void resetPassword_ValidRequest_ReturnsSuccessWithAutoLogin() {
        String token = "valid-token-123";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

        ResetPasswordResult mockResult = ResetPasswordResult.builder()
                .success(true)
                .message("Senha redefinida com sucesso")
                .accessToken("eyJhbGciOiJIUzI1NiJ9...")
                .refreshToken("refresh-token-123")
                .userDetails(createMockLoginResponseDTO())
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword))
                .thenReturn(mockResult);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("Senha redefinida com sucesso", response.getBody().getMessage());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());
        assertNotNull(response.getBody().getUserDetails());

        verify(passwordResetService).resetPasswordWithTokens(token, newPassword, confirmPassword);
    }

    @Test
    void resetPassword_ValidRequestWithoutAutoLogin_ReturnsSuccess() {
        String token = "valid-token-123";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

        ResetPasswordResult mockResult = ResetPasswordResult.builder()
                .success(true)
                .message("Senha redefinida com sucesso")
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword))
                .thenReturn(mockResult);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("Senha redefinida com sucesso", response.getBody().getMessage());
        assertNull(response.getBody().getAccessToken());
        assertNull(response.getBody().getRefreshToken());
        assertNull(response.getBody().getUserDetails());

        verify(passwordResetService).resetPasswordWithTokens(token, newPassword, confirmPassword);
    }

    @Test
    void resetPassword_InvalidToken_ReturnsBadRequest() {
        String token = "invalid-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

        ResetPasswordResult mockResult = ResetPasswordResult.builder()
                .success(false)
                .message("Token inválido ou expirado")
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword))
                .thenReturn(mockResult);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("Token inválido ou expirado", response.getBody().getMessage());

        verify(passwordResetService).resetPasswordWithTokens(token, newPassword, confirmPassword);
    }

    @Test
    void resetPassword_InvalidPassword_ReturnsBadRequest() {
        String token = "valid-token";
        String invalidPassword = "weak";
        String confirmPassword = "weak";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(invalidPassword)
                .confirmPassword(confirmPassword)
                .build();

        ResetPasswordResult mockResult = ResetPasswordResult.builder()
                .success(false)
                .message("A senha deve ter entre 4 e 8 caracteres")
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, invalidPassword, confirmPassword))
                .thenReturn(mockResult);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("A senha deve ter entre 4 e 8 caracteres", response.getBody().getMessage());

        verify(passwordResetService).resetPasswordWithTokens(token, invalidPassword, confirmPassword);
    }

    @Test
    void resetPassword_SamePasswordAsCurrent_ReturnsBadRequest() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

        ResetPasswordResult mockResult = ResetPasswordResult.builder()
                .success(false)
                .message("A nova senha não pode ser igual à senha atual")
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword))
                .thenReturn(mockResult);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("A nova senha não pode ser igual à senha atual", response.getBody().getMessage());

        verify(passwordResetService).resetPasswordWithTokens(token, newPassword, confirmPassword);
    }

    @Test
    void resetPassword_ServiceThrowsException_ReturnsInternalServerError() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@123";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("Erro interno do servidor", response.getBody().getMessage());

        verify(passwordResetService).resetPasswordWithTokens(token, newPassword, confirmPassword);
    }

    @Test
    void resetPassword_PasswordMismatch_ReturnsBadRequest() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = "Test@456";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

        ResetPasswordResult mockResult = ResetPasswordResult.builder()
                .success(false)
                .message("As senhas não coincidem")
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword))
                .thenReturn(mockResult);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("As senhas não coincidem", response.getBody().getMessage());

        verify(passwordResetService).resetPasswordWithTokens(token, newPassword, confirmPassword);
    }

    @Test
    void resetPassword_EmptyConfirmPassword_ReturnsBadRequest() {
        String token = "valid-token";
        String newPassword = "Test@123";
        String confirmPassword = "";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(token)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

        ResetPasswordResult mockResult = ResetPasswordResult.builder()
                .success(false)
                .message("Confirmação de senha é obrigatória")
                .build();

        when(passwordResetService.resetPasswordWithTokens(token, newPassword, confirmPassword))
                .thenReturn(mockResult);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isSuccess());
        assertEquals("Confirmação de senha é obrigatória", response.getBody().getMessage());

        verify(passwordResetService).resetPasswordWithTokens(token, newPassword, confirmPassword);
    }

    private LoginResponseDTO createMockLoginResponseDTO() {
        LoginResponseDTO.LoginUserDTO user = LoginResponseDTO.LoginUserDTO.builder()
                .id(1L)
                .username("testuser")
                .roles(List.of("ROLE_USER"))
                .enabled(true)
                .build();

        LoginResponseDTO.LoginPermissionDTO permission = LoginResponseDTO.LoginPermissionDTO.builder()
                .action("read")
                .subject("user")
                .build();

        LoginResponseDTO.LoginFunctionalityDTO functionality = LoginResponseDTO.LoginFunctionalityDTO.builder()
                .name("basic")
                .build();

        return LoginResponseDTO.builder()
                .user(user)
                .permissions(List.of(permission))
                .functionalities(List.of(functionality))
                .build();
    }
}