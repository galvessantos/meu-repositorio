package com.montreal.oauth.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    private LoginUserDTO user;
    private List<LoginPermissionDTO> permissions;
    private List<LoginFunctionalityDTO> functionalities;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginUserDTO {
        private Long id;
        private String username;
        private List<String> roles;
        private String companyId;
        private boolean enabled;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginPermissionDTO {
        private String action;
        private String subject;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginFunctionalityDTO {
        private String name;
    }
}