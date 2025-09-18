package com.montreal.msiav_bh.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public record ConsultaNotificationResponseDTO(
        @JsonProperty("success") Boolean success,
        @JsonProperty("message") String message,
        @JsonProperty("data") List<NotificationData> data
) {
    public record NotificationData(
            @JsonProperty("nome_credor") String nomeCredor,
            @JsonProperty("data_pedido") String dataPedido,
            @JsonProperty("numero_contrato") String numeroContrato,
            @JsonProperty("protocolo") String protocolo,
            @JsonProperty("etapa") String etapa,
            @JsonProperty("data_movimentacao") String dataMovimentacao,
            @JsonProperty("veiculos") List<VeiculoInfo> veiculos,
            @JsonProperty("credor") List<CredorInfo> credor,
            @JsonProperty("devedor") List<DevedorInfo> devedor,
            @JsonProperty("garantidor") List<GarantidorInfo> garantidor,
            @JsonProperty("contrato") List<ContratoInfo> contrato,
            @JsonProperty("serventia") List<ServentiaInfo> serventia
    ) {}

    public record VeiculoInfo(
            @JsonProperty("chassi") String chassi,
            @JsonProperty("renavam") String renavam,
            @JsonProperty("gravame") String gravame,
            @JsonProperty("placa") String placa,
            @JsonProperty("modelo") String modelo,
            @JsonProperty("ano_fabricacao") Integer anoFabricacao,
            @JsonProperty("ano_modelo") Integer anoModelo,
            @JsonProperty("cor") String cor,
            @JsonProperty("uf_emplacamento") String ufEmplacamento,
            @JsonProperty("registro_detran") String registroDetran,
            @JsonProperty("possui_gps") String possuiGps
    ) {}

    public record CredorInfo(
            @JsonProperty("nome") String nome,
            @JsonProperty("cnpj") String cnpj,
            @JsonProperty("endereco") String endereco,
            @JsonProperty("email") String email,
            @JsonProperty("telefone_contato") String telefoneContato,
            @JsonProperty("inscricao_estadual") String inscricaoEstadual
    ) {}

    public record DevedorInfo(
            @JsonProperty("nome") String nome,
            @JsonProperty("cpf_cnpj") String cpfCnpj,
            @JsonProperty("enderecos") List<EnderecoInfo> enderecos,
            @JsonProperty("contatos_email") List<EmailInfo> contatosEmail,
            @JsonProperty("contatos_telefones") List<TelefoneInfo> contatoTelefones
    ) {}

    public record GarantidorInfo(
            @JsonProperty("nome") String nome,
            @JsonProperty("cpf_cnpj") String cpfCnpj
    ) {}

    public record ContratoInfo(
            @JsonProperty("numero") String numero,
            @JsonProperty("protocolo") String protocolo,
            @JsonProperty("data_contrato") LocalDate dataContrato,
            @JsonProperty("data_pedido") LocalDate dataPedido,
            @JsonProperty("data_notificacao") LocalDate dataNotificacao,
            @JsonProperty("data_decurso_prazo") LocalDate dataDecursoPrazo,
            @JsonProperty("municipio_contrato") String municipioContrato,
            @JsonProperty("certidao_busca_apreensao") String certidaoBuscaApreensao,
            @JsonProperty("valor_divida") String valorDivida,
            @JsonProperty("valor_leilao") String valorLeilao,
            @JsonProperty("taxa_juros") String taxaJuros,
            @JsonProperty("valor_parcela") String valorParcela,
            @JsonProperty("quantidade_parcelas_pagas") Integer quantidadeParcelasPagas,
            @JsonProperty("quantidade_parcelas_abertas") Integer quantidadeParcelasAbertas,
            @JsonProperty("data_primeira_parcela") LocalDate dataPrimeiraParcela,
            @JsonProperty("descricao") String descricao,
            @JsonProperty("protocolo_ba") String protocoloBa,
            @JsonProperty("data_certidao") LocalDate dataCertidao,
            @JsonProperty("data_restricao") LocalDate dataRestricao,
            @JsonProperty("numero_restricao") String numeroRestricao,
            @JsonProperty("data_baixa_restricao") LocalDate dataBaixaRestricao,
            @JsonProperty("nsu") String nsu
    ) {}

    public record ServentiaInfo(
            @JsonProperty("cns") Integer cns,
            @JsonProperty("nome") String nome,
            @JsonProperty("endereco") String endereco,
            @JsonProperty("titular") String titular,
            @JsonProperty("substituto") String substituto
    ) {}

    public record EnderecoInfo(
            @JsonProperty("endereco") String endereco
    ) {}

    public record EmailInfo(
            @JsonProperty("email") String email
    ) {}

    public record TelefoneInfo(
            @JsonProperty("telefone") String telefone
    ){}
}