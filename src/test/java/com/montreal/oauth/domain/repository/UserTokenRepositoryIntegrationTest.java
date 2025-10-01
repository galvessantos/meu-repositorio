package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.entity.UserToken;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class UserTokenRepositoryIntegrationTest {

    @Autowired
    private IUserTokenRepository userTokenRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    private UserInfo testUser;
    private UserInfo anotherUser;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);
        userRole.setRequiresTokenFirstLogin(false);
        userRole.setTokenLogin(false);
        userRole = roleRepository.save(userRole);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        testUser = new UserInfo();
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("Test@123"));
        testUser.setCpf("12345678900");
        testUser.setEnabled(true);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);

        anotherUser = new UserInfo();
        anotherUser.setUsername("another@example.com");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("Test@456"));
        anotherUser.setCpf("98765432100");
        anotherUser.setEnabled(true);
        anotherUser.setRoles(roles);
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    void findActiveByUserId_WithActiveToken_ReturnsToken() {
        LocalDateTime now = LocalDateTime.now();
        UserToken activeToken = createUserToken(testUser, "ABC12", now.minusMinutes(2), now.plusMinutes(3), true);
        userTokenRepository.save(activeToken);

        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        assertTrue(result.isPresent());
        assertEquals(activeToken.getToken(), result.get().getToken());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    void findActiveByUserId_WithExpiredToken_ReturnsEmpty() {
        LocalDateTime now = LocalDateTime.now();
        UserToken expiredToken = createUserToken(testUser, "XYZ99", now.minusMinutes(10), now.minusMinutes(5), true);
        userTokenRepository.save(expiredToken);

        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        assertFalse(result.isPresent());
    }

    @Test
    void findActiveByUserId_WithInvalidToken_ReturnsEmpty() {
        LocalDateTime now = LocalDateTime.now();
        UserToken invalidToken = createUserToken(testUser, "DEF45", now.minusMinutes(2), now.plusMinutes(3), false);
        userTokenRepository.save(invalidToken);

        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        assertFalse(result.isPresent());
    }

    @Test
    void findActiveByUserId_WithSingleValidToken_ReturnsToken() {
        LocalDateTime now = LocalDateTime.now();
        UserToken validToken = createUserToken(testUser, "VALID1", now.minusMinutes(2), now.plusMinutes(3), true);
        UserToken invalidToken = createUserToken(testUser, "INVLD1", now.minusMinutes(5), now.plusMinutes(3), false);

        userTokenRepository.save(invalidToken);
        userTokenRepository.save(validToken);

        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        assertTrue(result.isPresent());
        assertEquals("VALID1", result.get().getToken());
        assertTrue(result.get().getIsValid());
    }

    @Test
    void findActiveByUserId_WithNonExistentUser_ReturnsEmpty() {
        LocalDateTime now = LocalDateTime.now();
        Long nonExistentUserId = 999L;

        Optional<UserToken> result = userTokenRepository.findActiveByUserId(nonExistentUserId, now);

        assertFalse(result.isPresent());
    }

    @Test
    void findActiveByUserId_WithDifferentUsers_ReturnsCorrectUserToken() {
        LocalDateTime now = LocalDateTime.now();
        UserToken tokenUser1 = createUserToken(testUser, "USER1", now.minusMinutes(2), now.plusMinutes(3), true);
        UserToken tokenUser2 = createUserToken(anotherUser, "USER2", now.minusMinutes(2), now.plusMinutes(3), true);

        userTokenRepository.save(tokenUser1);
        userTokenRepository.save(tokenUser2);

        Optional<UserToken> resultUser1 = userTokenRepository.findActiveByUserId(testUser.getId(), now);
        Optional<UserToken> resultUser2 = userTokenRepository.findActiveByUserId(anotherUser.getId(), now);

        assertTrue(resultUser1.isPresent());
        assertTrue(resultUser2.isPresent());
        assertEquals("USER1", resultUser1.get().getToken());
        assertEquals("USER2", resultUser2.get().getToken());
        assertEquals(testUser.getId(), resultUser1.get().getUser().getId());
        assertEquals(anotherUser.getId(), resultUser2.get().getUser().getId());
    }

    @Test
    void invalidateAllByUserId_WithExistingTokens_InvalidatesAll() {
        LocalDateTime now = LocalDateTime.now();
        UserToken token1 = createUserToken(testUser, "TOK01", now.minusMinutes(5), now.plusMinutes(3), true);
        UserToken token2 = createUserToken(testUser, "TOK02", now.minusMinutes(3), now.plusMinutes(3), true);
        UserToken token3 = createUserToken(testUser, "TOK03", now.minusMinutes(1), now.plusMinutes(3), true);

        userTokenRepository.save(token1);
        userTokenRepository.save(token2);
        userTokenRepository.save(token3);

        var allTokens = userTokenRepository.findAll();
        long validTokensCount = allTokens.stream()
                .filter(t -> t.getUser().getId().equals(testUser.getId()) && t.getIsValid())
                .count();
        assertEquals(3, validTokensCount);

        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        assertEquals(3, invalidatedCount);

        Optional<UserToken> activeToken = userTokenRepository.findActiveByUserId(testUser.getId(), now);
        assertFalse(activeToken.isPresent());
    }

    @Test
    void invalidateAllByUserId_WithNoTokens_ReturnsZero() {
        Long userId = testUser.getId();

        int invalidatedCount = userTokenRepository.invalidateAllByUserId(userId);

        assertEquals(0, invalidatedCount);
    }

    @Test
    void invalidateAllByUserId_WithAlreadyInvalidTokens_ReturnsZero() {
        LocalDateTime now = LocalDateTime.now();
        UserToken invalidToken = createUserToken(testUser, "INVALID", now.minusMinutes(2), now.plusMinutes(3), false);
        userTokenRepository.save(invalidToken);

        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        assertEquals(0, invalidatedCount);
    }

    @Test
    void invalidateAllByUserId_WithMixedTokens_InvalidatesOnlyValid() {
        LocalDateTime now = LocalDateTime.now();
        UserToken validToken = createUserToken(testUser, "VALID", now.minusMinutes(2), now.plusMinutes(3), true);
        UserToken invalidToken = createUserToken(testUser, "INVALID", now.minusMinutes(2), now.plusMinutes(3), false);

        userTokenRepository.save(validToken);
        userTokenRepository.save(invalidToken);

        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        assertEquals(1, invalidatedCount);
    }

    @Test
    void invalidateAllByUserId_DoesNotAffectOtherUsers() {
        LocalDateTime now = LocalDateTime.now();
        UserToken tokenUser1 = createUserToken(testUser, "USER1", now.minusMinutes(2), now.plusMinutes(3), true);
        UserToken tokenUser2 = createUserToken(anotherUser, "USER2", now.minusMinutes(2), now.plusMinutes(3), true);

        userTokenRepository.save(tokenUser1);
        userTokenRepository.save(tokenUser2);

        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        assertEquals(1, invalidatedCount);

        Optional<UserToken> otherUserToken = userTokenRepository.findActiveByUserId(anotherUser.getId(), now);
        assertTrue(otherUserToken.isPresent());
        assertEquals("USER2", otherUserToken.get().getToken());
    }

    @Test
    void findLatestByUserIdAndToken_WithExistingToken_ReturnsToken() {
        LocalDateTime now = LocalDateTime.now();
        UserToken token = createUserToken(testUser, "SRCH01", now.minusMinutes(2), now.plusMinutes(3), true);
        userTokenRepository.save(token);

        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "SRCH01");

        assertTrue(result.isPresent());
        assertEquals("SRCH01", result.get().getToken());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    void findLatestByUserIdAndToken_WithNonExistentToken_ReturnsEmpty() {
        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "NONE01");

        assertFalse(result.isPresent());
    }

    @Test
    void findLatestByUserIdAndToken_WithSingleToken_ReturnsToken() {
        LocalDateTime now = LocalDateTime.now();
        UserToken token = createUserToken(testUser, "FIND01", now.minusMinutes(2), now.plusMinutes(3), true);
        userTokenRepository.save(token);

        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "FIND01");

        assertTrue(result.isPresent());
        assertEquals("FIND01", result.get().getToken());
        assertEquals(token.getId(), result.get().getId());
    }

    @Test
    void findLatestByUserIdAndToken_WithWrongUser_ReturnsEmpty() {
        LocalDateTime now = LocalDateTime.now();
        UserToken token = createUserToken(testUser, "WRONG1", now.minusMinutes(2), now.plusMinutes(3), true);
        userTokenRepository.save(token);

        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(anotherUser.getId(), "WRONG1");

        assertFalse(result.isPresent());
    }

    @Test
    void findLatestByUserIdAndToken_ReturnsTokenRegardlessOfValidity() {
        LocalDateTime now = LocalDateTime.now();
        UserToken invalidToken = createUserToken(testUser, "INVLD1", now.minusMinutes(2), now.plusMinutes(3), false);
        userTokenRepository.save(invalidToken);

        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "INVLD1");

        assertTrue(result.isPresent());
        assertEquals("INVLD1", result.get().getToken());
        assertFalse(result.get().getIsValid());
    }

    @Test
    void findLatestByUserIdAndToken_ReturnsTokenRegardlessOfExpiration() {
        LocalDateTime now = LocalDateTime.now();
        UserToken expiredToken = createUserToken(testUser, "EXPRD1", now.minusMinutes(10), now.minusMinutes(5), true);
        userTokenRepository.save(expiredToken);

        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "EXPRD1");

        assertTrue(result.isPresent());
        assertEquals("EXPRD1", result.get().getToken());
        assertTrue(result.get().getExpiresAt().isBefore(now));
    }

    @Test
    void save_UserToken_PersistsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        UserToken token = createUserToken(testUser, "SAVE01", now, now.plusMinutes(5), true);

        UserToken savedToken = userTokenRepository.save(token);

        assertNotNull(savedToken.getId());
        assertEquals("SAVE01", savedToken.getToken());
        assertEquals(testUser.getId(), savedToken.getUser().getId());
        assertEquals(now.withNano(0), savedToken.getCreatedAt().withNano(0));
        assertTrue(savedToken.getIsValid());
    }

    @Test
    void save_UserTokenWithPrePersist_SetsDefaultValues() {
        UserToken token = new UserToken();
        token.setUser(testUser);
        token.setToken("PREP01");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        UserToken savedToken = userTokenRepository.save(token);

        assertNotNull(savedToken.getId());
        assertNotNull(savedToken.getCreatedAt());
        assertTrue(savedToken.getIsValid());
    }

    @Test
    void countActiveTokensByUserId_WithMultipleTokens_ReturnsCorrectCount() {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 3; i++) {
            UserToken token = createUserToken(testUser, "TOK" + String.format("%02d", i),
                    now.minusMinutes(10 - i), now.plusMinutes(5), true);
            userTokenRepository.save(token);
        }

        long activeCount = userTokenRepository.countActiveTokensByUserId(testUser.getId(), now);

        assertEquals(3, activeCount);
    }

    @Test
    void findAllActiveByUserId_WithMultipleTokens_ReturnsAllActive() {
        LocalDateTime now = LocalDateTime.now();
        UserToken validToken1 = createUserToken(testUser, "VAL01", now.minusMinutes(5), now.plusMinutes(5), true);
        UserToken validToken2 = createUserToken(testUser, "VAL02", now.minusMinutes(3), now.plusMinutes(5), true);
        UserToken invalidToken = createUserToken(testUser, "INV01", now.minusMinutes(2), now.plusMinutes(5), false);
        UserToken expiredToken = createUserToken(testUser, "EXP01", now.minusMinutes(10), now.minusMinutes(2), true);

        userTokenRepository.save(validToken1);
        userTokenRepository.save(validToken2);
        userTokenRepository.save(invalidToken);
        userTokenRepository.save(expiredToken);

        var activeTokens = userTokenRepository.findAllActiveByUserId(testUser.getId(), now);

        assertEquals(2, activeTokens.size());
        assertTrue(activeTokens.stream().anyMatch(t -> "VAL01".equals(t.getToken())));
        assertTrue(activeTokens.stream().anyMatch(t -> "VAL02".equals(t.getToken())));
    }

    @Test
    void findLatestByUserIdAndToken_WithDifferentUsers_ReturnsCorrectToken() {
        LocalDateTime now = LocalDateTime.now();
        String tokenValue = "SAME01";

        UserToken tokenUser1 = createUserToken(testUser, tokenValue, now.minusMinutes(5), now.plusMinutes(5), true);
        UserToken tokenUser2 = createUserToken(anotherUser, tokenValue, now.minusMinutes(3), now.plusMinutes(5), true);

        userTokenRepository.save(tokenUser1);
        userTokenRepository.save(tokenUser2);

        Optional<UserToken> resultUser1 = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), tokenValue);
        Optional<UserToken> resultUser2 = userTokenRepository.findLatestByUserIdAndToken(anotherUser.getId(), tokenValue);

        assertTrue(resultUser1.isPresent());
        assertTrue(resultUser2.isPresent());
        assertEquals(tokenUser1.getId(), resultUser1.get().getId());
        assertEquals(tokenUser2.getId(), resultUser2.get().getId());
        assertEquals(testUser.getId(), resultUser1.get().getUser().getId());
        assertEquals(anotherUser.getId(), resultUser2.get().getUser().getId());
    }

    private UserToken createUserToken(UserInfo user, String token, LocalDateTime createdAt,
                                      LocalDateTime expiresAt, boolean isValid) {
        return UserToken.builder()
                .user(user)
                .token(token)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .isValid(isValid)
                .build();
    }
}
