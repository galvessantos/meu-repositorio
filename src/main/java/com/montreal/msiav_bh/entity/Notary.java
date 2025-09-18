package com.montreal.msiav_bh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notary {
    @Id
    @
            GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cns;
    private String name;
    private String holder;
    private String substitute;
    private String address;
    private String contactPhone;

    @OneToOne
    @JoinColumn(name = "contrato_id")
    @JsonBackReference
    private Contract contrato;
}

