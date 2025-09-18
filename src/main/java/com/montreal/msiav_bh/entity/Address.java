package com.montreal.msiav_bh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "address", uniqueConstraints = {
        @UniqueConstraint(name = "unique_address", columnNames = {"postal_code", "street", "number", "neighborhood", "city"})
})
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O código postal (CEP) é obrigatório.")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato 99999-999.")
    @Column(name = "postal_code", nullable = false, length = 9)
    private String postalCode;

    @NotBlank(message = "O nome da rua é obrigatório.")
    @Column(nullable = false, length = 255)
    private String street;

    @NotBlank(message = "O número do imóvel é obrigatório.")
    @Pattern(regexp = "\\d+", message = "O número do imóvel deve conter apenas dígitos.")
    @Column(nullable = false, length = 10)
    private String number;

    @NotBlank(message = "O bairro é obrigatório.")
    @Column(nullable = false, length = 100)
    private String neighborhood;

    @Column(length = 255)
    private String complement;

    @NotBlank(message = "O estado é obrigatório.")
    @Column(nullable = false, length = 50)
    private String state;

    @NotBlank(message = "A cidade é obrigatória.")
    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 500)
    private String note;
}

