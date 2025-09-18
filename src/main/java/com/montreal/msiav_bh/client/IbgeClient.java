package com.montreal.msiav_bh.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.montreal.core.client.BaseClient;
import com.montreal.msiav_bh.dto.CityDTO;
import com.montreal.msiav_bh.dto.StateDTO;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class IbgeClient extends BaseClient {

    private static final String LOCATIONS_URL = "https://servicodados.ibge.gov.br/api/v1/localidades/estados";
    private static final String CITY_URL = "https://servicodados.ibge.gov.br/api/v1/localidades/estados/%s/municipios";

    public IbgeClient( ObjectMapper mapper) {
        super(mapper);
    }

    public List<StateDTO> getStates() {

        try {
            return executeGetRequest(LOCATIONS_URL, null, new TypeReference<List<StateDTO>>(){});
        } catch (Exception e) {
            handleException(e, LOCATIONS_URL);
            return null;
        }

    }

    public List<CityDTO> getCities(String codeState) {

        try {
            var url = String.format(CITY_URL, codeState);
            return executeGetRequest(url, null, new TypeReference<List<CityDTO>>(){});
        } catch (Exception e) {
            handleException(e, LOCATIONS_URL);
            return null;
        }

    }

}
