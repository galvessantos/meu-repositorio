package com.montreal.oauth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponseDTO {
	
    private String accessToken;
    private String token;
    private AuthResponseDTO userDetails;
    private Boolean requiresToken;
    
}
