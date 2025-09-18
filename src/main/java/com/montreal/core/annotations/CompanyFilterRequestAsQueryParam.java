package com.montreal.core.annotations;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
        in = ParameterIn.QUERY,
        description = "Tipo de empresa",
        name = "companyType",
        schema = @Schema(type = "CompanyTypeEnum", allowableValues = {"DADOS_ESCRITORIO_COBRANCA", "DADOS_LOCALIZADOR", "DADOS_GUINCHO", "DADOS_PATIO"})
)
@Parameter(
        in = ParameterIn.QUERY,
        description = "Nome da empresa",
        name = "name",
        schema = @Schema(type = "String", example = "Empresa x")
)
@Parameter(
        in = ParameterIn.QUERY,
        description = "Documento da empresa",
        name = "document",
        schema = @Schema(type = "String", example = "12345678901234")
)
public @interface CompanyFilterRequestAsQueryParam {
}
