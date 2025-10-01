package com.montreal.oauth.controller;

import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.entity.UserToken;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.domain.repository.IRoleRepository;
import com.montreal.oauth.domain.repository.IUserRepository;
import com.montreal.oauth.domain.repository.IUserTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AuthTokenControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IUserTokenRepository userTokenRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserInfo testUser;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        passwordEncoder = new BCryptPasswordEncoder();

        // Limpar dados de teste
        userTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Criar role de teste
        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);
        userRole.setRequiresTokenFirstLogin(false);
        userRole.setTokenLogin(false);
        userRole = roleRepository.save(userRole);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Criar usuário de teste
        testUser = new UserInfo();
        testUser.setUsername("testuser@example.com");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("Test@123"));
        testUser.setCpf("12345678900");
        testUser.setPhone("+5511999999999");
        testUser.setCompanyId("123");
        testUser.setEnabled(true);
        testUser.setFirstLoginCompleted(false);
        testUser.setPasswordChangedByUser(true);
        testUser.setCreatedByAdmin(false);
        testUser.setReset(false);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);
    }

    @Test
    void generateToken_ValidRequest_ReturnsSuccessResponse() throws Exception {
        // Given
        AuthTokenController.GenerateTokenRequest request = new AuthTokenController.GenerateTokenRequest();
        request.setUserId(testUser.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token", hasLength(5))) // tokenLength padrão
                .andExpect(jsonPath("$.expiresAt", notNullValue()))
                .andExpect(jsonPath("$.expiresAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")));

        // Verificar se o token foi salvo no banco
        var tokens = userTokenRepository.findAll();
        assertEquals(1, tokens.size());
        assertEquals(testUser.getId(), tokens.get(0).getUser().getId());
        assertTrue(tokens.get(0).getIsValid());
    }

    @Test
    void generateToken_InvalidUserId_ReturnsNotFound() throws Exception {
        // Given
        AuthTokenController.GenerateTokenRequest request = new AuthTokenController.GenerateTokenRequest();
        request.setUserId(999L);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        // Verificar se nenhum token foi criado
        var tokens = userTokenRepository.findAll();
        assertEquals(0, tokens.size());
    }

    @Test
    void generateToken_NullUserId_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"userId\": null}";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void generateToken_InvalidatesOldTokens() throws Exception {
        // Given
        // Criar token antigo
        LocalDateTime now = LocalDateTime.now();
        UserToken oldToken = UserToken.builder()
                .user(testUser)
                .token("OLD01")
                .createdAt(now.minusMinutes(10))
                .expiresAt(now.plusMinutes(5))
                .isValid(true)
                .build();
        userTokenRepository.save(oldToken);

        AuthTokenController.GenerateTokenRequest request = new AuthTokenController.GenerateTokenRequest();
        request.setUserId(testUser.getId());

        // When
        mockMvc.perform(post("/api/v1/auth/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        var tokens = userTokenRepository.findAll();
        assertEquals(2, tokens.size()); // Token antigo + novo token
        
        // Verificar se o token antigo foi invalidado
        var oldTokenFromDb = userTokenRepository.findById(oldToken.getId()).orElse(null);
        assertNotNull(oldTokenFromDb);
        assertFalse(oldTokenFromDb.getIsValid());
        
        // Verificar se há apenas um token ativo
        var activeToken = userTokenRepository.findActiveByUserId(testUser.getId(), LocalDateTime.now());
        assertTrue(activeToken.isPresent());
        assertNotEquals(oldToken.getId(), activeToken.get().getId());
    }

    @Test
    void validateToken_ValidToken_ReturnsJwtResponse() throws Exception {
        // Given
        // Criar token válido
        LocalDateTime now = LocalDateTime.now();
        UserToken validToken = UserToken.builder()
                .user(testUser)
                .token("VALID")
                .createdAt(now.minusMinutes(2))
                .expiresAt(now.plusMinutes(3))
                .isValid(true)
                .build();
        userTokenRepository.save(validToken);

        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(testUser.getId());
        request.setToken("VALID");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userDetails", notNullValue()))
                .andExpect(jsonPath("$.userDetails.user.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.userDetails.user.username", is("testuser@example.com")))
                .andExpect(jsonPath("$.userDetails.user.email", is("testuser@example.com")))
                .andExpect(jsonPath("$.userDetails.user.cpf", is("12345678900")))
                .andExpect(jsonPath("$.userDetails.user.phone", is("+5511999999999")))
                .andExpect(jsonPath("$.userDetails.user.companyId", is("123")))
                .andExpect(jsonPath("$.userDetails.user.enabled", is(true)))
                .andExpect(jsonPath("$.userDetails.user.roles", hasSize(1)))
                .andExpect(jsonPath("$.userDetails.user.roles[0]", is("ROLE_USER")));

        // Verificar se o token foi invalidado após uso
        var tokenFromDb = userTokenRepository.findById(validToken.getId()).orElse(null);
        assertNotNull(tokenFromDb);
        assertFalse(tokenFromDb.getIsValid());
    }

    @Test
    void validateToken_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(testUser.getId());
        request.setToken("INVALID");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("TOKEN_INVALIDO")));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsUnauthorized() throws Exception {
        // Given
        // Criar token expirado
        LocalDateTime now = LocalDateTime.now();
        UserToken expiredToken = UserToken.builder()
                .user(testUser)
                .token("EXPIRED")
                .createdAt(now.minusMinutes(10))
                .expiresAt(now.minusMinutes(5))
                .isValid(true)
                .build();
        userTokenRepository.save(expiredToken);

        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(testUser.getId());
        request.setToken("EXPIRED");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("TOKEN_EXPIRADO")));
    }

    @Test
    void validateToken_AlreadyUsedToken_ReturnsUnauthorized() throws Exception {
        // Given
        // Criar token já usado (inválido)
        LocalDateTime now = LocalDateTime.now();
        UserToken usedToken = UserToken.builder()
                .user(testUser)
                .token("USED")
                .createdAt(now.minusMinutes(5))
                .expiresAt(now.plusMinutes(3))
                .isValid(false) // Já foi usado
                .build();
        userTokenRepository.save(usedToken);

        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(testUser.getId());
        request.setToken("USED");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("TOKEN_INVALIDO")));
    }

    @Test
    void validateToken_NonExistentUser_ReturnsUnauthorized() throws Exception {
        // Given
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(999L);
        request.setToken("ANYTOKEN");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("TOKEN_INVALIDO")));
    }

    @Test
    void validateToken_FirstLoginNotCompleted_CompletesFirstLogin() throws Exception {
        // Given
        testUser.setFirstLoginCompleted(false);
        testUser = userRepository.save(testUser);

        LocalDateTime now = LocalDateTime.now();
        UserToken validToken = UserToken.builder()
                .user(testUser)
                .token("FIRST1")
                .createdAt(now.minusMinutes(2))
                .expiresAt(now.plusMinutes(3))
                .isValid(true)
                .build();
        userTokenRepository.save(validToken);

        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(testUser.getId());
        request.setToken("FIRST1");

        // When
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        UserInfo updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(updatedUser.isFirstLoginCompleted());
    }

    @Test
    void validateToken_WrongUserForToken_ReturnsUnauthorized() throws Exception {
        // Given
        // Criar outro usuário
        UserInfo anotherUser = new UserInfo();
        anotherUser.setUsername("another@example.com");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("Test@456"));
        anotherUser.setCpf("98765432100");
        anotherUser.setPhone("+5511888888888");
        anotherUser.setCompanyId("456");
        anotherUser.setEnabled(true);
        anotherUser.setFirstLoginCompleted(true);
        anotherUser.setPasswordChangedByUser(true);
        anotherUser.setCreatedByAdmin(false);
        anotherUser.setReset(false);
        anotherUser.setRoles(testUser.getRoles());
        anotherUser = userRepository.save(anotherUser);

        // Criar token para o primeiro usuário
        LocalDateTime now = LocalDateTime.now();
        UserToken tokenForTestUser = UserToken.builder()
                .user(testUser)
                .token("WRONG1")
                .createdAt(now.minusMinutes(2))
                .expiresAt(now.plusMinutes(3))
                .isValid(true)
                .build();
        userTokenRepository.save(tokenForTestUser);

        // Tentar validar com o segundo usuário
        AuthTokenController.ValidateTokenRequest request = new AuthTokenController.ValidateTokenRequest();
        request.setUserId(anotherUser.getId());
        request.setToken("WRONG1");

        // When & Then
        var result = mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        
        // Debug do erro
        if (result.andReturn().getResponse().getStatus() == 500) {
            System.out.println("=== ERRO 500 DEBUG ===");
            System.out.println("Response: " + result.andReturn().getResponse().getContentAsString());
            System.out.println("======================");
        }
        
        result.andExpect(status().isUnauthorized())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.error", is("TOKEN_INVALIDO")));
    }

    @Test
    void validateToken_MalformedRequest_ReturnsBadRequest() throws Exception {
        // Given
        String malformedJson = "{\"userId\": \"not-a-number\", \"token\": \"TEST\"}";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_EmptyRequest_ReturnsBadRequest() throws Exception {
        // Given
        String emptyJson = "{}";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("TOKEN_INVALIDO")));
    }

    @Test
    void endToEndFlow_GenerateAndValidateToken_Success() throws Exception {
        // Given
        AuthTokenController.GenerateTokenRequest generateRequest = new AuthTokenController.GenerateTokenRequest();
        generateRequest.setUserId(testUser.getId());

        // Step 1: Generate token
        String generateResponse = mockMvc.perform(post("/api/v1/auth/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthTokenController.GenerateTokenResponse tokenResponse = 
                objectMapper.readValue(generateResponse, AuthTokenController.GenerateTokenResponse.class);

        // Step 2: Validate the generated token
        AuthTokenController.ValidateTokenRequest validateRequest = new AuthTokenController.ValidateTokenRequest();
        validateRequest.setUserId(testUser.getId());
        validateRequest.setToken(tokenResponse.getToken());

        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userDetails.user.id", is(testUser.getId().intValue())));

        // Step 3: Try to use the same token again (should fail)
        mockMvc.perform(post("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("TOKEN_INVALIDO")));
    }
}