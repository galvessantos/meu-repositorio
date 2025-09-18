package com.montreal.msiav_bh.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.montreal.core.client.BaseClient;
import com.montreal.core.properties.DetranMSProperties;
import com.montreal.msiav_bh.dto.request.DetranMsAuthenticateRequest;
import com.montreal.msiav_bh.dto.request.DetranMSSendSeizureNoticeRequest;
import com.montreal.msiav_bh.dto.response.DetranMsAuthenticateResponse;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class DetranMSClient extends BaseClient {
    private final DetranMSProperties detranMSProperties;

    public DetranMSClient(ObjectMapper mapper, DetranMSProperties detranMSProperties) {
        super(mapper);
        this.detranMSProperties = detranMSProperties;
    }

    @SneakyThrows
    public DetranMsAuthenticateResponse getAuthenticate(DetranMsAuthenticateRequest request) {
        String url = detranMSProperties.getUrl() + "/usuario/authenticate";
        return executePostRequest(url, request, null, new TypeReference<>() {});
    }

    @SneakyThrows
    public void sendSeizureNotice(String authorization, DetranMSSendSeizureNoticeRequest request) {
        String url = detranMSProperties.getUrl() + "/solicitacao/enviar-auto-apreensao";
        executePostRequest(url, request, authorization, new TypeReference<>(){});
    }
}
