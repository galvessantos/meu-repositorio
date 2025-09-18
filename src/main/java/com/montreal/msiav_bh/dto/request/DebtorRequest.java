package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtorRequest {

    @NotNull(message = "Nome do devedor não pode ser nulo")
    @Size(min = 1, max = 100, message = "Nome do devedor deve ter entre 1 e 100 caracteres")
    private String name;

    @NotNull(message = "CPF/CNPJ do devedor não pode ser nulo")
    private String cpfCnpj;

    @NotNull(message = "Endereço do devedor é obrigatório")
    private AddressRequest address;
    
}
