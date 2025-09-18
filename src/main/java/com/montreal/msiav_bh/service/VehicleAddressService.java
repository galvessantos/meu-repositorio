package com.montreal.msiav_bh.service;

import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.entity.VehicleAddress;
import com.montreal.msiav_bh.repository.VehicleAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleAddressService {

    private final VehicleAddressRepository vehicleAddressRepository;
    
    public List<VehicleAddress> findByVehicleId(Long vehicleId) {
        log.info("Buscando endereços associados ao veículo com ID: {}", vehicleId);
        
        List<VehicleAddress> vehicleAddresses = vehicleAddressRepository.findByVehicleId(vehicleId);
        
        if (vehicleAddresses.isEmpty()) {
            log.warn("Nenhuma associação de endereço encontrada para o veículo ID: {}", vehicleId);
            throw new NotFoundException("Nenhuma associação de endereço encontrada para o veículo ID: " + vehicleId);
        }
        return vehicleAddresses;
    }

}
