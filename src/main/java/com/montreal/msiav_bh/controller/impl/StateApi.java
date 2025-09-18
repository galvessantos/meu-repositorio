package com.montreal.msiav_bh.controller.impl;

import com.montreal.msiav_bh.client.IbgeClient;
import com.montreal.msiav_bh.controller.IStateApi;
import com.montreal.msiav_bh.dto.CityDTO;
import com.montreal.msiav_bh.dto.StateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StateApi implements IStateApi {

    private final IbgeClient client;

    @Override
    public List<StateDTO> listAll() {
        return client.getStates();
    }

    @Override
    public List<CityDTO> listCityAll(String codeState) {
        return client.getCities(codeState);
    }

}
