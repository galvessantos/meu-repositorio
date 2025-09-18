package com.montreal.msiav_bh.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero")
    private String numero;

    @Column(name = "protocolo")
    private String protocolo;

    @Column(name = "data_contrato")
    private LocalDate dataContrato;

    @Column(name = "data_pedido")
    private LocalDate dataPedido;

    @Column(name = "data_notificacao")
    private LocalDate dataNotificacao;

    @Column(name = "data_decurso_prazo")
    private LocalDate dataDecursoPrazo;

    @Column(name = "municipio_contrato")
    private String municipioContrato;

    @Column(name = "valor_divida")
    private String valorDivida;

    @Column(name = "valor_leilao")
    private String valorLeilao;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor_parcela")
    private String valorParcela;

    @Column(name = "taxa_juros")
    private String taxaJuros;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "certidao_busca_apreensao", columnDefinition = "TEXT")
    private String certidaoBuscaApreensao;

    @Column(name = "data_primeira_parcela")
    private LocalDate dataPrimeiraParcela;

    @Column(name = "quantidade_parcelas_pagas")
    private Integer quantidadeParcelasPagas;

    @Column(name = "quantidade_parcelas_abertas")
    private Integer quantidadeParcelasAbertas;

    @Column(name = "protocolo_ba")
    private String protocoloBa;

    @Column(name = "data_certidao")
    private LocalDate dataCertidao;

    @Column(name = "data_restricao")
    private LocalDate dataRestricao;

    @Column(name = "numero_restricao")
    private String numeroRestricao;

    @Column(name = "data_baixa_restricao")
    private LocalDate dataBaixaRestricao;

    @Column(name = "nsu")
    private String nsu;

    @Column(name = "indicador_restricao_circulacao")
    private String indicadorRestricaoCirculacao;

    // Relacionamentos
    @OneToOne(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Creditor credor;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Debtor> devedores = new ArrayList<>();

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Guarantors> garantidores = new ArrayList<>();

    @OneToMany(mappedBy = "contratoEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleCache> veiculos = new ArrayList<>();


    @OneToOne(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Notary serventia;

    @OneToOne(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Detran detran;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Agency> orgaos = new ArrayList<>();

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Notification> notificacoes = new ArrayList<>();

}
