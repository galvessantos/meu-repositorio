package com.montreal.oauth.controller;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.montreal.core.domain.dto.CheckUserNameDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.montreal.core.domain.dto.response.MessageResponse;
import com.montreal.core.domain.enumerations.MessageTypeEnum;
import com.montreal.core.domain.exception.NegocioException;
import com.montreal.core.domain.service.EmailService;
import com.montreal.core.domain.service.ValidationService;
import com.montreal.core.exception_handler.ProblemType;
import com.montreal.oauth.domain.dto.AuthRequestDTO;
import com.montreal.oauth.domain.dto.CheckPasswordResetDTO;
import com.montreal.oauth.domain.dto.JwtResponseDTO;
import com.montreal.oauth.domain.dto.RefreshTokenRequestDTO;
import com.montreal.oauth.domain.dto.request.PasswordChangeRequest;
import com.montreal.oauth.domain.dto.request.UserRequest;
import com.montreal.oauth.domain.dto.response.PassRecoveryResponse;
import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.RefreshToken;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.IUserRepository;
import com.montreal.oauth.domain.service.JwtService;
import com.montreal.oauth.domain.service.RefreshTokenService;
import com.montreal.oauth.domain.service.UserService;
import com.montreal.oauth.mapper.IUserMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Gerenciamento de usuários.")
public class UserController {

	public static final String PASSWORD_RECOVERY_TITLE 	= "recuperação de senha";
    public static final String REQUEST_FAILED_MESSAGE 	= "Requisição realizada com falha";
    public static final String EMAIL_VERIFICATION_TITLE = "verificação de email cadastrado";
    public static final String REQUEST_SUCCESS_MESSAGE 	= "Requisição Realizada com Sucesso";

    private final JwtService jwtService;
    private final UserService userService;
    private final EmailService emailService;
    private final ValidationService validation;
    private final IUserRepository iUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    
    @Operation(summary = "Cadastrar usuário")
    @PostMapping("/auth/user")
    public UserResponse saveUser(@RequestBody @Valid UserRequest userRequest) {
    	UserResponse savedUser =  userService.saveUser(userRequest);
    	return savedUser;
    }

    @Operation(summary = "Enviar e-mail de cadastro de usuário pelo ID")
    @GetMapping("/auth/user/{id}/send-email-register")
    public ResponseEntity<String> sendEmailRegister(@PathVariable Long id) {
        log.info("[GET] /auth/user/{}/send-email-register - Iniciando envio de e-mail", id);
        try {
            boolean emailSent = userService.sendEmailRegister(id);
            if (emailSent) {
                log.info("E-mail de cadastro enviado com sucesso para usuário ID: {}", id);
                return ResponseEntity.ok("E-mail enviado com sucesso.");
            } else {
                log.warn("Falha ao enviar e-mail de cadastro para usuário ID: {}", id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Não foi possível enviar o e-mail.");
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar e-mail de cadastro para usuário ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao tentar enviar o e-mail.");
        }
    }

    @Operation(summary = "Obter usuário por ID")
    @GetMapping("/auth/user/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        var userInfo = userService.getUserById(id);
        var UserResponse = IUserMapper.INSTANCE.toResponse(userInfo);
        return ResponseEntity.ok(UserResponse);
    }

    @Operation(summary = "Atualizar usuário")
    @PutMapping("/auth/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest userRequest) {
        UserInfo user = userService.updateUser(id, userRequest);
        UserResponse userResponse = userService.decryptSensitiveFieldsForResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Listar usuários")
    @GetMapping("/auth/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            HttpServletRequest request) {
        try {
            Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
            Page<UserResponse> userResponses = userService.getFilteredUsers(pageable, request);
            return new ResponseEntity<>(userResponses, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erro ao buscar usuários", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Buscar perfis", hidden = true)
    @PostMapping("/auth/profile")
    public ResponseEntity<UserResponse> getUserProfile() {
        UserResponse userResponse = userService.getUser();
        return ResponseEntity.ok().body(userResponse);
    }

    @Operation(summary = "Fazer login", hidden = false)
    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponseDTO> authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO) {
        log.info("Autenticando usuário: {}", authRequestDTO.getUsername());

        try {
            JwtResponseDTO response = userService.authenticate(authRequestDTO);
            return ResponseEntity.ok(response);
        } catch (NegocioException e) {
            log.error("Erro ao autenticar usuário: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    JwtResponseDTO.builder()
                            .accessToken(null)
                            .token(null)
                            .userDetails(null)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erro inesperado ao autenticar usuário: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    JwtResponseDTO.builder()
                            .accessToken(null)
                            .token(null)
                            .userDetails(null)
                            .build()
            );
        }
    }

    @Operation(summary = "Atualizar token", hidden = true)
    @PostMapping("/auth/refresh-token")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.GenerateToken(userInfo.getUsername());
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDTO.getToken()).build();
                }).orElseThrow(() -> new NegocioException(ProblemType.RECURSO_NAO_ENCONTRADO, "Refresh Token não está no banco de dados"));
    }

    @Operation(summary = "Verificar usuário", hidden = true)
    @PostMapping("/auth/check-user-email")
    public ResponseEntity<CheckUserNameDTO> checkUserName(@RequestBody CheckUserNameDTO checkUserName) {

        Set<ConstraintViolation<CheckUserNameDTO>> violations = getConstraintViolations(checkUserName);

        if (!violations.isEmpty()) {
            List<String> errorMessages = violations.stream().map(ConstraintViolation::getMessage).toList();
            return new ResponseEntity<>(getCheckUsernameResponseError(errorMessages.toString()), HttpStatus.BAD_REQUEST);
        }

        String userName = checkUserName.getUserName();
        UserInfo userInfo = iUserRepository.findByUsername(userName);
        userInfo = userService.decryptSensitiveFields(userInfo);
        
        if (Optional.ofNullable(userInfo).isEmpty()) {
            return new ResponseEntity<>(getCheckUsernameResponseError("Usuário não encontrado"), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @Operation(summary = "Recuperação de senha", hidden = true)
    @PostMapping("/auth/password-recovery")
    public ResponseEntity<?> passwordRecovery(@RequestBody CheckUserNameDTO checkUserName) throws Exception {

        Set<ConstraintViolation<CheckUserNameDTO>> violations = getConstraintViolations(checkUserName);

        if (!violations.isEmpty()) {
            List<String> errorMessages = violations.stream().map(ConstraintViolation::getMessage).toList();
            return new ResponseEntity<>(getCheckUsernameResponseError(errorMessages.toString()), HttpStatus.BAD_REQUEST);
        }

        String userName = checkUserName.getUserName();
        UserInfo userInfo = iUserRepository.findByUsername(userName);

        if ((Optional.ofNullable(iUserRepository.findByUsername(userName))).isEmpty()) {
            return new ResponseEntity<>(getCheckUsernameResponseError("Usuário não encontrado"), HttpStatus.BAD_REQUEST);
        }

        String linkPlain = OffsetDateTime.now()
                .toEpochSecond() + "," +
                userInfo.getId().toString() + "," +
                userInfo.getFullName().replace(" ", "-") + "," +
                userInfo.getEmail() + "," +
                OffsetDateTime.now().toEpochSecond();

        String link = linkPlain;
        String linkParse = link.replace("/", "-W-");
        String linkEmail = "http://localhost:4002/reset-password?link=" + linkParse;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        userInfo.setLink(link);
        userInfo.setReset(true);
        userInfo.setResetAt(timestamp);
        
        userService.update(userInfo);
        emailService.sendEmailFromTemplate(userInfo.getFullName(), linkEmail, userInfo.getEmail());
        List<PassRecoveryResponse.Object> objects = Collections.singletonList(PassRecoveryResponse.Object.builder().link(linkParse).build());

        return new ResponseEntity<>(getPassRecoveryResponseSuccess(objects, "Link de recuperação gerado com sucesso"), HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Resetar senha", hidden = true)
    @PostMapping("/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody CheckPasswordResetDTO checkPassword) {
        Set<ConstraintViolation<CheckPasswordResetDTO>> violations = validation.getValidator().validate(checkPassword);
        if (!violations.isEmpty()) {
            List<String> objects = violations.stream().map(ConstraintViolation::getMessage).toList();
            MessageResponse msgResponse = userService.messageList("erros", objects, MessageTypeEnum.MSG_BAD_REQUEST, "A senha está mal formatada ou não foi informada!");
            return new ResponseEntity<>(msgResponse, HttpStatusCode.valueOf(msgResponse.getStatus()));
        } else {
            String encodedPassword = passwordEncoder.encode(checkPassword.getPassword());
            MessageResponse msgResponse = userService.passwordReset(encodedPassword, checkPassword.getEmail(), checkPassword.getLink());
            return new ResponseEntity<>(msgResponse, HttpStatusCode.valueOf(msgResponse.getStatus()));
        }
    }

    @Operation(summary = "Finalizar Cadastro de usuário")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/auth/user/complete-registration/{idUser}")
    public UserResponse completeRegistration(@PathVariable Long idUser) {
        return userService.completeRegistration(idUser);
    }

    private Set<ConstraintViolation<CheckUserNameDTO>> getConstraintViolations(CheckUserNameDTO checkUserNameDTO) {
        return validation.getValidator().validate(checkUserNameDTO);
    }

    private static PassRecoveryResponse getPassRecoveryResponseSuccess(List<PassRecoveryResponse.Object> objects, String errorMessages) {
        return PassRecoveryResponse.builder()
                .status(HttpStatus.ACCEPTED.value())
                .type(HttpStatus.ACCEPTED.getReasonPhrase())
                .detail(REQUEST_SUCCESS_MESSAGE)
                .title(PASSWORD_RECOVERY_TITLE)
                .userMessage(errorMessages)
                .objects(objects)
                .timestamp(OffsetDateTime.now())
                .build();
    }


    private static CheckUserNameDTO getCheckUsernameResponseError(String errorMessages) {
        return CheckUserNameDTO.builder().userName(errorMessages)
                .build();
    }
}
