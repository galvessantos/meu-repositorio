package com.montreal.oauth.domain.enumerations;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Tipos de autenticação de dois fatores disponíveis")
public enum TwoFactorTypeEnum {

    @Schema(description = "Autenticação via SMS")
    SMS,

    @Schema(description = "Autenticação via Email")
    EMAIL

}