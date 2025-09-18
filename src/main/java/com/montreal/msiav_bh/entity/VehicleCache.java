package com.montreal.msiav_bh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle_cache",
        indexes = {
                @Index(name = "idx_placa", columnList = "placa"),
                @Index(name = "idx_contrato", columnList = "contrato"),
                @Index(name = "idx_protocolo", columnList = "protocolo"),
                @Index(name = "idx_api_sync_date", columnList = "api_sync_date"),
                @Index(name = "idx_contrato_hash", columnList = "contrato_hash"),
                @Index(name = "idx_placa_hash", columnList = "placa_hash"),
                @Index(name = "idx_contrato_placa_hash", columnList = "contrato_placa_hash")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_contrato_hash", columnNames = {"contrato_hash"}),
                @UniqueConstraint(name = "unique_placa_hash", columnNames = {"placa_hash"}),
                @UniqueConstraint(name = "unique_contrato_placa_hash", columnNames = {"contrato_placa_hash"})
        })
public class VehicleCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private Long externalId;

    @Column(name = "credor")
    private String credor;

    @Column(name = "data_pedido")
    private LocalDate dataPedido;

    @Column(name = "contrato", columnDefinition = "TEXT")
    @JsonIgnore
    private String contrato;

    @Column(name = "placa", columnDefinition = "TEXT")
    @JsonIgnore
    private String placa;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "uf")
    private String uf;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "cpf_devedor")
    private String cpfDevedor;

    @Column(name = "protocolo")
    private String protocolo;

    @Column(name = "etapa_atual")
    private String etapaAtual;

    @Column(name = "status_apreensao")
    private String statusApreensao;

    @Column(name = "ultima_movimentacao")
    private LocalDateTime ultimaMovimentacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "api_sync_date")
    private LocalDateTime apiSyncDate;

    @Column(name = "contrato_hash", length = 64)
    private String contratoHash;

    @Column(name = "placa_hash", length = 64)
    private String placaHash;

    @Column(name = "contrato_placa_hash", length = 64)
    private String contratoPlacaHash;

    @Column(name = "marca_modelo")
    private String marcaModelo;

    @Column(name = "registro_detran")
    private String registroDetran;

    @Column(name = "possui_gps")
    private Boolean possuiGPS;

    @Column(name = "ano_fabricacao")
    private String anoFabricacao;

    @Column(name = "ano_modelo")
    private String anoModelo;

    @Column(name = "cor")
    private String cor;

    @Column(name = "chassi")
    private String chassi;

    @Column(name = "renavam")
    private String renavam;

    @Column(name = "gravame")
    private String gravame;

    @ManyToOne
    @JoinColumn(name = "contrato_entity_id")
    @JsonBackReference
    private Contract contratoEntity;
}