package com.montreal.integration.service.dbtranms;

import com.montreal.core.properties.DetranMSProperties;
import com.montreal.msiav_bh.client.DetranMSClient;
import com.montreal.msiav_bh.dto.request.DetranMsAuthenticateRequest;
import com.montreal.msiav_bh.dto.response.DetranMsAuthenticateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetranMSAuthenticationServiceTest {

    @Mock
    private DetranMSProperties detranMSProperties;

    @Mock
    private DetranMSClient detranMSClient;

    @InjectMocks
    private DetranMSAuthenticationService service;

    @Test
    @DisplayName("Deve autenticar com sucesso quando as credenciais são válidas")
    void shouldAuthenticateSuccessfully() {
        String username = "usuarioTeste";
        String password = "senhaTeste";

        DetranMsAuthenticateResponse expectedResponse = DetranMsAuthenticateResponse.builder()
                .token("token-valido")
                .build();

        when(detranMSProperties.getUsername()).thenReturn(username);
        when(detranMSProperties.getPassword()).thenReturn(password);
        when(detranMSClient.getAuthenticate(argThat(request ->
                request.username().equals(username) &&
                        request.password().equals(password))))
                .thenReturn(expectedResponse);

        DetranMsAuthenticateResponse actualResponse = service.getAuthenticate();

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.token()).isEqualTo("token-valido");

        ArgumentCaptor<DetranMsAuthenticateRequest> requestCaptor = ArgumentCaptor.forClass(DetranMsAuthenticateRequest.class);
        verify(detranMSClient).getAuthenticate(requestCaptor.capture());

        DetranMsAuthenticateRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.username()).isEqualTo(username);
        assertThat(capturedRequest.password()).isEqualTo(password);
    }

    @Test
    @DisplayName("Deve propagar resposta de erro quando o cliente retorna erro")
    void shouldPropagateErrorResponseWhenClientReturnsError() {
        String username = "usuarioTeste";
        String password = "senhaTeste";

        DetranMsAuthenticateResponse errorResponse = DetranMsAuthenticateResponse.builder()
                .errorMessage("Credenciais inválidas")
                .build();

        when(detranMSProperties.getUsername()).thenReturn(username);
        when(detranMSProperties.getPassword()).thenReturn(password);
        when(detranMSClient.getAuthenticate(argThat(request ->
                request.username().equals(username) &&
                        request.password().equals(password))))
                .thenReturn(errorResponse);

        DetranMsAuthenticateResponse actualResponse = service.getAuthenticate();

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.errorMessage()).isEqualTo("Credenciais inválidas");

        ArgumentCaptor<DetranMsAuthenticateRequest> requestCaptor = ArgumentCaptor.forClass(DetranMsAuthenticateRequest.class);
        verify(detranMSClient).getAuthenticate(requestCaptor.capture());

        DetranMsAuthenticateRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.username()).isEqualTo(username);
        assertThat(capturedRequest.password()).isEqualTo(password);
    }
}