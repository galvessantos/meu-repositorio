package com.montreal.msiav_bh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Detran {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sigla_uf")
    private String siglaUf;

    @Column(name = "nome_detran")
    private String nomeDetran;

    @Column(name = "estado")
    private String estado;

    @OneToOne
    @JoinColumn(name = "contrato_id")
    @JsonBackReference
    private Contract contrato;
}
