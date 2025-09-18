package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemParameterRequest {

    @NotBlank(message = "O nome do sistema é obrigatório.")
    private String system;

    @NotBlank(message = "O nome do parâmetro é obrigatório.")
    private String parameter;

    @NotBlank(message = "O valor do parâmetro é obrigatório.")
    private String value;
}
