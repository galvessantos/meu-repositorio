package com.montreal.msiav_bh.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.montreal.core.domain.exception.NegocioException;
import com.montreal.core.exception_handler.ProblemType;
import com.montreal.msiav_bh.dto.SystemParameterDTO;
import com.montreal.msiav_bh.entity.SystemParameter;
import com.montreal.msiav_bh.repository.SystemParameterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemParameterService {

    public final SystemParameterRepository repository;

    public SystemParameterDTO getSystemParameter(String system, String parameter) {
        return repository.findBySystemAndParameter(system, parameter)
                .map(this::getSystemParameterDTO)
                .orElseThrow(() -> new NegocioException(ProblemType.DADOS_INVALIDOS, "System parameter not found"));
    }

    public List<SystemParameterDTO> listAll() {
        return repository.findAll().stream()
                .map(this::getSystemParameterDTO)
                .toList();
    }

    public SystemParameterDTO create(SystemParameterDTO systemParameterDTO) {
        SystemParameter systemParameter = SystemParameter.builder()
                .system(systemParameterDTO.getSystem())
                .parameter(systemParameterDTO.getParameter())
                .value(systemParameterDTO.getValue())
                .build();
        return getSystemParameterDTO(repository.save(systemParameter));
    }

    private SystemParameterDTO getSystemParameterDTO(SystemParameter systemParameter) {
        return SystemParameterDTO.builder()
                .id(systemParameter.getId())
                .system(systemParameter.getSystem())
                .parameter(systemParameter.getParameter())
                .value(systemParameter.getValue())
                .build();
    }

}

