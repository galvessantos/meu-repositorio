package com.montreal.msiav_bh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "system_parameter")
public class SystemParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @NotBlank(message = "O nome do sistema é obrigatório.")
    @Column(nullable = false, length = 100)
    private String system; // Nome do sistema ao qual o parâmetro pertence

    @NotBlank(message = "O nome do parâmetro é obrigatório.")
    @Column(nullable = false, length = 100)
    private String parameter; // Nome do parâmetro

    @NotBlank(message = "O valor do parâmetro é obrigatório.")
    @Column(nullable = false, length = 255)
    private String value; // Valor do parâmetro
}
