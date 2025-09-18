package com.montreal.integration.service.dbtranms;

import com.montreal.msiav_bh.dto.request.DetranMSSendSeizureNoticeRequest;
import com.montreal.msiav_bh.dto.response.DetranMsAuthenticateResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DetranMSIntegrationService {
    private final DetranMSAuthenticationService detranMSAuthenticationService;
    private final DetranMSSendSeizureNoticeService detranMSSendSeizureNoticeService;

    public String getToken() {
        DetranMsAuthenticateResponse response = detranMSAuthenticationService.getAuthenticate();
        return getTokenResponse(response);
    }

    public void sendSeizureNotice(String authorization, DetranMSSendSeizureNoticeRequest request) {
        detranMSSendSeizureNoticeService.send(authorization, request);
    }

    private String getTokenResponse(DetranMsAuthenticateResponse response) {
        if (response == null) {
            return StringUtils.EMPTY;
        }

        return StringUtils.isNotBlank(response.token()) ? response.token() : StringUtils.EMPTY;
    }
}
