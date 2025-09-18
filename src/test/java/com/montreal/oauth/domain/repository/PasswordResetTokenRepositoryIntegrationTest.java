package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.PasswordResetToken;
import com.montreal.oauth.domain.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class PasswordResetTokenRepositoryIntegrationTest {

    @Autowired
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private IUserRepository userRepository;

    private UserInfo testUser;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        testUser = new UserInfo();
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("Test@123"));
        testUser.setCpf("12345678900");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void findByToken_ValidToken_ReturnsToken() {
        PasswordResetToken token = createPasswordResetToken("test-token-123");
        passwordResetTokenRepository.save(token);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("test-token-123");

        assertTrue(result.isPresent());
        assertEquals("test-token-123", result.get().getToken());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    void findByToken_InvalidToken_ReturnsEmpty() {
        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("invalid-token");

        assertFalse(result.isPresent());
    }

    @Test
    void findByUser_Id_ShouldReturnUserTokens() {
        PasswordResetToken token1 = createPasswordResetToken("token-1");
        PasswordResetToken token2 = createPasswordResetToken("token-2");
        passwordResetTokenRepository.save(token1);
        passwordResetTokenRepository.save(token2);

        List<PasswordResetToken> result = passwordResetTokenRepository.findByUser_Id(testUser.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getToken().equals("token-1")));
        assertTrue(result.stream().anyMatch(t -> t.getToken().equals("token-2")));
    }

    @Test
    void findValidTokensByUserId_ShouldReturnOnlyValidTokens() {
        PasswordResetToken validToken = createPasswordResetToken("valid-token");
        PasswordResetToken expiredToken = createExpiredToken("expired-token");
        PasswordResetToken usedToken = createPasswordResetToken("used-token");
        usedToken.setUsed(true);
        usedToken.setUsedAt(LocalDateTime.now());

        passwordResetTokenRepository.save(validToken);
        passwordResetTokenRepository.save(expiredToken);
        passwordResetTokenRepository.save(usedToken);

        List<PasswordResetToken> result = passwordResetTokenRepository.findValidTokensByUserId(
                testUser.getId(), LocalDateTime.now());

        assertEquals(1, result.size());
        assertEquals("valid-token", result.get(0).getToken());
    }

    @Test
    void findExpiredUnusedTokens_ShouldReturnExpiredTokens() {
        PasswordResetToken validToken = createPasswordResetToken("valid-token");
        PasswordResetToken expiredToken = createExpiredToken("expired-token");
        PasswordResetToken usedExpiredToken = createExpiredToken("used-expired-token");
        usedExpiredToken.setUsed(true);

        passwordResetTokenRepository.save(validToken);
        passwordResetTokenRepository.save(expiredToken);
        passwordResetTokenRepository.save(usedExpiredToken);


        List<PasswordResetToken> result = passwordResetTokenRepository.findExpiredUnusedTokens(LocalDateTime.now());

        assertEquals(1, result.size());
        assertEquals("expired-token", result.get(0).getToken());
    }

    @Test
    void existsValidTokenByUserId_ShouldReturnTrueWhenValidTokenExists() {
        PasswordResetToken validToken = createPasswordResetToken("valid-token");
        passwordResetTokenRepository.save(validToken);

        boolean result = passwordResetTokenRepository.existsValidTokenByUserId(
                testUser.getId(), LocalDateTime.now());

        assertTrue(result);
    }

    @Test
    void existsValidTokenByUserId_ShouldReturnFalseWhenNoValidToken() {
        boolean result = passwordResetTokenRepository.existsValidTokenByUserId(
                testUser.getId(), LocalDateTime.now());

        assertFalse(result);
    }

    @Test
    void deleteByUser_Id_ShouldRemoveAllUserTokens() {
        PasswordResetToken token1 = createPasswordResetToken("token-1");
        PasswordResetToken token2 = createPasswordResetToken("token-2");
        passwordResetTokenRepository.save(token1);
        passwordResetTokenRepository.save(token2);

        passwordResetTokenRepository.deleteByUser_Id(testUser.getId());

        List<PasswordResetToken> remainingTokens = passwordResetTokenRepository.findByUser_Id(testUser.getId());
        assertTrue(remainingTokens.isEmpty());
    }

    @Test
    void deleteByExpiresAtBefore_ShouldRemoveExpiredTokens() {
        PasswordResetToken validToken = createPasswordResetToken("valid-token");
        PasswordResetToken expiredToken = createExpiredToken("expired-token");
        passwordResetTokenRepository.save(validToken);
        passwordResetTokenRepository.save(expiredToken);

        passwordResetTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        Optional<PasswordResetToken> remainingValidToken = passwordResetTokenRepository.findByToken("valid-token");
        Optional<PasswordResetToken> removedExpiredToken = passwordResetTokenRepository.findByToken("expired-token");

        assertTrue(remainingValidToken.isPresent());
        assertFalse(removedExpiredToken.isPresent());
    }

    @Test
    void save_ShouldPersistTokenWithCorrectData() {
        PasswordResetToken token = createPasswordResetToken("new-token");

        PasswordResetToken savedToken = passwordResetTokenRepository.save(token);

        assertNotNull(savedToken.getId());
        assertEquals("new-token", savedToken.getToken());
        assertEquals(testUser.getId(), savedToken.getUser().getId());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());
        assertFalse(savedToken.isUsed());
        assertNull(savedToken.getUsedAt());
    }

    @Test
    void entityIsExpired_ValidToken_ReturnsFalse() {
        PasswordResetToken token = createPasswordResetToken("valid-token");
        passwordResetTokenRepository.save(token);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("valid-token");

        assertTrue(result.isPresent());
        assertFalse(result.get().isExpired());
    }

    @Test
    void entityIsExpired_ExpiredToken_ReturnsTrue() {
        PasswordResetToken expiredToken = createExpiredToken("expired-token");
        passwordResetTokenRepository.save(expiredToken);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("expired-token");

        assertTrue(result.isPresent());
        assertTrue(result.get().isExpired());
    }

    @Test
    void entityIsValid_ValidToken_ReturnsTrue() {
        PasswordResetToken token = createPasswordResetToken("valid-token");
        passwordResetTokenRepository.save(token);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("valid-token");

        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
    }

    @Test
    void entityIsValid_UsedToken_ReturnsFalse() {
        PasswordResetToken token = createPasswordResetToken("used-token");
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("used-token");

        assertTrue(result.isPresent());
        assertFalse(result.get().isValid());
    }

    @Test
    void entityIsValid_ExpiredToken_ReturnsFalse() {
        PasswordResetToken expiredToken = createExpiredToken("expired-token");
        passwordResetTokenRepository.save(expiredToken);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken("expired-token");

        assertTrue(result.isPresent());
        assertFalse(result.get().isValid());
    }

    private PasswordResetToken createPasswordResetToken(String tokenValue) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(testUser);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        return token;
    }

    private PasswordResetToken createExpiredToken(String tokenValue) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(testUser);
        token.setCreatedAt(LocalDateTime.now().minusHours(1));
        token.setExpiresAt(LocalDateTime.now().minusMinutes(30));
        token.setUsed(false);
        return token;
    }
}