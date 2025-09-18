package com.montreal.msiav_bh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgencyDTO {
    private String nome;
    private String cnpj;
    private String endereco;
    private String email;
    @JsonProperty("telefone_contato")
    private String telefoneContato;
}
