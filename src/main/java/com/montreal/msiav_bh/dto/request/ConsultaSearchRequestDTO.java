package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ConsultaSearchRequestDTO(
        @JsonProperty("nome_credor") String nomeCredor,

        @JsonProperty("data_pedido")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataPedido,

        @JsonProperty("numero_contrato") String numeroContrato,
        @JsonProperty("etapa") String etapa,

        @JsonProperty("data_movimentacao")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataMovimentacao,

        @JsonProperty("uf_emplacamento") String ufEmplacamento,
        @JsonProperty("placa") String placa,
        @JsonProperty("modelo_veiculo") String modeloVeiculo
) {}
