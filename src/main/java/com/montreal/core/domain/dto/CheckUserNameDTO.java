package com.montreal.core.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckUserNameDTO {

    @NotBlank (message = "Nome de Usuário não informado")
    @NotNull (message = "Campo Nome de Usuário não pode ser nulo")
    @Email (message = "O Nome de Usuário informado não é válido")
    public String userName;

}
