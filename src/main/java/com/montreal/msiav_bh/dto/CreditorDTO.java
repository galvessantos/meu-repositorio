package com.montreal.msiav_bh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreditorDTO {
    private String nome;
    private String cnpj;
    private String email;
    @JsonProperty("inscricao_estadual")
    private String inscricaoEstadual;
    private String endereco;
    private String telefone;
}
