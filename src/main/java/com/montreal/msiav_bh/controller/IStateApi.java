package com.montreal.msiav_bh.controller;

import com.montreal.msiav_bh.dto.CityDTO;
import com.montreal.msiav_bh.dto.StateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Estados")
public interface IStateApi {

    @GetMapping("/state")
    @Operation(summary = "Listar todos os estados",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Estados recuperados com sucesso",
                            content = @Content(schema = @Schema(implementation = StateDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Acesso não autorizado"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Estados não foram encontrados"
                    )
            }
    )
    List<StateDTO> listAll();

    @GetMapping("/state-city/{codeState}")
    @Operation(summary = "Listar todos os municipios de um estado",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Municipios recuperados com sucesso",
                            content = @Content(schema = @Schema(implementation = StateDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Acesso não autorizado"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Municipios não foram encontrados"
                    )
            }
    )
    List<CityDTO> listCityAll(@PathVariable String codeState);

}
