package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.entity.PasswordHistory;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.PasswordHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordHistoryServiceUnitTest {

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordHistoryService passwordHistoryService;

    private UserInfo testUser;
    private String newPassword;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordHistoryService, "maxPasswordHistory", 3);
        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setEnabled(true);
        newPassword = "Test@123";
        hashedPassword = "$2a$10$hashed.password.for.testing";
    }

    @Test
    void validatePasswordHistory_NoPreviousPasswords_ShouldPass() {
        when(passwordHistoryRepository.findLastPasswordsByUserId(1L, PageRequest.of(0, 3)))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                passwordHistoryService.validatePasswordHistory(testUser, newPassword)
        );

        verify(passwordHistoryRepository).findLastPasswordsByUserId(1L, PageRequest.of(0, 3));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void validatePasswordHistory_PasswordNotInHistory_ShouldPass() {
        List<PasswordHistory> previousPasswords = Arrays.asList(
                createPasswordHistory("$2a$10$old.password.1"),
                createPasswordHistory("$2a$10$old.password.2"),
                createPasswordHistory("$2a$10$old.password.3")
        );

        when(passwordHistoryRepository.findLastPasswordsByUserId(1L, PageRequest.of(0, 3)))
                .thenReturn(previousPasswords);

        when(passwordEncoder.matches(eq(newPassword), eq("$2a$10$old.password.1"))).thenReturn(false);
        when(passwordEncoder.matches(eq(newPassword), eq("$2a$10$old.password.2"))).thenReturn(false);
        when(passwordEncoder.matches(eq(newPassword), eq("$2a$10$old.password.3"))).thenReturn(false);

        assertDoesNotThrow(() ->
                passwordHistoryService.validatePasswordHistory(testUser, newPassword)
        );

        verify(passwordHistoryRepository).findLastPasswordsByUserId(1L, PageRequest.of(0, 3));
        verify(passwordEncoder, times(3)).matches(eq(newPassword), anyString());
    }

    @Test
    void validatePasswordHistory_PasswordInHistory_ShouldThrowException() {
        List<PasswordHistory> previousPasswords = Arrays.asList(
                createPasswordHistory("$2a$10$old.password.1"),
                createPasswordHistory("$2a$10$old.password.2")
        );

        when(passwordHistoryRepository.findLastPasswordsByUserId(1L, PageRequest.of(0, 3)))
                .thenReturn(previousPasswords);

        when(passwordEncoder.matches(eq(newPassword), eq("$2a$10$old.password.1"))).thenReturn(false);
        when(passwordEncoder.matches(eq(newPassword), eq("$2a$10$old.password.2"))).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                passwordHistoryService.validatePasswordHistory(testUser, newPassword)
        );

        assertEquals("Por questões de segurança, você não pode reutilizar uma senha já utilizada anteriormente. Por favor, escolha uma nova senha.", exception.getMessage());
        verify(passwordHistoryRepository).findLastPasswordsByUserId(1L, PageRequest.of(0, 3));
        verify(passwordEncoder, times(2)).matches(eq(newPassword), anyString());
    }

    @Test
    void savePasswordToHistory_ValidPassword_ShouldSaveAndCleanup() {
        when(passwordHistoryRepository.save(any(PasswordHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        passwordHistoryService.savePasswordToHistory(testUser, hashedPassword);

        verify(passwordHistoryRepository).save(any(PasswordHistory.class));
        verify(passwordHistoryRepository).cleanupOldPasswordHistory(1L, 3);
    }

    @Test
    void savePasswordToHistory_ShouldCreateCorrectPasswordHistory() {
        when(passwordHistoryRepository.save(any(PasswordHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        passwordHistoryService.savePasswordToHistory(testUser, hashedPassword);

        verify(passwordHistoryRepository).save(argThat(passwordHistory -> {
            assertEquals(testUser, passwordHistory.getUser());
            assertEquals(hashedPassword, passwordHistory.getPasswordHash());
            return true;
        }));
    }

    private PasswordHistory createPasswordHistory(String passwordHash) {
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setId(1L);
        passwordHistory.setUser(testUser);
        passwordHistory.setPasswordHash(passwordHash);
        passwordHistory.setCreatedAt(LocalDateTime.now());
        return passwordHistory;
    }
}
