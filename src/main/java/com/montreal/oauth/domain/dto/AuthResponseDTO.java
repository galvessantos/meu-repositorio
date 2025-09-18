package com.montreal.oauth.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AuthResponseDTO {
	
    private Object user;
    private List<PermissionDetailsDTO> permissions;
    private List<FunctionalityDetailsDTO> functionalities;

    @Data
    @Builder
    public static class UserDetailsDTO {
        private Long id;
        private String username;
        private String email;
        private List<String> roles;
        private String cpf;
        private String phone;
        private String companyId;
        private String link;
        private String tokenTemporary;
        private LocalDateTime tokenExpiredAt;
        private boolean isReset;
        private LocalDateTime resetAt;
        private boolean isEnabled;
        private boolean isCreatedByAdmin;
        private boolean isPasswordChangedByUser;
    }

    @Data
    @Builder
    public static class PermissionDetailsDTO {
        private String action;
        private String subject;
        private List<String> fields;
        private String description;
        private Map<String, List<String>> conditions;
    }

    @Data
    @Builder
    public static class FunctionalityDetailsDTO {
        private Long id;
        private String name;
        private String description;
        private List<String> availableFor;
    }
}

