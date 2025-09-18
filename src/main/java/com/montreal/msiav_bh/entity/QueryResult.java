package com.montreal.msiav_bh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class QueryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_apreensao")
    private String statusApreensao;


    private LocalDateTime dataHoraApreensao;

    private LocalDateTime dataUltimaMovimentacao;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = true)
    private Address address;


    @Column(name = "agendamento_apreensao")
    private LocalDateTime agendamentoApreensao;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private VehicleCache vehicle;


}
