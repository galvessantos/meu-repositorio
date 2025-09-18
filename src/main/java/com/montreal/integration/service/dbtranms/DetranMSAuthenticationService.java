package com.montreal.integration.service.dbtranms;

import com.montreal.core.properties.DetranMSProperties;
import com.montreal.msiav_bh.client.DetranMSClient;
import com.montreal.msiav_bh.dto.request.DetranMsAuthenticateRequest;
import com.montreal.msiav_bh.dto.response.DetranMsAuthenticateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetranMSAuthenticationService {
    private final DetranMSProperties detranMSProperties;
    private final DetranMSClient detranMSClient;

    protected DetranMsAuthenticateResponse getAuthenticate() {
        try {
            DetranMsAuthenticateRequest request = new DetranMsAuthenticateRequest(detranMSProperties.getUsername(), detranMSProperties.getPassword());
            return detranMSClient.getAuthenticate(request);
        } catch (Exception e) {
            log.error("Failed to getAuthenticate with Detran MS. Error: {}", e.getMessage(), e);
            return DetranMsAuthenticateResponse.builder().errorMessage(e.getMessage()).build();
        }
    }
}
