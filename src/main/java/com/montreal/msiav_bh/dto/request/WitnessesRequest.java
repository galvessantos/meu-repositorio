package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WitnessesRequest {

    @NotBlank(message = "O nome da testemunha não pode ser nulo ou vazio.")
    private String name;

    @NotBlank(message = "O RG da testemunha não pode ser nulo ou vazio.")
    @Pattern(regexp = "\\d{7,12}", message = "O RG deve conter entre 7 e 12 dígitos numéricos.")
    private String rg;

    @NotBlank(message = "O CPF da testemunha não pode ser nulo ou vazio.")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00.")
    private String cpf;
}
