package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.dto.SeizureNoticeDTO;
import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;
import com.montreal.msiav_bh.repository.VehicleSeizureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleSeizureService {
    private final VehicleSeizureRepository vehicleSeizureRepository;

    @Transactional
    public List<SeizureNoticeDTO> getSeizureNoticeListByStageAndStatus(VehicleStageEnum vehicleStageEnum, SeizureStatusEnum seizureStatusEnum) {
        return vehicleSeizureRepository.findSeizureNoticeToSend(vehicleStageEnum, seizureStatusEnum);
    }

    @Transactional
    public void updateVehicleSeizureStatus(Long vehicleSeizureId, SeizureStatusEnum status) {
        vehicleSeizureRepository.findById(vehicleSeizureId)
                .ifPresent(vehicleSeizure -> {
                    vehicleSeizure.setStatus(status);
                    vehicleSeizureRepository.save(vehicleSeizure);
                });
    }
}
