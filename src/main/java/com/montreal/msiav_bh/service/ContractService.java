package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.dto.response.QueryDetailResponseDTO;
import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.repository.ContractRepository;
import com.montreal.msiav_bh.repository.VehicleCacheRepository;
import org.springframework.stereotype.Service;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final VehicleCacheRepository vehicleCacheRepository;

    public ContractService(ContractRepository contractRepository, VehicleCacheRepository vehicleCacheRepository) {
        this.contractRepository = contractRepository;
        this.vehicleCacheRepository = vehicleCacheRepository;
    }

    public void saveContractDataBase(QueryDetailResponseDTO dto) {

        dto.data().contrato();
    }

    public VehicleCache getVehicleWithContract(Long vehicleId) {
        return vehicleCacheRepository.findContractById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
    }



}
