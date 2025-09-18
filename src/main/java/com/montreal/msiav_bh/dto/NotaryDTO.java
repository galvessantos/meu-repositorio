package com.montreal.msiav_bh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NotaryDTO {
    private String cns;
    private String nome;
    private String titular;
    private String substituto;
    private String endereco;
    @JsonProperty("telefone_contato")
    private String telefoneContato;
}
