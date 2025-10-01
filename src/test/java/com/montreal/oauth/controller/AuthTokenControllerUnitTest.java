package com.montreal.oauth.controller;

import com.montreal.oauth.domain.dto.AuthResponseDTO;
import com.montreal.oauth.domain.dto.JwtResponseDTO;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.entity.UserToken;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.domain.service.JwtService;
import com.montreal.oauth.domain.service.RefreshTokenService;
import com.montreal.oauth.domain.service.UserService;
import com.montreal.oauth.domain.service.UserTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private UserTokenService userTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthTokenController authTokenController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserInfo testUser;
    private UserToken testToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authTokenController).build();
        objectMapper = new ObjectMapper();

        // Configurar usuário de teste
        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setCpf("12345678901");
        testUser.setPhone("+5511999999999");
        testUser.setCompanyId("123");
        testUser.setEnabled(true);
        testUser.setFirstLoginCompleted(false);
        testUser.setPasswordChangedByUser(true);
        testUser.setCreatedByAdmin(false);
        testUser.setReset(false);

        // Configurar roles
        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName(RoleEnum.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        // Configurar token de teste
        LocalDateTime now = LocalDateTime.now();
        testToken = UserToken.builder()
                .id(1L)
                .user(testUser)
                .token("ABC12")
                .createdAt(now)
                .expiresAt(now.plusMinutes(5))
                .isValid(true)
                .build();
    }

    @Test
    void generateToken_ValidUser_ReturnsSuccessResponse() {
        // Given
        AuthTokenController.GenerateTokenRequest request = new AuthTokenController.GenerateTokenRequest();
        request.setUserId(1L);

        when(userService.findById(1L)).thenReturn(testUser);
        when(userTokenService.generateAndPersist(testUser)).thenReturn(testToken);

        // When
        ResponseEntity<AuthTokenController.GenerateTokenResponse> response = 
                authTokenController.generateToken(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ABC12", response.getBody().getToken());
        assertEquals(testToken.getExpiresAt().toString(), response.getBody().getExpiresAt());

        verify(userService).findById(1L);
        verify(userTokenService).generateAndPersist(testUser);
    }

    @Test
    void generateToken_InvalidUser_ThrowsException() {
        // Given
        AuthTokenController.GenerateTokenRequest request = new AuthTokenController.GenerateTokenRequest();
        request.setUserId(999L);

        when(userService.findById(999L)).thenThrow(new RuntimeException("User not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authTokenController.generateToken(request);
        });

        verify(userService).findById(999L);
        verify(userTokenService, never()).generateAndPersist(any());
    }

    @Test
    void validateToken_ValidToken_ReturnsJwtResponse() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken("ABC12");

        when(userTokenService.validateToken(1L, "ABC12")).thenReturn(UserTokenService.ValidationResult.OK);
        when(userService.findById(1L)).thenReturn(testUser);
        when(userService.save(any(UserInfo.class))).thenReturn(testUser); // Mock para save do primeiro login
        when(jwtService.GenerateToken("testuser@example.com")).thenReturn("jwt-access-token");
        when(refreshTokenService.getTokenByUserId(1L)).thenReturn("");
        when(refreshTokenService.createRefreshToken("testuser@example.com"))
                .thenReturn(createMockRefreshToken("refresh-token"));

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponseDTO);
        
        JwtResponseDTO jwtResponse = (JwtResponseDTO) response.getBody();
        assertEquals("jwt-access-token", jwtResponse.getAccessToken());
        assertEquals("refresh-token", jwtResponse.getToken());
        assertNotNull(jwtResponse.getUserDetails());

        verify(userTokenService).validateToken(1L, "ABC12");
        verify(userService, times(1)).findById(1L); // Apenas uma chamada no controller
        verify(jwtService).GenerateToken("testuser@example.com");
        verify(refreshTokenService).getTokenByUserId(1L);
        verify(refreshTokenService).createRefreshToken("testuser@example.com");
    }

    @Test
    void validateToken_ValidTokenWithExistingRefreshToken_ReturnsJwtResponseWithExistingRefresh() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken("ABC12");

        when(userTokenService.validateToken(1L, "ABC12")).thenReturn(UserTokenService.ValidationResult.OK);
        when(userService.findById(1L)).thenReturn(testUser);
        when(userService.save(any(UserInfo.class))).thenReturn(testUser); // Mock para save do primeiro login
        when(jwtService.GenerateToken("testuser@example.com")).thenReturn("jwt-access-token");
        when(refreshTokenService.getTokenByUserId(1L)).thenReturn("existing-refresh-token");

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponseDTO);
        
        JwtResponseDTO jwtResponse = (JwtResponseDTO) response.getBody();
        assertEquals("jwt-access-token", jwtResponse.getAccessToken());
        assertEquals("existing-refresh-token", jwtResponse.getToken());

        verify(refreshTokenService).getTokenByUserId(1L);
        verify(refreshTokenService, never()).createRefreshToken(anyString());
    }

    @Test
    void validateToken_FirstLoginNotCompleted_CompletesFirstLogin() {
        // Given
        testUser.setFirstLoginCompleted(false);
        
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken("ABC12");

        when(userTokenService.validateToken(1L, "ABC12")).thenReturn(UserTokenService.ValidationResult.OK);
        when(userService.findById(1L)).thenReturn(testUser);
        when(userService.save(any(UserInfo.class))).thenReturn(testUser);
        when(jwtService.GenerateToken("testuser@example.com")).thenReturn("jwt-access-token");
        when(refreshTokenService.getTokenByUserId(1L)).thenReturn("");
        when(refreshTokenService.createRefreshToken("testuser@example.com"))
                .thenReturn(createMockRefreshToken("refresh-token"));

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(testUser.isFirstLoginCompleted());
        verify(userService).save(testUser);
    }

    @Test
    void validateToken_InvalidToken_ReturnsUnauthorized() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken("INVALID");

        when(userTokenService.validateToken(1L, "INVALID")).thenReturn(UserTokenService.ValidationResult.INVALID);

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthTokenController.ValidateTokenError);
        
        AuthTokenController.ValidateTokenError error = (AuthTokenController.ValidateTokenError) response.getBody();
        assertEquals("TOKEN_INVALIDO", error.getError());

        verify(userTokenService).validateToken(1L, "INVALID");
        verify(userService, never()).findById(anyLong());
        verify(jwtService, never()).GenerateToken(anyString());
    }

    @Test
    void validateToken_ExpiredToken_ReturnsUnauthorized() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken("EXPIRED");

        when(userTokenService.validateToken(1L, "EXPIRED")).thenReturn(UserTokenService.ValidationResult.EXPIRED);

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthTokenController.ValidateTokenError);
        
        AuthTokenController.ValidateTokenError error = (AuthTokenController.ValidateTokenError) response.getBody();
        assertEquals("TOKEN_EXPIRADO", error.getError());

        verify(userTokenService).validateToken(1L, "EXPIRED");
        verify(userService, never()).findById(anyLong());
        verify(jwtService, never()).GenerateToken(anyString());
    }

    @Test
    void validateToken_ExceptionDuringTokenGeneration_ReturnsInternalServerError() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken("ABC12");

        when(userTokenService.validateToken(1L, "ABC12")).thenReturn(UserTokenService.ValidationResult.OK);
        when(userService.findById(1L)).thenReturn(testUser);
        when(userService.save(any(UserInfo.class))).thenReturn(testUser); // Mock para save do primeiro login
        when(jwtService.GenerateToken("testuser@example.com")).thenThrow(new RuntimeException("JWT generation failed"));

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthTokenController.ValidateTokenError);
        
        AuthTokenController.ValidateTokenError error = (AuthTokenController.ValidateTokenError) response.getBody();
        assertEquals("ERRO_INTERNO", error.getError());

        verify(userTokenService).validateToken(1L, "ABC12");
        verify(userService, times(1)).findById(1L);
        verify(jwtService).GenerateToken("testuser@example.com");
    }

    @Test
    void validateToken_ValidToken_CreatesCorrectUserDetails() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken("ABC12");

        when(userTokenService.validateToken(1L, "ABC12")).thenReturn(UserTokenService.ValidationResult.OK);
        when(userService.findById(1L)).thenReturn(testUser);
        when(userService.save(any(UserInfo.class))).thenReturn(testUser); // Mock para save do primeiro login
        when(jwtService.GenerateToken("testuser@example.com")).thenReturn("jwt-access-token");
        when(refreshTokenService.getTokenByUserId(1L)).thenReturn("");
        when(refreshTokenService.createRefreshToken("testuser@example.com"))
                .thenReturn(createMockRefreshToken("refresh-token"));

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponseDTO jwtResponse = (JwtResponseDTO) response.getBody();
        
        AuthResponseDTO userDetails = jwtResponse.getUserDetails();
        assertNotNull(userDetails);
        assertNotNull(userDetails.getUser());
        
        // Verificar se os dados do usuário estão corretos
        AuthResponseDTO.UserDetailsDTO user = (AuthResponseDTO.UserDetailsDTO) userDetails.getUser();
        assertEquals(1L, user.getId());
        assertEquals("testuser@example.com", user.getUsername());
        assertEquals("testuser@example.com", user.getEmail());
        assertEquals("12345678901", user.getCpf());
        assertEquals("+5511999999999", user.getPhone());
        assertEquals("123", user.getCompanyId());
        assertTrue(user.isEnabled());
        assertFalse(user.isCreatedByAdmin());
        assertTrue(user.isPasswordChangedByUser());
        assertFalse(user.isReset());
        
        List<String> roles = user.getRoles();
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    void validateToken_NullUserIdInRequest_HandledGracefully() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(null);
        request.setToken("ABC12");

        when(userTokenService.validateToken(null, "ABC12")).thenReturn(UserTokenService.ValidationResult.INVALID);

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userTokenService).validateToken(null, "ABC12");
    }

    @Test
    void validateToken_NullTokenInRequest_HandledGracefully() {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(1L);
        request.setToken(null);

        when(userTokenService.validateToken(1L, null)).thenReturn(UserTokenService.ValidationResult.INVALID);

        // When
        ResponseEntity<?> response = authTokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userTokenService).validateToken(1L, null);
    }

    // Método auxiliar para criar mock do RefreshToken
    private com.montreal.oauth.domain.entity.RefreshToken createMockRefreshToken(String token) {
        com.montreal.oauth.domain.entity.RefreshToken refreshToken = 
                new com.montreal.oauth.domain.entity.RefreshToken();
        refreshToken.setToken(token);
        return refreshToken;
    }
}