package com.montreal.msiav_bh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DetranDTO {
    @JsonProperty("sigla_uf")
    private String siglaUf;
    @JsonProperty("nome_detran")
    private String nomeDetran;
    private String estado;
}
