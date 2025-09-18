package com.montreal.integration.controller;

import com.montreal.integration.response.IntegrationDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Tag(name = "Integration API", description = "API de integração com outros sistemas")
public interface IIntegrationApi {

    @GetMapping("/integration/{numberContract}")
    @Operation(summary = "Recupera os dados de integração por número de contrato",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dados de integração recuperados com sucesso",
                            content = @Content(schema = @Schema(implementation = IntegrationDataResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Acesso não autorizado"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Dados de integração não encontrados"
                    )
            }
    )
    IntegrationDataResponse getIntegrationData(@PathVariable String numberContract);

}
