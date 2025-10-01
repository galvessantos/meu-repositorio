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

        // Criar role de teste
        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);
        userRole.setRequiresTokenFirstLogin(false);
        userRole.setTokenLogin(false);
        userRole = roleRepository.save(userRole);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Criar primeiro usuário de teste
        testUser = new UserInfo();
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("Test@123"));
        testUser.setCpf("12345678900");
        testUser.setEnabled(true);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);

        // Criar segundo usuário de teste
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
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken activeToken = createUserToken(testUser, "ABC12", now.minusMinutes(2), now.plusMinutes(3), true);
        userTokenRepository.save(activeToken);

        // When
        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        // Then
        assertTrue(result.isPresent());
        assertEquals(activeToken.getToken(), result.get().getToken());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    void findActiveByUserId_WithExpiredToken_ReturnsEmpty() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken expiredToken = createUserToken(testUser, "XYZ99", now.minusMinutes(10), now.minusMinutes(5), true);
        userTokenRepository.save(expiredToken);

        // When
        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findActiveByUserId_WithInvalidToken_ReturnsEmpty() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken invalidToken = createUserToken(testUser, "DEF45", now.minusMinutes(2), now.plusMinutes(3), false);
        userTokenRepository.save(invalidToken);

        // When
        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findActiveByUserId_WithMultipleTokens_ReturnsLatest() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken olderToken = createUserToken(testUser, "OLD01", now.minusMinutes(10), now.plusMinutes(3), true);
        UserToken newerToken = createUserToken(testUser, "NEW01", now.minusMinutes(2), now.plusMinutes(3), true);
        
        userTokenRepository.save(olderToken);
        userTokenRepository.save(newerToken);

        // When
        Optional<UserToken> result = userTokenRepository.findActiveByUserId(testUser.getId(), now);

        // Then
        assertTrue(result.isPresent());
        assertEquals("NEW01", result.get().getToken());
    }

    @Test
    void findActiveByUserId_WithNonExistentUser_ReturnsEmpty() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Long nonExistentUserId = 999L;

        // When
        Optional<UserToken> result = userTokenRepository.findActiveByUserId(nonExistentUserId, now);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findActiveByUserId_WithDifferentUsers_ReturnsCorrectUserToken() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken tokenUser1 = createUserToken(testUser, "USER1", now.minusMinutes(2), now.plusMinutes(3), true);
        UserToken tokenUser2 = createUserToken(anotherUser, "USER2", now.minusMinutes(2), now.plusMinutes(3), true);
        
        userTokenRepository.save(tokenUser1);
        userTokenRepository.save(tokenUser2);

        // When
        Optional<UserToken> resultUser1 = userTokenRepository.findActiveByUserId(testUser.getId(), now);
        Optional<UserToken> resultUser2 = userTokenRepository.findActiveByUserId(anotherUser.getId(), now);

        // Then
        assertTrue(resultUser1.isPresent());
        assertTrue(resultUser2.isPresent());
        assertEquals("USER1", resultUser1.get().getToken());
        assertEquals("USER2", resultUser2.get().getToken());
        assertEquals(testUser.getId(), resultUser1.get().getUser().getId());
        assertEquals(anotherUser.getId(), resultUser2.get().getUser().getId());
    }

    @Test
    void invalidateAllByUserId_WithExistingTokens_InvalidatesAll() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken token1 = createUserToken(testUser, "TOK01", now.minusMinutes(5), now.plusMinutes(3), true);
        UserToken token2 = createUserToken(testUser, "TOK02", now.minusMinutes(3), now.plusMinutes(3), true);
        UserToken token3 = createUserToken(testUser, "TOK03", now.minusMinutes(1), now.plusMinutes(3), true);
        
        userTokenRepository.save(token1);
        userTokenRepository.save(token2);
        userTokenRepository.save(token3);

        // When
        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        // Then
        assertEquals(3, invalidatedCount);
        
        // Verificar se todos os tokens foram invalidados
        Optional<UserToken> activeToken = userTokenRepository.findActiveByUserId(testUser.getId(), now);
        assertFalse(activeToken.isPresent());
    }

    @Test
    void invalidateAllByUserId_WithNoTokens_ReturnsZero() {
        // Given
        Long userId = testUser.getId();

        // When
        int invalidatedCount = userTokenRepository.invalidateAllByUserId(userId);

        // Then
        assertEquals(0, invalidatedCount);
    }

    @Test
    void invalidateAllByUserId_WithAlreadyInvalidTokens_ReturnsZero() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken invalidToken = createUserToken(testUser, "INVALID", now.minusMinutes(2), now.plusMinutes(3), false);
        userTokenRepository.save(invalidToken);

        // When
        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        // Then
        assertEquals(0, invalidatedCount);
    }

    @Test
    void invalidateAllByUserId_WithMixedTokens_InvalidatesOnlyValid() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken validToken = createUserToken(testUser, "VALID", now.minusMinutes(2), now.plusMinutes(3), true);
        UserToken invalidToken = createUserToken(testUser, "INVALID", now.minusMinutes(2), now.plusMinutes(3), false);
        
        userTokenRepository.save(validToken);
        userTokenRepository.save(invalidToken);

        // When
        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        // Then
        assertEquals(1, invalidatedCount);
    }

    @Test
    void invalidateAllByUserId_DoesNotAffectOtherUsers() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken tokenUser1 = createUserToken(testUser, "USER1", now.minusMinutes(2), now.plusMinutes(3), true);
        UserToken tokenUser2 = createUserToken(anotherUser, "USER2", now.minusMinutes(2), now.plusMinutes(3), true);
        
        userTokenRepository.save(tokenUser1);
        userTokenRepository.save(tokenUser2);

        // When
        int invalidatedCount = userTokenRepository.invalidateAllByUserId(testUser.getId());

        // Then
        assertEquals(1, invalidatedCount);
        
        // Verificar se o token do outro usuário ainda está ativo
        Optional<UserToken> otherUserToken = userTokenRepository.findActiveByUserId(anotherUser.getId(), now);
        assertTrue(otherUserToken.isPresent());
        assertEquals("USER2", otherUserToken.get().getToken());
    }

    @Test
    void findLatestByUserIdAndToken_WithExistingToken_ReturnsToken() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken token = createUserToken(testUser, "SEARCH", now.minusMinutes(2), now.plusMinutes(3), true);
        userTokenRepository.save(token);

        // When
        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "SEARCH");

        // Then
        assertTrue(result.isPresent());
        assertEquals("SEARCH", result.get().getToken());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    void findLatestByUserIdAndToken_WithNonExistentToken_ReturnsEmpty() {
        // Given
        // When
        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "NONEXISTENT");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findLatestByUserIdAndToken_WithMultipleSameTokens_ReturnsLatest() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken olderToken = createUserToken(testUser, "SAME", now.minusMinutes(10), now.plusMinutes(3), true);
        UserToken newerToken = createUserToken(testUser, "SAME", now.minusMinutes(2), now.plusMinutes(3), true);
        
        userTokenRepository.save(olderToken);
        userTokenRepository.save(newerToken);

        // When
        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "SAME");

        // Then
        assertTrue(result.isPresent());
        assertEquals(newerToken.getId(), result.get().getId());
        assertTrue(result.get().getCreatedAt().isAfter(olderToken.getCreatedAt()));
    }

    @Test
    void findLatestByUserIdAndToken_WithWrongUser_ReturnsEmpty() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken token = createUserToken(testUser, "WRONG_USER", now.minusMinutes(2), now.plusMinutes(3), true);
        userTokenRepository.save(token);

        // When
        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(anotherUser.getId(), "WRONG_USER");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findLatestByUserIdAndToken_ReturnsTokenRegardlessOfValidity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken invalidToken = createUserToken(testUser, "INVALID_BUT_FOUND", now.minusMinutes(2), now.plusMinutes(3), false);
        userTokenRepository.save(invalidToken);

        // When
        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "INVALID_BUT_FOUND");

        // Then
        assertTrue(result.isPresent());
        assertEquals("INVALID_BUT_FOUND", result.get().getToken());
        assertFalse(result.get().getIsValid());
    }

    @Test
    void findLatestByUserIdAndToken_ReturnsTokenRegardlessOfExpiration() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken expiredToken = createUserToken(testUser, "EXPIRED_BUT_FOUND", now.minusMinutes(10), now.minusMinutes(5), true);
        userTokenRepository.save(expiredToken);

        // When
        Optional<UserToken> result = userTokenRepository.findLatestByUserIdAndToken(testUser.getId(), "EXPIRED_BUT_FOUND");

        // Then
        assertTrue(result.isPresent());
        assertEquals("EXPIRED_BUT_FOUND", result.get().getToken());
        assertTrue(result.get().getExpiresAt().isBefore(now));
    }

    @Test
    void save_UserToken_PersistsCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserToken token = createUserToken(testUser, "SAVE_TEST", now, now.plusMinutes(5), true);

        // When
        UserToken savedToken = userTokenRepository.save(token);

        // Then
        assertNotNull(savedToken.getId());
        assertEquals("SAVE_TEST", savedToken.getToken());
        assertEquals(testUser.getId(), savedToken.getUser().getId());
        assertEquals(now.withNano(0), savedToken.getCreatedAt().withNano(0)); // Ignorar nanossegundos
        assertTrue(savedToken.getIsValid());
    }

    @Test
    void save_UserTokenWithPrePersist_SetsDefaultValues() {
        // Given
        UserToken token = new UserToken();
        token.setUser(testUser);
        token.setToken("PREPERSIST_TEST");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        // Não definir createdAt e isValid para testar @PrePersist

        // When
        UserToken savedToken = userTokenRepository.save(token);

        // Then
        assertNotNull(savedToken.getId());
        assertNotNull(savedToken.getCreatedAt());
        assertTrue(savedToken.getIsValid());
    }

    // Método auxiliar para criar UserToken
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