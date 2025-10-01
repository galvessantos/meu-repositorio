package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.entity.UserToken;
import com.montreal.oauth.domain.repository.IUserTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTokenServiceUnitTest {

    @Mock
    private IUserTokenRepository userTokenRepository;

    @InjectMocks
    private UserTokenService userTokenService;

    private UserInfo testUser;
    private UserToken validToken;
    private UserToken expiredToken;
    private UserToken invalidToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userTokenService, "tokenLength", 5);
        ReflectionTestUtils.setField(userTokenService, "tokenTtlMinutes", 5);

        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");

        LocalDateTime now = LocalDateTime.now();

        validToken = UserToken.builder()
                .id(1L)
                .user(testUser)
                .token("ABC12")
                .createdAt(now.minusMinutes(2))
                .expiresAt(now.plusMinutes(3))
                .isValid(true)
                .build();

        expiredToken = UserToken.builder()
                .id(2L)
                .user(testUser)
                .token("XYZ99")
                .createdAt(now.minusMinutes(10))
                .expiresAt(now.minusMinutes(5))
                .isValid(true)
                .build();

        invalidToken = UserToken.builder()
                .id(3L)
                .user(testUser)
                .token("DEF45")
                .createdAt(now.minusMinutes(2))
                .expiresAt(now.plusMinutes(3))
                .isValid(false)
                .build();
    }

    @Test
    void generateRandomToken_WithValidLength_ReturnsTokenOfCorrectLength() {
        int length = 8;
        String token = userTokenService.generateRandomToken(length);
        assertNotNull(token);
        assertEquals(length, token.length());
        assertTrue(token.matches("[A-Za-z0-9]+"));
    }

    @Test
    void generateRandomToken_WithZeroLength_ReturnsEmptyString() {
        int length = 0;
        String token = userTokenService.generateRandomToken(length);
        assertNotNull(token);
        assertEquals(0, token.length());
    }

    @Test
    void generateRandomToken_MultipleCalls_ReturnsDifferentTokens() {
        int length = 10;
        String token1 = userTokenService.generateRandomToken(length);
        String token2 = userTokenService.generateRandomToken(length);
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void generateAndPersist_ValidUser_InvalidatesOldTokensAndCreatesNew() {
        when(userTokenRepository.invalidateAllByUserId(testUser.getId())).thenReturn(2);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(validToken);

        UserToken result = userTokenService.generateAndPersist(testUser);

        assertNotNull(result);
        assertEquals(validToken, result);
        verify(userTokenRepository).invalidateAllByUserId(testUser.getId());
        verify(userTokenRepository).save(any(UserToken.class));
    }

    @Test
    void generateAndPersist_ValidUser_CreatesTokenWithCorrectProperties() {
        when(userTokenRepository.invalidateAllByUserId(testUser.getId())).thenReturn(0);
        when(userTokenRepository.save(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken token = invocation.getArgument(0);
            assertNotNull(token.getToken());
            assertEquals(5, token.getToken().length());
            assertEquals(testUser, token.getUser());
            assertNotNull(token.getCreatedAt());
            assertNotNull(token.getExpiresAt());
            assertTrue(token.getIsValid());
            assertTrue(token.getExpiresAt().isAfter(token.getCreatedAt()));
            return token;
        });

        userTokenService.generateAndPersist(testUser);
        verify(userTokenRepository).save(any(UserToken.class));
    }

    @Test
    void getActiveToken_SingleActiveToken_ReturnsToken() {
        Long userId = 1L;
        when(userTokenRepository.countActiveTokensByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(1L);
        when(userTokenRepository.findActiveByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(validToken));

        Optional<UserToken> result = userTokenService.getActiveToken(userId);

        assertTrue(result.isPresent());
        assertEquals(validToken, result.get());
        verify(userTokenRepository).countActiveTokensByUserId(eq(userId), any(LocalDateTime.class));
        verify(userTokenRepository).findActiveByUserId(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void getActiveToken_NoActiveToken_ReturnsEmpty() {
        Long userId = 1L;
        when(userTokenRepository.countActiveTokensByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(0L);

        Optional<UserToken> result = userTokenService.getActiveToken(userId);

        assertFalse(result.isPresent());
        verify(userTokenRepository).countActiveTokensByUserId(eq(userId), any(LocalDateTime.class));
        verify(userTokenRepository, never()).findActiveByUserId(any(), any());
    }

    @Test
    void getActiveToken_MultipleActiveTokens_CleansUpAndReturnsLatest() {
        Long userId = 1L;
        UserToken olderToken = UserToken.builder()
                .id(2L)
                .user(testUser)
                .token("OLD01")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isValid(true)
                .build();

        when(userTokenRepository.countActiveTokensByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(2L);
        when(userTokenRepository.findAllActiveByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(olderToken, validToken));
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(olderToken);

        Optional<UserToken> result = userTokenService.getActiveToken(userId);

        assertTrue(result.isPresent());
        assertEquals(validToken, result.get());
        verify(userTokenRepository).save(olderToken);
        assertFalse(olderToken.getIsValid());
        verify(userTokenRepository).countActiveTokensByUserId(eq(userId), any(LocalDateTime.class));
        verify(userTokenRepository).findAllActiveByUserId(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void invalidateAll_ValidUserId_CallsRepository() {
        Long userId = 1L;
        when(userTokenRepository.invalidateAllByUserId(userId)).thenReturn(3);
        userTokenService.invalidateAll(userId);
        verify(userTokenRepository).invalidateAllByUserId(userId);
    }

    @Test
    void validateToken_ValidTokenAndUser_ReturnsOKAndInvalidatesToken() {
        Long userId = 1L;
        String token = "ABC12";
        when(userTokenRepository.findLatestByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(validToken));
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(validToken);

        UserTokenService.ValidationResult result = userTokenService.validateToken(userId, token);

        assertEquals(UserTokenService.ValidationResult.OK, result);
        verify(userTokenRepository).findLatestByUserIdAndToken(userId, token);
        verify(userTokenRepository).save(any(UserToken.class));
        assertFalse(validToken.getIsValid());
    }

    @Test
    void validateToken_NonExistentToken_ReturnsInvalid() {
        Long userId = 1L;
        String token = "NONEXISTENT";
        when(userTokenRepository.findLatestByUserIdAndToken(userId, token))
                .thenReturn(Optional.empty());

        UserTokenService.ValidationResult result = userTokenService.validateToken(userId, token);

        assertEquals(UserTokenService.ValidationResult.INVALID, result);
        verify(userTokenRepository).findLatestByUserIdAndToken(userId, token);
        verify(userTokenRepository, never()).save(any(UserToken.class));
    }

    @Test
    void validateToken_InvalidToken_ReturnsInvalid() {
        Long userId = 1L;
        String token = "DEF45";
        when(userTokenRepository.findLatestByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(invalidToken));

        UserTokenService.ValidationResult result = userTokenService.validateToken(userId, token);

        assertEquals(UserTokenService.ValidationResult.INVALID, result);
        verify(userTokenRepository).findLatestByUserIdAndToken(userId, token);
        verify(userTokenRepository, never()).save(any(UserToken.class));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsExpired() {
        Long userId = 1L;
        String token = "XYZ99";
        when(userTokenRepository.findLatestByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(expiredToken));

        UserTokenService.ValidationResult result = userTokenService.validateToken(userId, token);

        assertEquals(UserTokenService.ValidationResult.EXPIRED, result);
        verify(userTokenRepository).findLatestByUserIdAndToken(userId, token);
        verify(userTokenRepository, never()).save(any(UserToken.class));
    }

    @Test
    void validateToken_TokenWithNullIsValid_ReturnsInvalid() {
        Long userId = 1L;
        String token = "NULL_VALID";
        UserToken tokenWithNullValid = UserToken.builder()
                .id(4L)
                .user(testUser)
                .token(token)
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .isValid(null)
                .build();

        when(userTokenRepository.findLatestByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(tokenWithNullValid));

        UserTokenService.ValidationResult result = userTokenService.validateToken(userId, token);

        assertEquals(UserTokenService.ValidationResult.INVALID, result);
        verify(userTokenRepository).findLatestByUserIdAndToken(userId, token);
        verify(userTokenRepository, never()).save(any(UserToken.class));
    }

    @Test
    void validateToken_TokenWithFalseIsValid_ReturnsInvalid() {
        Long userId = 1L;
        String token = "FALSE_VALID";
        UserToken tokenWithFalseValid = UserToken.builder()
                .id(5L)
                .user(testUser)
                .token(token)
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .isValid(false)
                .build();

        when(userTokenRepository.findLatestByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(tokenWithFalseValid));

        UserTokenService.ValidationResult result = userTokenService.validateToken(userId, token);

        assertEquals(UserTokenService.ValidationResult.INVALID, result);
        verify(userTokenRepository).findLatestByUserIdAndToken(userId, token);
        verify(userTokenRepository, never()).save(any(UserToken.class));
    }

    @Test
    void validateToken_ValidTokenButExpiredByOneSecond_ReturnsExpired() {
        Long userId = 1L;
        String token = "BARELY_EXPIRED";
        LocalDateTime now = LocalDateTime.now();
        UserToken barelyExpiredToken = UserToken.builder()
                .id(6L)
                .user(testUser)
                .token(token)
                .createdAt(now.minusMinutes(10))
                .expiresAt(now.minusSeconds(1))
                .isValid(true)
                .build();

        when(userTokenRepository.findLatestByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(barelyExpiredToken));

        UserTokenService.ValidationResult result = userTokenService.validateToken(userId, token);

        assertEquals(UserTokenService.ValidationResult.EXPIRED, result);
        verify(userTokenRepository).findLatestByUserIdAndToken(userId, token);
        verify(userTokenRepository, never()).save(any(UserToken.class));
    }
}
