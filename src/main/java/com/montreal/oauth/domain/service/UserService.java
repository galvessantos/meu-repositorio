package com.montreal.oauth.domain.service;

import com.montreal.core.domain.dto.response.MessageResponse;
import com.montreal.core.domain.enumerations.MessageTypeEnum;
import com.montreal.core.domain.exception.ClientServiceException;
import com.montreal.core.domain.exception.ConflictUserException;
import com.montreal.core.domain.exception.CryptoException;
import com.montreal.core.domain.exception.EmailException;
import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.NegocioException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.domain.exception.UnauthorizedException;
import com.montreal.core.domain.exception.UserNotFoundException;
import com.montreal.core.domain.service.EmailService;
import com.montreal.core.exception_handler.ProblemType;
import com.montreal.core.utils.PostgresCryptoUtil;
import com.montreal.msiav_bh.entity.Company;
import com.montreal.msiav_bh.repository.CompanyRepository;
import com.montreal.oauth.domain.component.CryptoComponent;
import com.montreal.oauth.domain.dto.AuthRequestDTO;
import com.montreal.oauth.domain.dto.AuthResponseDTO;
import com.montreal.oauth.domain.dto.JwtResponseDTO;
import com.montreal.oauth.domain.dto.request.UserRequest;
import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.RefreshToken;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.domain.exception.AuthenticateException;
import com.montreal.oauth.domain.exception.DuplicateUserException;
import com.montreal.oauth.domain.exception.ResourceNotFoundException;
import com.montreal.oauth.domain.mapper.MessageMapper;
import com.montreal.oauth.domain.repository.IRoleRepository;
import com.montreal.oauth.domain.repository.IUserRepository;
import com.montreal.oauth.exception.BusinessException;
import com.montreal.oauth.mapper.IUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Value("${montreal.oauth.encryptSecretKey}")
    String encryptSecretKey;

    private final CryptoComponent aes;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PostgresCryptoUtil postgresCryptoUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired(required = false)
    private PasswordHistoryService passwordHistoryService;

    @Transactional
    private UserInfo createAndSaveUser(UserRequest userRequest) {
        validateUserCreated(userRequest);
        findRoles(userRequest);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(userRequest.getPassword());

        UserInfo user = IUserMapper.INSTANCE.toEntity(userRequest);
        user.setCreatedByAdmin(true);
        user.setPasswordChangedByUser(false);
        user.setResetAt(new Timestamp(System.currentTimeMillis()));
        user.setPassword(encodedPassword);


        user = encryptSensitiveFields(user);

        return userRepository.save(user);
    }

    public UserResponse saveUser(UserRequest userRequest) {
        try {

            UserInfo savedUser = createAndSaveUser(userRequest);


            UserInfo userForReturn = decryptSensitiveFields(savedUser);
            return IUserMapper.INSTANCE.toResponse(userForReturn);

        } catch (DataIntegrityViolationException e) {
            throw new ConflictUserException(
                    String.format("Usuário com o username '%s' já existe", userRequest.getUsername()));
        } catch (DuplicateUserException | NegocioException | ClientServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao salvar usuário", e);
            throw new InternalErrorException(
                    String.format("Falha ao salvar usuário, erro: %s", e.getMessage()));
        }
    }

    public boolean sendEmailRegister(Long userId) {
    	log.info("Enviando e-mail de cadastro para o usuário com ID: {}", userId);
        try {
        	Optional<UserInfo> user = userRepository.findById(userId);
            if(user.isPresent()) {
            	sendEmailWithRollback(user.get());
            	return true;
            }else {
            	log.error("Nenhum usuário encontrado com ID: {}", userId);
            	return false;
            }
        } catch (EmailException e) {
            log.error("Erro ao enviar e-mail de cadastro para usuário ID {}: {}", userId, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Erro inesperado ao tentar enviar e-mail para usuário ID {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }


    private void sendEmailWithRollback(UserInfo savedUser) {
        try {
        	savedUser = decryptSensitiveFields(savedUser);
            emailService.sendEmailRegistrationConfirmation(savedUser);
        } catch (EmailException e) {
            log.error("Erro ao enviar e-mail de confirmação. Removendo usuário criado.", e);
            userRepository.deleteById(savedUser.getId());
            throw new EmailException("Erro ao enviar e-mail. Usuário removido.", e);
        }
    }

    public UserResponse getUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Nenhum usuário autenticado encontrado no contexto de segurança.");
                return null;
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails)) {
                log.warn("O principal autenticado não é uma instância de UserDetails.");
                return null;
            }

            String username = ((UserDetails) principal).getUsername();
            Optional<UserInfo> optionalUser = userRepository.obterByUsername(username);

            if (optionalUser.isEmpty()) {
                log.warn("Usuário com username '{}' não encontrado no banco de dados.", username);
                return null;
            }

            UserInfo user = optionalUser.get();
            user = decryptSensitiveFields(user);

            return modelMapper.map(user, UserResponse.class);

        } catch (Exception e) {
            log.error("Erro ao tentar recuperar o usuário autenticado: {}", e.getMessage(), e);
            return null;
        }
    }

    public UserInfo getUserInfo() {
    	try {
    		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    		UserDetails userDetail = (UserDetails) authentication.getPrincipal();
    		String usernameFromAccessToken = userDetail.getUsername();

    		Optional<UserInfo> user = userRepository.obterByUsername(usernameFromAccessToken);
    		if (user.isPresent()) {
    			return decryptSensitiveFields(user.get());
    		} else {
    			log.warn("Usuário com username '{}' não encontrado no banco de dados.", usernameFromAccessToken);
    			return null;
    		}
    	} catch (Exception e) {
    		log.error("Erro ao tentar recuperar o usuário autenticado: {}", e.getMessage(), e);
    		return null;
    	}
    }

    public UserInfo getUserInfo(Long id) {
    	UserInfo user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Veiculo não encontrado"));
    	return decryptSensitiveFields(user);
    }

    public MessageResponse passwordRecoveryValidation(String key, List<String> objects) {
        return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_BAD_REQUEST, "O email está mal formatado ou não foi informado!", this.addListObj(key, objects)).build();
    }

    public MessageResponse messageList(String key, List<String> objects, MessageTypeEnum msgType, String details) {
        return MessageMapper.createMessageBuilder(msgType, details, this.addListObj(key, objects)).build();
    }

    public UserResponse findByEmail(String email) {
        return modelMapper.map(decryptSensitiveFields(userRepository.findByEmail(email)), UserResponse.class);
    }

    public MessageResponse passwordRecovery(String email) {
        try {
            Optional<UserInfo> optionalUser = Optional.ofNullable(userRepository.findByEmail(email));

            if (optionalUser.isEmpty()) {
                return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_NOT_FOUND, "O email não foi encontrado!", this.addSingleObj("erros", "Não foi possível identificar o usuário com o email informado!")).build();
            }
            UserInfo user = optionalUser.get();
            user = decryptSensitiveFields(user);
            String linkPlain = OffsetDateTime.now().toEpochSecond() +
                    "," +
                    user.getId() +
                    "," +
                    user.getFullName().replace(" ", "-") +
                    "," +
                    user.getEmail() +
                    "," +
                    OffsetDateTime.now().toEpochSecond();

            String link = aes.encryptFromString(linkPlain, encryptSecretKey);
            String linkParse = link.replace("/", "-W-");
            String linkEmail = "http://localhost:4202/#/home?link=" + linkParse;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            user.setLink(link);
            user.setReset(true);
            user.setResetAt(timestamp);

            userRepository.save(user);
            emailService.sendEmailFromTemplate(user.getFullName(), linkEmail, user.getEmail());

            return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_OK, "Link de recuperação gerado com sucesso!", this.addSingleObj("link", linkParse)).build();

        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalErrorException("Falha ao gerar link de recuperação de senha!", e);
        }
    }

    public MessageResponse login(String username, String password) {

        UserInfo user = userRepository.findByUsername(username);
        user = decryptSensitiveFields(user);

        if ((Optional.ofNullable(user)).isEmpty()) {
            return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_UNAUTHORIZED, "Acesso Negado!", this.addSingleObj("erros", "Não foi possível acessar com os dados informados!")).build();
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if(authentication.isAuthenticated()) {
            String accessToken;
            try {
                accessToken = aes.encryptFromString(jwtService.GenerateToken(username), encryptSecretKey);
            } catch (Exception e) {
                throw new NegocioException(ProblemType.ERRO_NEGOCIO, "Erro ao gerar o token de acesso", e);
            }

            String token = refreshTokenService.getTokenByUserId(user.getId());
            if(token.isEmpty()) {
                RefreshToken refreshToken =  refreshTokenService.createRefreshToken(username);
                token = refreshToken.getToken();
            }

            var jwtResponseDTO = JwtResponseDTO.builder().accessToken(accessToken).token(token).build();
            Map<String, List<String>> list = new HashMap<>();

            list.put("token", Collections.singletonList(jwtResponseDTO.getToken()));
            list.put("accessToken", Collections.singletonList(jwtResponseDTO.getAccessToken()));

            return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_OK, "Acesso realizado com sucesso!", list).build();
        } else {
            return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_UNAUTHORIZED, "Acesso Negado!", this.addSingleObj("erros", "Não foi possível acessar com os dados informados!")).build();
        }
    }

    public MessageResponse passwordReset(String password, String email, String link) {
        UserInfo user = userRepository.findByEmail(email);
        user = decryptSensitiveFields(user);
        if ((Optional.ofNullable(user)).isEmpty()) {
            return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_NOT_FOUND, "O email não foi encontrado", this.addSingleObj("erros", "Não foi possível acessar com os dados informados!")).build();
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        user.setPassword(encodedPassword);
        user.setReset(false);
        user = encryptSensitiveFields(user);
        userRepository.save(user);
        return MessageMapper.createMessageBuilder(MessageTypeEnum.MSG_OK, "Link de recuperação gerado com sucesso!", this.addSingleObj("OK", "!")).build();
    }

    public void update(UserInfo user) {
    	user = encryptSensitiveFields(user);
        userRepository.save(user);
    }

    public Map<String, List<String>> addListObj(String key, List<String> objects) {
        return new HashMap<>(Map.of(key, new ArrayList<>(objects)));
    }

    public Map<String, List<String>> addSingleObj(String key, String value) {
        return new HashMap<>(Map.of(key, Collections.singletonList(value)));
    }

    private void findRoles(UserRequest userRequest) {
        userRequest.getRoles().forEach(role ->
            roleRepository.findByName(role.getName()).ifPresentOrElse(
                    foundRole -> role.setId(foundRole.getId()),
                    () -> {
                        throw new NegocioException(String.format("Não foi possível encontrar o papel com o nome: %s", role.getName()));
                    }
            )
        );
    }

    @Transactional
    public UserInfo updateUser(Long userId, UserRequest userRequest) {
        try {

            UserInfo existingUser = userRepository.findFirstById(userId);
            if (existingUser == null) {
                throw new UserNotFoundException("Não é possível encontrar o registro com o identificador: " + userId);
            }
            existingUser = decryptSensitiveFields(existingUser);

            if (!existingUser.getUsername().equals(userRequest.getUsername()) && userRepository.existsByUsername(userRequest.getUsername())) {
                throw new ConflictUserException(String.format("Usuário com o username '%s' já existe", userRequest.getUsername()));
            }

            if (!existingUser.getEmail().equals(userRequest.getEmail()) && userRepository.existsByEmail(userRequest.getEmail())) {
                throw new ConflictUserException(String.format("Usuário com o email '%s' já existe", userRequest.getEmail()));
            }

            Set<Role> userRoles = userRequest.getRoles().stream()
            	    .map(roleRequest -> {
                        Role role = new Role();
            	        role.setId(roleRequest.getId());
            	        role.setName(roleRequest.getName());
                        role.setBiometricValidation(roleRequest.getBiometricValidation());
                        role.setRequiresTokenFirstLogin(roleRequest.getRequiresTokenFirstLogin());
            	        return role;
            	    })
            	    .collect(Collectors.toSet());

            existingUser.setUsername(userRequest.getUsername());
            existingUser.setFullName(userRequest.getFullName());
            existingUser.setEmail(userRequest.getEmail());
            existingUser.setEnabled(userRequest.isEnabled());
            existingUser.setRoles(userRoles);
            existingUser.setPasswordChangedByUser(existingUser.isPasswordChangedByUser());
            existingUser.setCreatedByAdmin(existingUser.isCreatedByAdmin());
            existingUser.setCompanyId(userRequest.getCompanyId());
            existingUser.setCpf(userRequest.getCpf());
            existingUser.setPhone(userRequest.getPhone());
            existingUser = encryptSensitiveFields(existingUser);
            return userRepository.save(existingUser);
        } catch (UserNotFoundException | ConflictUserException e) {
            log.error("Erro ao atualizar usuario", e);
            throw e;
        } catch (Exception e) {
            log.error("Erro ao atualizar usuario ", e);
            throw new InternalErrorException("Falha ao atualizar usuário");
        }
    }

    public UserResponse changePassword(Long userId, String newPassword) {
        UserInfo user = getUserById(userId);

        validatePassword(newPassword);

        if (passwordHistoryService != null) {
            passwordHistoryService.validatePasswordHistory(user, newPassword);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedNewPassword = encoder.encode(newPassword);

        if (passwordHistoryService != null) {
            passwordHistoryService.savePasswordToHistory(user, encodedNewPassword);
        }

        user.setPassword(encodedNewPassword);
        user.setPasswordChangedByUser(true);
        user.setEnabled(true);
        user = encryptSensitiveFields(user);
        var userSaved = userRepository.save(user);
        userSaved = decryptSensitiveFields(userSaved);
        return IUserMapper.INSTANCE.toResponse(userSaved);
    }

    public UserInfo getUserById(Long id) {
    	UserInfo user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID: " + id));
    	return decryptSensitiveFields(user);
    }

    @Transactional
    public JwtResponseDTO authenticate(AuthRequestDTO auth) {
        try {
            log.info("Autenticando usuário: {}", auth.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(auth.getUsername(), auth.getPassword())
            );

            if (authentication.isAuthenticated()) {
                UserInfo user = userRepository.findByUsername(auth.getUsername());
                user = decryptSensitiveFields(user);

                if (!user.isPasswordChangedByUser()) {
                    throw new AuthenticateException(String.format("Usuário %s não definiu a senha", auth.getUsername()));
                }

                boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> RoleEnum.ROLE_ADMIN.equals(role.getName()));

                if (!isAdmin) {
                    Company company = companyRepository.findById(Long.valueOf(user.getCompanyId()))
                            .orElseThrow(() -> new AuthenticateException("Empresa associada ao usuário não encontrada."));

                    if (!company.getIsActive()) {
                        throw new BusinessException(
                                String.format("Empresa %s está inativa e não pode acessar o sistema.", company.getName())
                        );
                    }
                }

                String token = refreshTokenService.getTokenByUserId(user.getId());
                String accessToken = jwtService.GenerateToken(auth.getUsername());

                if (token.isEmpty()) {
                    RefreshToken refreshToken = refreshTokenService.createRefreshToken(auth.getUsername());
                    token = refreshToken.getToken();
                }

                AuthResponseDTO.UserDetailsDTO userDetails = AuthResponseDTO.UserDetailsDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
                        .cpf(user.getCpf())
                        .phone(user.getPhone())
                        .companyId(user.getCompanyId() != null ? user.getCompanyId() : null)
                        .link(user.getLink())
                        .tokenTemporary(user.getTokenTemporary())
                        .tokenExpiredAt(user.getTokenExpiredAt())
                        .isReset(user.isReset())
                        .isEnabled(user.isEnabled())
                        .isCreatedByAdmin(user.isCreatedByAdmin())
                        .isPasswordChangedByUser(user.isPasswordChangedByUser())
                        .build();

                List<AuthResponseDTO.PermissionDetailsDTO> permissions;
                List<AuthResponseDTO.FunctionalityDetailsDTO> functionalities;

                if (isAdmin) {
                    permissions = List.of(
                            AuthResponseDTO.PermissionDetailsDTO.builder()
                                    .action("manage")
                                    .subject("all")
                                    .description("Permite ao administrador realizar qualquer ação em qualquer entidade ou funcionalidade.")
                                    .build()
                    );

                    functionalities = List.of(
                            AuthResponseDTO.FunctionalityDetailsDTO.builder()
                                    .name("manageAll")
                                    .description("Permite ao administrador realizar qualquer funcionalidade do sistema.")
                                    .availableFor(List.of("ROLE_ADMIN"))
                                    .build()
                    );

                } else {
                    permissions = user.getRoles().stream()
                            .flatMap(role -> role.getRolePermissions().stream())
                            .map(rolePermission -> {
                                var permission = rolePermission.getPermission();
                                return AuthResponseDTO.PermissionDetailsDTO.builder()
                                        .action(permission.getAction())
                                        .subject(permission.getSubject())
                                        .fields(permission.getFields() != null ? List.of(permission.getFields().split(",")) : null)
                                        .description(permission.getDescription())
                                        .conditions(null)
                                        .build();
                            })
                            .distinct()
                            .toList();

                    functionalities = user.getRoles().stream()
                            .flatMap(role -> role.getRoleFunctionalities().stream())
                            .map(roleFunctionality -> {
                                var functionality = roleFunctionality.getFunctionality();
                                return AuthResponseDTO.FunctionalityDetailsDTO.builder()
                                        .id(functionality.getId())
                                        .name(functionality.getName())
                                        .description(functionality.getDescription())
                                        .availableFor(List.of(roleFunctionality.getRole().getName().name()))
                                        .build();
                            })
                            .distinct()
                            .toList();
                }

                AuthResponseDTO userData = AuthResponseDTO.builder()
                        .user(userDetails)
                        .permissions(permissions)
                        .functionalities(functionalities)
                        .build();

                return JwtResponseDTO.builder()
                        .accessToken(accessToken)
                        .token(token)
                        .userDetails(userData)
                        .build();
            } else {
                throw new AuthenticateException("Falha ao autenticar Usuário " + auth.getUsername());
            }

        } catch (AuthenticateException e) {
            throw e;
        } catch (DisabledException e) {
            throw new AuthenticateException(String.format("Usuário %s está desativado", auth.getUsername()));
        } catch (Exception e) {
            throw new AuthenticateException(String.format("Falha ao autenticar Usuário %s - Falha: %s", auth.getUsername(), e.getMessage()));
        }
    }

    private void validateUserCreated(UserRequest userRequest) {

        boolean isAdmin = userRequest.getRoles().stream().anyMatch(role -> role.getName() == RoleEnum.ROLE_ADMIN);
        log.info("Validando usuário. É administrador? {}", isAdmin);

        if (userRepository.existsByUsernameIgnoreCase(userRequest.getUsername())) {
            throw new NegocioException(String.format("O username '%s' já está em uso.", userRequest.getUsername()));
        }

        if (!isAdmin && (userRequest.getCompanyId() == null)) {
            throw new NegocioException("O campo companyId é obrigatório para usuários não administradores.");
        }


        if (isCpfExist(userRequest.getCpf())) {
            throw new DuplicateUserException(
                    String.format("O CPF %s já está em uso.", userRequest.getCpf()));
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateUserException(String.format("O username %s já está em uso.", userRequest.getUsername()));
        }
    }


    private Optional<String> safeDecrypt(String encryptedHex) {
        try {
            return Optional.ofNullable(postgresCryptoUtil.decrypt(encryptedHex));
        } catch (Exception e) {
            log.warn("Não foi possível descriptografar CPF do usuário (hex='{}')", encryptedHex, e);
            return Optional.empty();
        }
    }

    private boolean isCpfExist(String rawCpfInput) {
        String rawCpf = rawCpfInput.replaceAll("\\D", "");

        return userRepository.findAll().stream()
                .map(UserInfo::getCpf)
                .map(this::safeDecrypt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(decrypted -> decrypted.replaceAll("\\D", ""))
                .anyMatch(decrypted -> decrypted.equals(rawCpf));
    }

    public UserResponse completeRegistration(Long idUser) {
        log.info("Completando registro para usuário com ID: {}", idUser);
        try {
            var user = getUserById(idUser);

            user.setPasswordChangedByUser(true);
            user.setEnabled(true);
            user.setTokenTemporary(null);
            user.setTokenExpiredAt(null);
            user = encryptSensitiveFields(user);
            var userSaved = userRepository.save(user);
            userSaved = decryptSensitiveFields(userSaved);
            return IUserMapper.INSTANCE.toResponse(userSaved);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao completar o registro do usuário", e);
            throw new InternalErrorException("Falha ao completar o registro do usuário");
        }
    }

    private void validatePassword(String password) {
        log.info("Validando critérios da senha");

        if (password.length() < 4 || password.length() > 8) {
            throw new NegocioException("A senha deve ter entre 4 e 8 caracteres");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new NegocioException("A senha deve conter pelo menos uma letra minúscula");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new NegocioException("A senha deve conter pelo menos uma letra maiúscula");
        }
        if (!password.matches(".*\\d.*")) {
            throw new NegocioException("A senha deve conter pelo menos um número");
        }
        if (!password.matches(".*[_@#].*")) {
            throw new NegocioException("A senha deve conter pelo menos um dos caracteres especiais: _ @ #");
        }
    }

    public UserInfo findById(Long userId) {
        log.info("Buscando usuário pelo ID: {}", userId);
        UserInfo user =  userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
        return decryptSensitiveFields(user);
    }

    public List<UserInfo> findUsersByCompanyId(Long companyId) {
        log.info("Buscando usuários pelo companyId: {}", companyId);
        List<UserInfo> users = userRepository.findAllByCompanyId(companyId);
        if (users.isEmpty()) {
            log.warn("Nenhum usuário encontrado para o companyId: {}", companyId);
        }

        return users.stream()
                    .map(this::decryptSensitiveFields)
                    .collect(Collectors.toList());
    }

    public UserInfo getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException( "Usuário não está autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            UserInfo user = userRepository.findByUsername(username);
            if (user == null) {
                throw new NotFoundException("Usuário autenticado não encontrado");
            }
            return decryptSensitiveFields(user);
        } else {
            throw new UnauthorizedException("Usuário não está autenticado");
        }
    }

    public UserInfo encryptSensitiveFields(UserInfo user) {
        if (user == null) {
            return null;
        }

        if (user.getCpf() != null && !user.getCpf().isEmpty()) {
            try {
                user.setCpf(postgresCryptoUtil.encrypt(user.getCpf()));
            } catch (Exception e) {
                log.error("Erro ao criptografar o CPF do usuário '{}': {}", user.getUsername(), e.getMessage(), e);
                user.setCpf(null);
            }
        }

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            try {
                user.setPhone(postgresCryptoUtil.encrypt(user.getPhone()));
            } catch (Exception e) {
                log.error("Erro ao criptografar o telefone do usuário '{}': {}", user.getUsername(), e.getMessage(), e);
                user.setPhone(null);
            }
        }
        return user;
    }


    public UserInfo decryptSensitiveFields(UserInfo user) {

        if (user == null) {
            return null;
        }

        UserInfo userCopy = new UserInfo();
        userCopy.setId(user.getId());
        userCopy.setUsername(user.getUsername());
        userCopy.setPassword(user.getPassword());
        userCopy.setFullName(user.getFullName());
        userCopy.setEmail(user.getEmail());
        userCopy.setEnabled(user.isEnabled());
        userCopy.setRoles(user.getRoles());
        userCopy.setCompanyId(user.getCompanyId());
        userCopy.setCreatedByAdmin(user.isCreatedByAdmin());
        userCopy.setPasswordChangedByUser(user.isPasswordChangedByUser());
        userCopy.setLink(user.getLink());
        userCopy.setReset(user.isReset());
        userCopy.setResetAt(user.getResetAt());
        userCopy.setTokenTemporary(user.getTokenTemporary());
        userCopy.setTokenExpiredAt(user.getTokenExpiredAt());
        userCopy.setEnabled(user.isEnabled());

        if (user.getCpf() != null) {
            try {
                userCopy.setCpf(postgresCryptoUtil.decrypt(user.getCpf()));
            } catch (Exception e) {
                log.error("Erro ao descriptografar o CPF do usuário '{}': {}", user.getUsername(), e.getMessage(), e);
                userCopy.setCpf(null);
            }
        }

        if (user.getPhone() != null) {
            try {
                userCopy.setPhone(postgresCryptoUtil.decrypt(user.getPhone()));
            } catch (Exception e) {
                log.error("Erro ao descriptografar o telefone do usuário '{}': {}", user.getUsername(), e.getMessage(), e);
                userCopy.setPhone(null);
            }
        }

        return userCopy;
    }

    public UserResponse decryptSensitiveFieldsForResponse(UserInfo user) {

        if (user == null) {
            return null;
        }

        UserResponse userResponse = IUserMapper.INSTANCE.toResponse(user);

        try {
            userResponse.setCpf(getDecryptedCpf(user.getCpf()));
            userResponse.setPhone(getDecryptedPhone(user.getPhone()));
        } catch (Exception e) {
            log.error("Erro ao descriptografar dados do usuário '{}': {}", user.getUsername(), e.getMessage(), e);
        }

        return userResponse;
    }

    private String getDecryptedCpf(String cpf) {
        if (StringUtils.isBlank(cpf)) {
            return null;
        }
        return postgresCryptoUtil.decrypt(cpf);
    }

    private String getDecryptedPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        return postgresCryptoUtil.decrypt(phone);
    }

    public Page<UserResponse> getFilteredUsers(Pageable pageable, HttpServletRequest request) {
        StringBuilder sql = new StringBuilder("SELECT u.*, STRING_AGG(r.name::text, ',') as roles_ids " +
                "FROM users u inner join users_roles ur on u.id = ur.user_info_id " +
                "inner join roles r on ur.roles_id = r.id WHERE 1=1 ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> normalizedParams = new HashMap<>();

            for (String key : parameterMap.keySet()) {
                String normalizedKey = key.equalsIgnoreCase("fullName") ? "fullname" : key.toLowerCase();
                normalizedParams.put(normalizedKey, request.getParameter(key));
            }

            if (normalizedParams.containsKey("id")) {
                sql.append("AND id = :id ");
                countSql.append("AND id = :id ");
                params.put("id", Long.parseLong(normalizedParams.get("id")));
            }

            if (normalizedParams.containsKey("username")) {
                sql.append("AND LOWER(username) LIKE LOWER(:username) ");
                countSql.append("AND LOWER(username) LIKE LOWER(:username) ");
                params.put("username", "%" + normalizedParams.get("username") + "%");
            }

            if (normalizedParams.containsKey("email")) {
                sql.append("AND LOWER(email) LIKE LOWER(:email) ");
                countSql.append("AND LOWER(email) LIKE LOWER(:email) ");
                params.put("email", "%" + normalizedParams.get("email") + "%");
            }

            if (normalizedParams.containsKey("fullname")) {
                sql.append("AND LOWER(fullname) LIKE LOWER(:fullname) ");
                countSql.append("AND LOWER(fullname) LIKE LOWER(:fullname) ");
                params.put("fullname", "%" + normalizedParams.get("fullname") + "%");
            }

            if (normalizedParams.containsKey("enabled")) {
                sql.append("AND is_enabled = :isEnabled ");
                countSql.append("AND is_enabled = :isEnabled ");
                params.put("isEnabled", Boolean.parseBoolean(normalizedParams.get("enabled")));
            }

            if (normalizedParams.containsKey("cpf") && !normalizedParams.get("cpf").isBlank()) {
                sql.append("AND cpf ~ '^[0-9a-fA-F]+$' ");
                sql.append("AND descriptografar(decode(cpf, 'hex')) = :cpf ");
                countSql.append("AND cpf ~ '^[0-9a-fA-F]+$' ");
                countSql.append("AND descriptografar(decode(cpf, 'hex')) = :cpf ");
                params.put("cpf", normalizedParams.get("cpf"));
            }

            sql.append(" GROUP BY u.id, u.email, u.fullname, u.is_enabled, u.is_reset, u.link, u.password, " +
                    "u.reset_at, u.username, u.cpf, u.is_created_by_admin, " +
                    "u.is_password_changed_by_user, u.company_id, u.phone, " +
                    "u.token_temporary, u.token_expired_at ");

            if (pageable.getSort().isSorted()) {
                sql.append("ORDER BY ");
                String order = pageable.getSort().stream()
                        .map(order1 -> order1.getProperty() + " " + order1.getDirection().name())
                        .collect(Collectors.joining(", "));
                sql.append(order);
            } else {
                sql.append("ORDER BY id ASC ");
            }

            sql.append(" LIMIT :limit OFFSET :offset");
            params.put("limit", pageable.getPageSize());
            params.put("offset", pageable.getOffset());

            log.info("Executando query de usuários com SQL: {}", sql);
            List<UserInfo> users = namedParameterJdbcTemplate.query(
                sql.toString(),
                new MapSqlParameterSource(params),
                (rs, rowNum) -> {
                    UserInfo u = new UserInfo();
                    u.setId(rs.getLong("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setFullName(rs.getString("fullname"));
                    u.setCpf(rs.getString("cpf"));
                    u.setPhone(rs.getString("phone"));
                    u.setEnabled(rs.getBoolean("is_enabled"));
                    u.setCompanyId(rs.getString("company_id"));
                    Set<Role> roles = Arrays.stream(rs.getString("roles_ids")
                            .split(","))
                            .map(Role::new)
                            .collect(Collectors.toSet());
                    u.setRoles(roles);
                    return decryptSensitiveFields(u);
                }
            );

            Integer totalResult = namedParameterJdbcTemplate.queryForObject(
                    countSql.toString(),
                    new MapSqlParameterSource(params),
                    Integer.class
            );
            int total = totalResult != null ? totalResult : 0;

            users.forEach(userInfo -> userInfo.getRoles()
                    .forEach(role -> roleRepository.findByName(role.getName())
                            .ifPresent(roleEntity -> role.setId(roleEntity.getId()))));

            List<UserResponse> responses = users.stream()
                    .map(user -> modelMapper.map(user, UserResponse.class))
                    .toList();

            return new PageImpl<>(responses, pageable, total);

        } catch (Exception e) {
            log.error("Erro ao filtrar usuários: {}", e.getMessage(), e);
            throw new InternalErrorException("Erro ao filtrar usuários", e);
        }
    }


}
