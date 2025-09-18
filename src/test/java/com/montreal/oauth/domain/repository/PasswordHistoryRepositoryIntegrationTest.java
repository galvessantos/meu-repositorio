package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.PasswordHistory;
import com.montreal.oauth.domain.entity.UserInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class PasswordHistoryRepositoryIntegrationTest {

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    private IUserRepository userRepository;

    private UserInfo testUser;
    private BCryptPasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        testUser = new UserInfo();
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("Test@123"));
        testUser.setEnabled(true);
        testUser.setCpf("12345678900");
        testUser = userRepository.save(testUser);
    }

    @Test
    void findLastPasswordsByUserId_ShouldReturnOrderedByCreatedAtDesc() {
        PasswordHistory password1 = createPasswordHistory("hash1", LocalDateTime.now().minusHours(2));
        PasswordHistory password2 = createPasswordHistory("hash2", LocalDateTime.now().minusHours(1));
        PasswordHistory password3 = createPasswordHistory("hash3", LocalDateTime.now());

        passwordHistoryRepository.save(password1);
        passwordHistoryRepository.save(password2);
        passwordHistoryRepository.save(password3);

        List<PasswordHistory> result = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 3)
        );

        assertEquals(3, result.size());
        assertEquals("hash3", result.get(0).getPasswordHash());
        assertEquals("hash2", result.get(1).getPasswordHash());
        assertEquals("hash1", result.get(2).getPasswordHash());
    }

    @Test
    void findLastPasswordsByUserId_WithPagination_ShouldRespectLimit() {
        LocalDateTime now = LocalDateTime.now();

        PasswordHistory password1 = createPasswordHistory("hash1", now.minusHours(5));
        PasswordHistory password2 = createPasswordHistory("hash2", now.minusHours(4));
        PasswordHistory password3 = createPasswordHistory("hash3", now.minusHours(3));
        PasswordHistory password4 = createPasswordHistory("hash4", now.minusHours(2));
        PasswordHistory password5 = createPasswordHistory("hash5", now.minusHours(1));

        passwordHistoryRepository.save(password1);
        passwordHistoryRepository.save(password2);
        passwordHistoryRepository.save(password3);
        passwordHistoryRepository.save(password4);
        passwordHistoryRepository.save(password5);

        passwordHistoryRepository.flush();
        entityManager.clear();

        List<PasswordHistory> result = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 3)
        );

        assertEquals(3, result.size());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
        assertEquals("hash5", result.get(0).getPasswordHash());
        assertTrue(result.get(1).getCreatedAt().isAfter(result.get(2).getCreatedAt()));
        assertEquals("hash4", result.get(1).getPasswordHash());
        assertEquals("hash3", result.get(2).getPasswordHash());
    }

    @Test
    void findLastPasswordsByUserId_EmptyHistory_ShouldReturnEmptyList() {
        List<PasswordHistory> result = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 3)
        );
        assertTrue(result.isEmpty());
    }

    @Test
    void cleanupOldPasswordHistory_ShouldRemoveExcessPasswords() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        PasswordHistory password1 = createPasswordHistory("hash1", baseTime.plusMinutes(10));
        PasswordHistory password2 = createPasswordHistory("hash2", baseTime.plusMinutes(20));
        PasswordHistory password3 = createPasswordHistory("hash3", baseTime.plusMinutes(30));
        PasswordHistory password4 = createPasswordHistory("hash4", baseTime.plusMinutes(40));
        PasswordHistory password5 = createPasswordHistory("hash5", baseTime.plusMinutes(50));

        passwordHistoryRepository.saveAll(List.of(password1, password2, password3, password4, password5));
        passwordHistoryRepository.flush();
        entityManager.clear();

        List<PasswordHistory> beforeCleanup = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 10)
        );
        assertEquals(5, beforeCleanup.size());

        passwordHistoryRepository.cleanupOldPasswordHistory(testUser.getId(), 3);
        passwordHistoryRepository.flush();
        entityManager.clear();

        List<PasswordHistory> afterCleanup = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 10)
        );

        assertEquals(3, afterCleanup.size());
        assertEquals("hash5", afterCleanup.get(0).getPasswordHash());
        assertEquals("hash4", afterCleanup.get(1).getPasswordHash());
        assertEquals("hash3", afterCleanup.get(2).getPasswordHash());
        assertTrue(afterCleanup.get(0).getCreatedAt().isAfter(afterCleanup.get(1).getCreatedAt()));
        assertTrue(afterCleanup.get(1).getCreatedAt().isAfter(afterCleanup.get(2).getCreatedAt()));
    }

    @Test
    void cleanupOldPasswordHistory_WithExactMaxHistory_ShouldNotRemoveAny() {
        for (int i = 1; i <= 3; i++) {
            PasswordHistory password = createPasswordHistory("hash" + i, LocalDateTime.now().minusHours(i));
            passwordHistoryRepository.save(password);
        }

        passwordHistoryRepository.cleanupOldPasswordHistory(testUser.getId(), 3);

        List<PasswordHistory> result = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 10)
        );
        assertEquals(3, result.size());
    }

    @Test
    void cleanupOldPasswordHistory_WithLessThanMaxHistory_ShouldNotRemoveAny() {
        for (int i = 1; i <= 2; i++) {
            PasswordHistory password = createPasswordHistory("hash" + i, LocalDateTime.now().minusHours(i));
            passwordHistoryRepository.save(password);
        }

        passwordHistoryRepository.cleanupOldPasswordHistory(testUser.getId(), 3);

        List<PasswordHistory> result = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 10)
        );
        assertEquals(2, result.size());
    }

    @Test
    void findLastPasswordsByUserId_DifferentUsers_ShouldNotInterfere() {
        UserInfo user2 = new UserInfo();
        user2.setUsername("user2@example.com");
        user2.setEmail("user2@example.com");
        user2.setPassword(passwordEncoder.encode("Test@123"));
        user2.setEnabled(true);
        user2.setCpf("98765432100");
        user2 = userRepository.save(user2);

        PasswordHistory user1Password = createPasswordHistory("hash1", LocalDateTime.now());
        PasswordHistory user2Password = createPasswordHistory("hash2", LocalDateTime.now());
        user2Password.setUser(user2);

        passwordHistoryRepository.save(user1Password);
        passwordHistoryRepository.save(user2Password);

        List<PasswordHistory> user1Result = passwordHistoryRepository.findLastPasswordsByUserId(
                testUser.getId(), PageRequest.of(0, 3)
        );
        List<PasswordHistory> user2Result = passwordHistoryRepository.findLastPasswordsByUserId(
                user2.getId(), PageRequest.of(0, 3)
        );

        assertEquals(1, user1Result.size());
        assertEquals("hash1", user1Result.get(0).getPasswordHash());

        assertEquals(1, user2Result.size());
        assertEquals("hash2", user2Result.get(0).getPasswordHash());
    }

    private PasswordHistory createPasswordHistory(String passwordHash, LocalDateTime createdAt) {
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUser(testUser);
        passwordHistory.setPasswordHash(passwordHash);
        passwordHistory.setCreatedAt(createdAt);
        return passwordHistory;
    }
}
