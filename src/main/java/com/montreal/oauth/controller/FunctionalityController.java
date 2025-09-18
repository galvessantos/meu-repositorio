package com.montreal.oauth.controller;

import com.montreal.oauth.domain.dto.FunctionalityDTO;
import com.montreal.oauth.domain.dto.response.FunctionalityResponseDTO;
import com.montreal.oauth.domain.entity.Functionality;
import com.montreal.oauth.domain.mapper.FunctionalityMapper;
import com.montreal.oauth.domain.service.FunctionalityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/functionalities")
@Tag(name = "Funcionalidades", description = "API para gerenciamento das funcionalidades do sistema.")
@RequiredArgsConstructor
public class FunctionalityController {

    private final FunctionalityService functionalityService;
    private final FunctionalityMapper functionalityMapper;

    @Operation(
        summary = "Lista todas as funcionalidades",
        description = "Retorna uma lista de todas as funcionalidades disponíveis no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de funcionalidades retornada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = FunctionalityResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping
    public ResponseEntity<List<FunctionalityResponseDTO>> findAll() {
        log.info("Buscando todas as funcionalidades.");
        List<FunctionalityResponseDTO> functionalities = functionalityMapper.toResponseDTOList(functionalityService.findAll());
        return ResponseEntity.ok(functionalities);
    }

    @Operation(
        summary = "Busca funcionalidade pelo nome",
        description = "Retorna os detalhes de uma funcionalidade específica com base no nome."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Funcionalidade encontrada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = FunctionalityResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Funcionalidade não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<FunctionalityResponseDTO> findByName(
            @Parameter(description = "Nome da funcionalidade a ser buscada", required = true)
            @PathVariable String name) {
        log.info("Buscando funcionalidade com nome: {}", name);
        FunctionalityResponseDTO responseDTO = functionalityService.findByName(name);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
        summary = "Cria uma nova funcionalidade",
        description = "Adiciona uma nova funcionalidade ao sistema, associando-a a uma descrição."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Funcionalidade criada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = FunctionalityResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping
    public ResponseEntity<FunctionalityResponseDTO> create(
            @Parameter(description = "Objeto contendo os dados da nova funcionalidade", required = true)
            @Valid @RequestBody FunctionalityDTO dto) {
        log.info("Criando funcionalidade: {}", dto.getName());

        Functionality functionality = functionalityService.create(dto);
        FunctionalityResponseDTO responseDTO = functionalityMapper.toResponseDTO(functionality);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
        summary = "Atualiza uma funcionalidade existente",
        description = "Modifica os detalhes de uma funcionalidade com base no ID fornecido."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Funcionalidade atualizada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = FunctionalityResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Funcionalidade não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<FunctionalityResponseDTO> update(
            @Parameter(description = "ID da funcionalidade a ser atualizada", required = true)
            @PathVariable Long id,
            @Parameter(description = "Objeto contendo os novos dados da funcionalidade", required = true)
            @Valid @RequestBody FunctionalityDTO dto) {
        log.info("Atualizando funcionalidade com ID: {}", id);

        Functionality updatedFunctionality = functionalityService.update(id, dto);
        FunctionalityResponseDTO responseDTO = functionalityMapper.toResponseDTO(updatedFunctionality);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
        summary = "Exclui uma funcionalidade",
        description = "Remove uma funcionalidade do sistema permanentemente com base no seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Funcionalidade excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Funcionalidade não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID da funcionalidade a ser excluída", required = true)
            @PathVariable Long id) {
        log.info("Deletando funcionalidade com ID: {}", id);
        functionalityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}
