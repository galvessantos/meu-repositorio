package com.montreal.oauth.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacialBiometricResponse {

    private Long userId;
    private Double score;
    private boolean success;

}
