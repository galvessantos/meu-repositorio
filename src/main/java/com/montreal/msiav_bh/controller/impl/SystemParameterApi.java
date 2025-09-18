package com.montreal.msiav_bh.controller.impl;

import com.montreal.msiav_bh.controller.ISystemParameterApi;
import com.montreal.msiav_bh.dto.SystemParameterDTO;
import com.montreal.msiav_bh.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SystemParameterApi implements ISystemParameterApi {

    private final SystemParameterService service;

    @Override
    public List<SystemParameterDTO> listAll() {
        return service.listAll();
    }

    @Override
    public SystemParameterDTO create(SystemParameterDTO systemParameterDTO) {
        return service.create(systemParameterDTO);
    }

}
