package com.montreal.oauth.domain.dto.request;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacialBiometricRequest {

    private Long userId;
    private String imageBase64;

}
