package com.montreal.msiav_bh.dto;

import lombok.Data;

import java.util.List;

@Data
public class DebtorDataDTO {
    private String nome;
    private String cpf_cnpj;
    private List<ContactEmailDTO> contatos_email;
    private List<ContactPhoneDTO> contatos_telefone;
    private List<AddressDTO> enderecos;
}
