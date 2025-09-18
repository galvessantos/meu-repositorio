package com.montreal.msiav_bh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificationDTO {
    @JsonProperty("forma_comunicacao")
    private String formaComunicacao;
    @JsonProperty("data_envio")
    private LocalDate dataEnvio;
    @JsonProperty("data_leitura")
    private LocalDate dataLeitura;
    @JsonProperty("arquivo_evidencia")
    private String arquivoEvidencia;
}