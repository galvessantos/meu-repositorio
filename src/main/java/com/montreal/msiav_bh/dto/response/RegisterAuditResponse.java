package com.montreal.msiav_bh.dto.response;

import java.time.LocalDateTime;

import com.montreal.core.domain.dto.RegisterAuditStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterAuditResponse {

    private Long id;
    private Long userId;
    private String action;
    private RegisterAuditStatusEnum status;
    private String description;
    private String ipAddress;
    private LocalDateTime timestamp;
}
