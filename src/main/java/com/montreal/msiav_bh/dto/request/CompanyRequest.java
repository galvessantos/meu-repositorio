package com.montreal.msiav_bh.dto.request;

import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;
import com.montreal.msiav_bh.enumerations.PhoneTypeEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRequest {

    private Long id;

    @NotBlank(message = "O nome da empresa é obrigatório.")
    private String name;

    @NotBlank(message = "Endereço de email da empresa é obrigatório.")
    @Email(message = "Endereço de email inválido.")
    private String email;

    @NotBlank(message = "Documento da empresa é obrigatório.")
    private String document;

    private String phoneDDD;

    private String phoneNumber;

    private PhoneTypeEnum phoneType;

    @NotNull(message = "Endereço é obrigatório.")
    private AddressRequest address;

    private String nameResponsible;

    @NotNull(message = "Tipo da empresa é obrigatório.")
    private CompanyTypeEnum companyType;

    @NotNull(message = "Estado de atividade da empresa é obrigatório.")
    private Boolean isActive;

    private String companyLogo;

    private String stateRegistration;
    private String bank;
    private String agency;
    private String account;
    private String pixType;
    private String pixKey;
    private String notification;
}
