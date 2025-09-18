package com.montreal.msiav_bh.dto;

import lombok.Data;

@Data
public class ContractDTO {
    private String numero;
    private String protocolo;
    private String data_contrato;
    private String data_pedido;
    private String data_notificacao;
    private String data_decurso_prazo;
    private String municipio_contrato;
    private String valor_divida;
    private String valor_leilao;
    private String descricao;
    private String valor_parcela;
    private String taxa_juros;
    private String certidao_busca_apreensao;
    private String data_primeira_parcela;
    private Integer quantidade_parcelas_pagas;
    private Integer quantidade_parcelas_abertas;
    private String protocolo_ba;
    private String data_certidao;
    private String data_restricao;
    private String numero_restricao;
    private String data_baixa_restricao;
    private String nsu;
    private String indicador_restricao_circulacao;
}
