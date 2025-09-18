package com.montreal.integration.controller.impl;

import com.montreal.integration.controller.IIntegrationApi;
import com.montreal.integration.response.IntegrationDataResponse;
import com.montreal.integration.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IntegrationApi implements IIntegrationApi {

    private final IntegrationService integrationService;

    @Override
    public IntegrationDataResponse getIntegrationData(String numberContract) {
        return integrationService.getIntegrationData(numberContract);
    }

}
