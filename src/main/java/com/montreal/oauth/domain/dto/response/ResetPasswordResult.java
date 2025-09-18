package com.montreal.oauth.domain.dto.response;

import com.montreal.oauth.domain.dto.AuthResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordResult {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private LoginResponseDTO userDetails;
}