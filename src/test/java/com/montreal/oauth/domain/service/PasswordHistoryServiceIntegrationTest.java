package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.entity.PasswordHistory;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.PasswordHistoryRepository;
import com.montreal.oauth.domain.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PasswordHistoryServiceIntegrationTest {

    @Autowired
    private PasswordHistoryService passwordHistoryService;

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

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
    void validatePasswordHistory_NoPreviousPasswords_ShouldPass() {
        String newPassword = "NewPass@123";

        assertDoesNotThrow(() ->
                passwordHistoryService.validatePasswordHistory(testUser, newPassword)
        );
    }

    @Test
    void validatePasswordHistory_PasswordNotInHistory_ShouldPass() {
        String oldPassword1 = "OldPass@123";
        String oldPassword2 = "OldPass@456";
        String newPassword = "NewPass@123";

        passwordHistoryService.savePasswordToHistory(testUser, passwordEncoder.encode(oldPassword1));
        passwordHistoryService.savePasswordToHistory(testUser, passwordEncoder.encode(oldPassword2));

        assertDoesNotThrow(() ->
                passwordHistoryService.validatePasswordHistory(testUser, newPassword)
        );
    }

    @Test
    void validatePasswordHistory_PasswordInHistory_ShouldThrowException() {
        String oldPassword = "OldPass@123";
        String newPassword = oldPassword;

        passwordHistoryService.savePasswordToHistory(testUser, passwordEncoder.encode(oldPassword));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                passwordHistoryService.validatePasswordHistory(testUser, newPassword)
        );

        assertEquals("Por questões de segurança, você não pode reutilizar uma senha já utilizada anteriormente. Por favor, escolha uma nova senha.", exception.getMessage());
    }

    @Test
    void savePasswordToHistory_ShouldSaveToDatabase() {
        String passwordHash = passwordEncoder.encode("Test@123");

        passwordHistoryService.savePasswordToHistory(testUser, passwordHash);

        List<PasswordHistory> savedPasswords = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 3)
        );

        assertEquals(1, savedPasswords.size());
        assertEquals(testUser.getId(), savedPasswords.get(0).getUser().getId());
        assertEquals(passwordHash, savedPasswords.get(0).getPasswordHash());
        assertNotNull(savedPasswords.get(0).getCreatedAt());
    }

    @Test
    void savePasswordToHistory_MultiplePasswords_ShouldMaintainOrder() {
        String password1 = passwordEncoder.encode("Pass1@123");
        String password2 = passwordEncoder.encode("Pass2@123");
        String password3 = passwordEncoder.encode("Pass3@123");

        passwordHistoryService.savePasswordToHistory(testUser, password1);
        passwordHistoryService.savePasswordToHistory(testUser, password2);
        passwordHistoryService.savePasswordToHistory(testUser, password3);

        List<PasswordHistory> savedPasswords = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 5)
        );

        assertEquals(3, savedPasswords.size());
        assertEquals(password3, savedPasswords.get(0).getPasswordHash());
        assertEquals(password2, savedPasswords.get(1).getPasswordHash());
        assertEquals(password1, savedPasswords.get(2).getPasswordHash());
    }

    @Test
    void savePasswordToHistory_ExceedsMaxHistory_ShouldCleanupOldPasswords() {
        String password1 = passwordEncoder.encode("Pass1@123");
        String password2 = passwordEncoder.encode("Pass2@123");
        String password3 = passwordEncoder.encode("Pass3@123");
        String password4 = passwordEncoder.encode("Pass4@123");
        String password5 = passwordEncoder.encode("Pass5@123");

        passwordHistoryService.savePasswordToHistory(testUser, password1);
        passwordHistoryService.savePasswordToHistory(testUser, password2);
        passwordHistoryService.savePasswordToHistory(testUser, password3);
        passwordHistoryService.savePasswordToHistory(testUser, password4);
        passwordHistoryService.savePasswordToHistory(testUser, password5);

        List<PasswordHistory> savedPasswords = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 10)
        );

        assertEquals(3, savedPasswords.size());
        assertEquals(password5, savedPasswords.get(0).getPasswordHash());
        assertEquals(password4, savedPasswords.get(1).getPasswordHash());
        assertEquals(password3, savedPasswords.get(2).getPasswordHash());
    }

    @Test
    void validatePasswordHistory_WithMaxHistoryLimit_ShouldRespectLimit() {
        String oldPassword1 = "OldPass@123";
        String oldPassword2 = "OldPass@456";
        String oldPassword3 = "OldPass@789";
        String oldPassword4 = "OldPass@012";
        String newPassword = oldPassword1;

        passwordHistoryService.savePasswordToHistory(testUser, passwordEncoder.encode(oldPassword1));
        passwordHistoryService.savePasswordToHistory(testUser, passwordEncoder.encode(oldPassword2));
        passwordHistoryService.savePasswordToHistory(testUser, passwordEncoder.encode(oldPassword3));
        passwordHistoryService.savePasswordToHistory(testUser, passwordEncoder.encode(oldPassword4));

        List<PasswordHistory> currentHistory = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 10)
        );
        System.out.println("Histórico atual após salvar 4 senhas:");
        currentHistory.forEach(ph -> System.out.println("Hash: " + ph.getPasswordHash()));

        assertDoesNotThrow(() ->
                passwordHistoryService.validatePasswordHistory(testUser, newPassword)
        );
    }
}
