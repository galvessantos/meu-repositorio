package com.montreal.oauth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "Dados de resposta de autenticação contendo informações do usuário, permissões e funcionalidades")
public class AuthResponseDTO {
	
    @Schema(description = "Dados do usuário autenticado")
    private Object user;
    
    @Schema(description = "Lista de permissões do usuário")
    private List<PermissionDetailsDTO> permissions;
    
    @Schema(description = "Lista de funcionalidades disponíveis para o usuário")
    private List<FunctionalityDetailsDTO> functionalities;

    @Data
    @Builder
    @Schema(description = "Detalhes do usuário autenticado")
    public static class UserDetailsDTO {
        
        @Schema(description = "ID único do usuário", example = "1")
        private Long id;
        
        @Schema(description = "Nome de usuário/login", example = "usuario@exemplo.com")
        private String username;
        
        @Schema(description = "Endereço de email do usuário", example = "usuario@exemplo.com")
        private String email;
        
        @Schema(description = "Lista de roles/papéis do usuário", example = "[\"USER\", \"ADMIN\"]")
        private List<String> roles;
        
        @Schema(description = "CPF do usuário", example = "12345678901")
        private String cpf;
        
        @Schema(description = "Telefone do usuário", example = "+5511999999999")
        private String phone;
        
        @Schema(description = "ID da empresa do usuário", example = "123")
        private String companyId;
        
        @Schema(description = "Link personalizado do usuário")
        private String link;
        
        @Schema(description = "Token temporário para operações especiais")
        private String tokenTemporary;
        
        @Schema(description = "Data de expiração do token temporário")
        private LocalDateTime tokenExpiredAt;
        
        @Schema(description = "Indica se o usuário está em processo de reset", example = "false")
        private boolean isReset;
        
        @Schema(description = "Data do último reset de senha")
        private LocalDateTime resetAt;
        
        @Schema(description = "Indica se o usuário está ativo", example = "true")
        private boolean isEnabled;
        
        @Schema(description = "Indica se o usuário foi criado por um administrador", example = "false")
        private boolean isCreatedByAdmin;
        
        @Schema(description = "Indica se o usuário já alterou a senha padrão", example = "true")
        private boolean isPasswordChangedByUser;
    }

    @Data
    @Builder
    @Schema(description = "Detalhes de uma permissão do usuário")
    public static class PermissionDetailsDTO {
        
        @Schema(description = "Ação permitida", example = "read")
        private String action;
        
        @Schema(description = "Recurso/entidade sobre o qual a ação é permitida", example = "user")
        private String subject;
        
        @Schema(description = "Campos específicos aos quais a permissão se aplica")
        private List<String> fields;
        
        @Schema(description = "Descrição da permissão", example = "Permite leitura de dados de usuário")
        private String description;
        
        @Schema(description = "Condições específicas para aplicação da permissão")
        private Map<String, List<String>> conditions;
    }

    @Data
    @Builder
    @Schema(description = "Detalhes de uma funcionalidade disponível para o usuário")
    public static class FunctionalityDetailsDTO {
        
        @Schema(description = "ID único da funcionalidade", example = "1")
        private Long id;
        
        @Schema(description = "Nome da funcionalidade", example = "Gestão de Usuários")
        private String name;
        
        @Schema(description = "Descrição da funcionalidade", example = "Permite gerenciar usuários do sistema")
        private String description;
        
        @Schema(description = "Lista de roles que têm acesso a esta funcionalidade")
        private List<String> availableFor;
    }
}

