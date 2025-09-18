package com.montreal.integration.service.dbtranms;

import com.montreal.msiav_bh.dto.request.DetranMSSendSeizureNoticeRequest;
import com.montreal.msiav_bh.dto.response.DetranMsAuthenticateResponse;
import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetranMSIntegrationServiceTest {

    @Mock
    private DetranMSAuthenticationService detranMSAuthenticationService;

    @Mock
    private DetranMSSendSeizureNoticeService detranMSSendSeizureNoticeService;

    @InjectMocks
    private DetranMSIntegrationService detranMSIntegrationService;

    @Test
    @DisplayName("Deve retornar token formatado quando autenticação for bem-sucedida")
    void shouldReturnFormattedTokenWhenAuthenticationIsSuccessful() {
        String tokenValue = "abc123";
        DetranMsAuthenticateResponse authResponse = DetranMsAuthenticateResponse.builder()
                .token(tokenValue)
                .build();

        when(detranMSAuthenticationService.getAuthenticate()).thenReturn(authResponse);

        String result = detranMSIntegrationService.getToken();

        assertThat(result).isEqualTo(tokenValue);
        verify(detranMSAuthenticationService).getAuthenticate();
    }

    @Test
    @DisplayName("Deve retornar string vazia quando token for nulo")
    void shouldReturnEmptyStringWhenTokenIsNull() {
        DetranMsAuthenticateResponse authResponse = DetranMsAuthenticateResponse.builder()
                .token(null)
                .build();
        when(detranMSAuthenticationService.getAuthenticate()).thenReturn(authResponse);

        String result = detranMSIntegrationService.getToken();

        assertThat(result).isEmpty();
        verify(detranMSAuthenticationService).getAuthenticate();
    }

    @Test
    @DisplayName("Deve retornar string vazia quando corpo da resposta for nulo")
    void shouldReturnEmptyStringWhenResponseBodyIsNull() {
        when(detranMSAuthenticationService.getAuthenticate()).thenReturn(null);

        String result = detranMSIntegrationService.getToken();

        assertThat(result).isEmpty();
        verify(detranMSAuthenticationService).getAuthenticate();
    }

    @Test
    @DisplayName("Deve enviar notificação de apreensão com autorização e requisição corretas")
    void shouldSendSeizureNoticeWithCorrectAuthorizationAndRequest() {
        String authorization = "Bearer token123";
        DetranMSSendSeizureNoticeRequest request = DetranMSSendSeizureNoticeRequest.builder()
                .nsu(1)
                .vehicleCondition(VehicleConditionEnum.BOM.getKey())
                .seizureDate("2025-01-02T19:41:36.409Z")
                .file("file-content")
                .build();

        detranMSIntegrationService.sendSeizureNotice(authorization, request);

        verify(detranMSSendSeizureNoticeService).send(authorization, request);
    }
}