package com.montreal.msiav_bh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "witnesses")
public class Witnesses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da testemunha não pode ser nulo ou vazio.")
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank(message = "O RG da testemunha não pode ser nulo ou vazio.")
    @Pattern(regexp = "\\d{7,12}", message = "O RG deve conter entre 7 e 12 dígitos numéricos.")
    @Column(nullable = false, length = 12, unique = true)
    private String rg;

    @NotBlank(message = "O CPF da testemunha não pode ser nulo ou vazio.")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00.")
    @Column(nullable = false, length = 14, unique = true)
    private String cpf;
}
