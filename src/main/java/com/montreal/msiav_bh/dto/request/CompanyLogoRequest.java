package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLogoRequest {

    @NotNull(message = "O ID da empresa (Mongo) é obrigatório.")
    private Long companyIdMongo;

    @NotBlank(message = "A imagem da empresa é obrigatória.")
    private String companyImage;
}
