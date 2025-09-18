package com.montreal.msiav_bh.service;


import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.response.VehicleCompanyResponse;
import com.montreal.msiav_bh.entity.VehicleCompany;
import com.montreal.msiav_bh.mapper.VehicleCompanyMapper;
import com.montreal.msiav_bh.repository.VehicleCompanyRepository;
import com.montreal.msiav_bh.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleCompanyService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCompanyMapper vehicleCompanyMapper;
    private final VehicleCompanyRepository vehicleCompanyRepository;

    public List<VehicleCompanyResponse> findByVehicleId(Long vehicleId) {
        log.info("Buscando empresas associadas ao veículo com ID: {}", vehicleId);
        List<VehicleCompanyResponse> list = new ArrayList<>();
        
        if (!vehicleRepository.existsById(vehicleId)) {
            log.error("Veículo com ID {} não encontrado.", vehicleId);
            throw new NotFoundException("Veículo não encontrado com ID: " + vehicleId);
        }

        // Buscar todas as associações do veículo com empresas
        List<VehicleCompany> vehicleCompanies = vehicleCompanyRepository.findByVehicleId(vehicleId);

        if (vehicleCompanies.isEmpty()) {
            log.warn("Nenhuma empresa associada ao veículo com ID: {}", vehicleId);
            throw new NotFoundException("Nenhuma empresa associada ao veículo com ID: " + vehicleId);
        }

        for(VehicleCompany vc: vehicleCompanies) {
        	list.add(vehicleCompanyMapper.toResponse(vc));
        }
        return list;
    }
}
