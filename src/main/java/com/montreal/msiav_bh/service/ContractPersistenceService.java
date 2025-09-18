package com.montreal.msiav_bh.service;

import com.montreal.core.utils.CryptoUtil;
import com.montreal.msiav_bh.dto.response.QueryDetailResponseDTO;
import com.montreal.msiav_bh.entity.*;
import com.montreal.msiav_bh.mapper.ContractMapper;
import com.montreal.msiav_bh.repository.ContractRepository;
import com.montreal.msiav_bh.repository.CreditorRepository;
import com.montreal.msiav_bh.repository.VehicleCacheRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ContractPersistenceService {

    private final ContractRepository contractRepository;
    private final VehicleCacheRepository vehicleCacheRepository;
    private final ContractMapper contractMapper;
    private final CreditorRepository creditorRepository;
    private final CryptoUtil cryptoUtil;

    public ContractPersistenceService(ContractRepository contractRepository,
                                      VehicleCacheRepository vehicleCacheRepository,
                                      ContractMapper contractMapper, CreditorRepository creditorRepository, CryptoUtil cryptoUtil) {
        this.contractRepository = contractRepository;
        this.vehicleCacheRepository = vehicleCacheRepository;
        this.contractMapper = contractMapper;
        this.creditorRepository = creditorRepository;
        this.cryptoUtil = cryptoUtil;
    }

    @Transactional
    public Contract saveContract(QueryDetailResponseDTO response) {
        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalArgumentException("Resposta inválida da API");
        }

        // Verificar se o contrato já existe
        Optional<Contract> existingContract = Optional.empty();
        if (response.data().contrato() != null && response.data().contrato().numero() != null) {
            existingContract = contractRepository.findByNumero(response.data().contrato().numero());
        }

        // Buscar veículos EXISTENTES no banco
        List<VehicleCache> existingVehicles = new ArrayList<>();
        if (response.data().contrato() != null && response.data().contrato().numero() != null) {
            existingVehicles = vehicleCacheRepository.findAllByContrato(response.data().contrato().numero());
        }

        Contract contract;

        if (existingContract.isPresent()) {
            Contract existing = existingContract.get();
            Contract managedContract = contractRepository.findById(existing.getId())
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

            contract = contractMapper.toEntity(response.data(), existingVehicles, managedContract);

        } else {
            contract = contractMapper.toEntity(response.data(), existingVehicles, null);
        }

        // SALVAR O CONTRATO PRIMEIRO
        contract = contractRepository.save(contract);

        // ATUALIZAR OS VEÍCULOS EXISTENTES com dados da API
        if (response.data().veiculos() != null && !response.data().veiculos().isEmpty()) {
            updateVehiclesWithApiData(existingVehicles, response.data().veiculos(), contract, cryptoUtil);
        }

        return contract;
    }

    // Método para atualizar veículos EXISTENTES com dados da API
    private void updateVehiclesWithApiData(List<VehicleCache> existingVehicles,
                                           List<QueryDetailResponseDTO.Veiculo> apiVehicles,
                                           Contract contract, CryptoUtil cryptoUtil) {
        if (apiVehicles == null) return;

        for (QueryDetailResponseDTO.Veiculo apiVehicle : apiVehicles) {
            if (apiVehicle.placa() == null) continue;

            // CRIPTOGRAFAR a placa da API para comparar com o banco
            String placaCriptografada = cryptoUtil.encrypt(apiVehicle.placa());

            // Encontrar o veículo existente pela placa CRIPTOGRAFADA
            existingVehicles.stream()
                    .filter(existingVehicle -> placaCriptografada.equals(existingVehicle.getPlaca()))
                    .findFirst()
                    .ifPresent(existingVehicle -> {
                        // Atualizar os campos com dados da API
                        updateVehicleFieldsFromApi(existingVehicle, apiVehicle, cryptoUtil);
                        existingVehicle.setContratoEntity(contract);


                        // SALVAR as alterações no veículo
                        vehicleCacheRepository.save(existingVehicle);

                        log.info("Veículo atualizado: {}", apiVehicle.placa());
                    });
        }
    }

    // Método para vincular veículos EXISTENTES ao contrato
    private void linkExistingVehiclesToContract(List<VehicleCache> existingVehicles, Contract contract) {
        for (VehicleCache existingVehicle : existingVehicles) {
            // Apenas atualizar a referência ao contrato
            existingVehicle.setContratoEntity(contract);
            // Isso fará UPDATE, não INSERT
            vehicleCacheRepository.save(existingVehicle);
        }
    }

    // Método para atualizar campos específicos do veículo (com criptografia)
    private void updateVehicleFieldsFromApi(VehicleCache vehicle, QueryDetailResponseDTO.Veiculo apiVehicle, CryptoUtil cryptoUtil) {
        // Campos que NÃO precisam ser criptografados
        if (apiVehicle.marcaModelo() != null) {
            vehicle.setMarcaModelo(apiVehicle.marcaModelo());
        }
        if (apiVehicle.registroDetran() != null) {
            vehicle.setRegistroDetran(apiVehicle.registroDetran());
        }
        if (apiVehicle.possuiGps() != null) {
            vehicle.setPossuiGPS(Boolean.valueOf(apiVehicle.possuiGps()));
        }
        if (apiVehicle.anoFabricacao() != null) {
            vehicle.setAnoFabricacao(apiVehicle.anoFabricacao());
        }
        if (apiVehicle.anoModelo() != null) {
            vehicle.setAnoModelo(apiVehicle.anoModelo());
        }
        if (apiVehicle.cor() != null) {
            vehicle.setCor(apiVehicle.cor());
        }
        if (apiVehicle.chassi() != null) {
            vehicle.setChassi(cryptoUtil.encrypt(apiVehicle.chassi())); // Criptografar
        }
        if (apiVehicle.renavam() != null) {
            vehicle.setRenavam(cryptoUtil.encrypt(apiVehicle.renavam())); // Criptografar
        }
        if (apiVehicle.gravame() != null) {
            vehicle.setGravame(cryptoUtil.encrypt(apiVehicle.gravame().toString())); // Criptografar
        }
        if (apiVehicle.ufEmplacamento() != null) {
            vehicle.setUf(apiVehicle.ufEmplacamento());
        }
    }
}
