package com.montreal.integration.service.dbtranms;

import com.montreal.msiav_bh.client.DetranMSClient;
import com.montreal.msiav_bh.dto.request.DetranMSSendSeizureNoticeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DetranMSSendSeizureNoticeServiceTest {

    @Mock
    private DetranMSClient detranMSClient;

    @InjectMocks
    private DetranMSSendSeizureNoticeService service;


    @Test
    @DisplayName("Deve enviar notificação de apreensão com sucesso")
    void shouldSendSeizureNoticeSuccessfully() {
        String authorization = "Bearer token123";
        DetranMSSendSeizureNoticeRequest request = mock(DetranMSSendSeizureNoticeRequest.class);

        service.send(authorization, request);

        verify(detranMSClient).sendSeizureNotice(authorization, request);
        verifyNoMoreInteractions(detranMSClient);
    }
}