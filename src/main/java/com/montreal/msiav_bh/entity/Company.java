package com.montreal.msiav_bh.entity;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;
import com.montreal.msiav_bh.enumerations.PhoneTypeEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da empresa é obrigatório.")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Endereço de email da empresa é obrigatório")
    @Email(message = "Endereço de email inválido")
    @Column(nullable = false, unique = false)
    private String email;

    @NotBlank(message = "Documento da empresa é obrigatório")
    @Column(nullable = false, unique = true)
    private String document;

    @Column(nullable = true, length = 2)
    private String phoneDDD;

    @Column(nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PhoneTypeEnum phoneType;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @Column(nullable = false)
    private String nameResponsible;

    @NotNull(message = "Tipo da empresa é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyTypeEnum companyType;

    @NotNull(message = "Estado de atividade da empresa é obrigatório")
    @Column(nullable = false)
    private Boolean isActive;



    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private String stateRegistration;

    @Column(nullable = true)
    private String bank;

    @Column(nullable = true)
    private String agency;

    @Column(nullable = true)
    private String account;

    @Column(nullable = true)
    private String pixType;

    @Column(nullable = true)
    private String pixKey;

    @Column(nullable = true)
    private String notification;
}