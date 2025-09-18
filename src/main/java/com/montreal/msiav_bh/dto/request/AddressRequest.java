package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    @NotBlank(message = "O código postal (CEP) é obrigatório.")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato 99999-999.")
    private String postalCode;

    @NotBlank(message = "O nome da rua é obrigatório.")
    private String street;

    @NotBlank(message = "O número do imóvel é obrigatório.")
    @Pattern(regexp = "\\d+", message = "O número do imóvel deve conter apenas dígitos.")
    private String number;

    @NotBlank(message = "O bairro é obrigatório.")
    private String neighborhood;

    private String complement;

    @NotBlank(message = "O estado é obrigatório.")
    private String state;

    @NotBlank(message = "A cidade é obrigatória.")
    private String city;

    private String note;
}
