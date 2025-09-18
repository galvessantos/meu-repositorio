package com.montreal.msiav_bh.controller;

import com.montreal.msiav_bh.dto.response.ContractDetails;
import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.service.ApiQueryService;
import com.montreal.msiav_bh.service.ContractPersistenceService;
import com.montreal.msiav_bh.service.ContractService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contract")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class ContractController {

    private final ApiQueryService apiQueryService;
    private final ContractPersistenceService contractPersistenceService;
    private final ContractService contractService;

    @GetMapping("/search/{vehicleID}")
    public ResponseEntity<?> searchContract(@PathVariable Long vehicleID) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            // executa a chamada da API em thread separada
            Future<ContractDetails> future = executor.submit(() -> apiQueryService.searchContract(vehicleID));


            ContractDetails response = future.get(10, TimeUnit.SECONDS);
            return ResponseEntity.ok(response);

        } catch (TimeoutException e) {

            VehicleCache vehicleCache = contractService.getVehicleWithContract(vehicleID);

            if (vehicleCache != null && vehicleCache.getContrato() != null) {
                return ResponseEntity.ok(vehicleCache.getContrato());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Contrato não encontrado no banco"));
            }

        } catch (HttpClientErrorException.NotFound ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Contrato não encontrado na API"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erro interno ao processar a requisição"));
        } finally {
            executor.shutdown();
        }
    }



    @GetMapping("/search-save/{vehicleID}")
    public ResponseEntity<?> searchAndSaveContract(@PathVariable Long vehicleID) {
        try {
            ContractDetails response = apiQueryService.searchContract(vehicleID);
            contractPersistenceService.saveContract(response.getDadosApi());
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException.NotFound ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Contrato não encontrado"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erro interno ao processar a requisição"));
        }
    }

}
