package com.montreal.msiav_bh.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record QueryDetailResponseDTO(
        boolean success,
        String message,
        Data data
) {
    public record Data(
            Credor credor,
            List<Devedor> devedores,
            List<Garantidor> garantidores,
            List<Veiculo> veiculos,
            Contrato contrato,
            Serventia serventia,
            Detran detran,

            @JsonProperty("orgao_escob")
            List<Orgao> orgaoEscob,

            @JsonProperty("orgao_guincho")
            List<Orgao> orgaoGuincho,

            @JsonProperty("orgao_despachante")
            List<Orgao> orgaoDespachante,

            @JsonProperty("orgao_leilao")
            List<Orgao> orgaoLeilao,

            @JsonProperty("orgao_localizador")
            List<Orgao> orgaoLocalizador,

            @JsonProperty("orgao_patio")
            List<Orgao> orgaoPatio,

            @JsonProperty("notificacao_eletronica")
            List<Notificacao> notificacaoEletronica,

            @JsonProperty("notificacao_via_ar")
            List<Notificacao> notificacaoViaAr
    ) {}

    public record Credor(
            String nome,
            String cnpj,
            String email,

            @JsonProperty("inscricao_estadual")
            String inscricaoEstadual,
            String endereco,
            String telefone
    ) {}

    public record Devedor(
            String nome,

            @JsonProperty("cpf_cnpj")
            String cpfCnpj,

            @JsonProperty("contatos_email")
            List<Email> contatosEmail,

            @JsonProperty("contatos_telefone")
            List<Telefone> contatosTelefone,

            List<Endereco> enderecos
    ) {}

    public record Garantidor(
            String nome,

            @JsonProperty("cpf_cnpj")
            String cpfCnpj
    ) {}

    public record Veiculo(
            String chassi,
            String renavam,
            Long gravame,
            String placa,

            @JsonProperty("marca_modelo")
            String marcaModelo,

            String cor,

            @JsonProperty("registro_detran")
            String registroDetran,

            @JsonProperty("possui_gps")
            String possuiGps,

            @JsonProperty("uf_emplacamento")
            String ufEmplacamento,

            @JsonProperty("ano_fabricacao")
            String anoFabricacao,

            @JsonProperty("ano_modelo")
            String anoModelo
    ) {}

    public record Contrato(
            String numero,
            String protocolo,

            @JsonProperty("data_contrato")
            LocalDate dataContrato,

            @JsonProperty("data_pedido")
            LocalDate dataPedido,

            @JsonProperty("data_notificacao")
            LocalDate dataNotificacao,

            @JsonProperty("data_decurso_prazo")
            LocalDate dataDecursoPrazo,

            @JsonProperty("municipio_contrato")
            String municipioContrato,

            @JsonProperty("valor_divida")
            BigDecimal valorDivida,

            @JsonProperty("valor_leilao")
            String valorLeilao,

            String descricao,

            @JsonProperty("valor_parcela")
            String valorParcela,

            @JsonProperty("taxa_juros")
            String taxaJuros,

            @JsonProperty("certidao_busca_apreensao")
            String certidaoBuscaApreensao,

            @JsonProperty("data_primeira_parcela")
            LocalDate dataPrimeiraParcela,

            @JsonProperty("quantidade_parcelas_pagas")
            Integer quantidadeParcelasPagas,

            @JsonProperty("quantidade_parcelas_abertas")
            Integer quantidadeParcelasAbertas,

            @JsonProperty("protocolo_b&a")
            String protocoloBa,

            @JsonProperty("data_certidao")
            LocalDate dataCertidao,

            @JsonProperty("data_restricao")
            LocalDate dataRestricao,

            @JsonProperty("numero_restricao")
            String numeroRestricao,

            @JsonProperty("data_baixa_restricao")
            LocalDate dataBaixaRestricao,

            String nsu,

            @JsonProperty("indicador_restricao_circulacao")
            String indicadorRestricaoCirculacao
    ) {}

    public record Serventia(
            Integer cns,
            String nome,
            String titular,
            String substituto,
            String endereco,

            @JsonProperty("telefone_contato")
            String telefoneContato
    ) {}

    public record Detran(
            @JsonProperty("sigla_uf")
            String siglaUf,

            @JsonProperty("nome_detran")
            String nomeDetran,

            String estado
    ) {}

    public record Orgao(
            String nome,
            String cnpj,
            String endereco,
            String email,

            @JsonProperty("telefone_contato")
            String telefoneContato
    ) {}

    public record Notificacao(
            @JsonProperty("forma_comunicacao")
            String formaComunicacao,

            @JsonProperty("data_envio")
            LocalDate dataEnvio,

            @JsonProperty("data_leitura")
            LocalDate dataLeitura,

            @JsonProperty("arquivo_evidencia")
            String arquivoEvidencia
    ) {}

    public record Email(String email) {}
    public record Telefone(String telefone) {}
    public record Endereco(String endereco) {}
}
