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
        description = "Data inicial da pequisa",
        name = "startDate",
        schema = @Schema(type = "LocalDate", example = "2024-02-02")
)
@Parameter(
        in = ParameterIn.QUERY,
        description = "Data final da pequisa",
        name = "endDate",
        schema = @Schema(type = "LocalDate", example = "2024-02-02")
)
public @interface ContractFilterRequestAsQueryParam {
}
