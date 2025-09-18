package com.montreal.oauth.domain.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PermissionResponseDTO {

    private Long id;
    private String action;
    private String subject;
    private String fields;
    private String description;
    private LocalDateTime createdAt;
}
