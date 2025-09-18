package com.montreal.msiav_bh.dto.request;

import com.montreal.core.domain.dto.RegisterAuditStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterAuditRequest {

    @NotNull(message = "O ID do usuário não pode ser nulo.")
    private Long userId;

    @NotBlank(message = "A ação não pode estar vazia.")
    private String action;

    @NotNull(message = "O status não pode ser nulo.")
    private RegisterAuditStatusEnum status;

    private String description;

    private String ipAddress;

    @NotNull(message = "O timestamp não pode ser nulo.")
    private LocalDateTime timestamp;
}
