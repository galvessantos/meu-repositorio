package com.montreal.msiav_bh.controller;

import com.montreal.msiav_bh.dto.SystemParameterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Parametros do Sistema")
public interface ISystemParameterApi {

    @GetMapping("/system-parameter")
    @Operation(summary = "Listar todos os parâmetros do sistema",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Parâmetros recuperados com sucesso",
                            content = @Content(schema = @Schema(implementation = SystemParameterDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Acesso não autorizado"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Parâmetros não foram encontrados"
                    )
            }
    )
    List<SystemParameterDTO> listAll();


    @PostMapping("/system-parameter")
    @Operation(summary = "Criar um novo parâmetro do sistema",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Parâmetro criado com sucesso",
                            content = @Content(schema = @Schema(implementation = SystemParameterDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Acesso não autorizado"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Parâmetros não criado"
                    )
            }
    )
    SystemParameterDTO create(@RequestBody SystemParameterDTO systemParameterDTO);

}
