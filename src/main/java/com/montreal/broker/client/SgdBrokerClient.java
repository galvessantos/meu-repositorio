package com.montreal.broker.client;

import com.montreal.broker.dto.enumerations.SendTypeEnum;
import com.montreal.broker.dto.request.DigitalSendRequest;
import com.montreal.broker.dto.request.TokenRequest;
import com.montreal.broker.dto.response.DigitalSendResponse;
import com.montreal.broker.dto.response.TokenResponse;
import com.montreal.broker.properties.SgdBrokenProperties;
import com.montreal.core.domain.exception.ClientServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SgdBrokerClient {

    private static final String LOAD_SEND_DIGITAL_URL = "%s/loadDadosEnvioDigital";
    private static final String TOKEN_URL = "%s/signin";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final SgdBrokenProperties properties;
    private final RestTemplate restTemplate;

    public DigitalSendResponse loadSendDigital(DigitalSendRequest request) {
        log.info("Enviando dados para notificação do tipo {}", SendTypeEnum.valueOf(request.getSendType()));

        try {

            var token = getToken();

            var headers = new HttpHeaders();
            headers.set(CONTENT_TYPE, APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<DigitalSendRequest> entity = new HttpEntity<>(request, headers);

            var uri = String.format(LOAD_SEND_DIGITAL_URL, properties.getUrl());
            ResponseEntity<DigitalSendResponse> response = restTemplate.exchange(
                    uri, HttpMethod.POST, entity, DigitalSendResponse.class);

            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ClientServiceException("Resposta do serviço está vazia ou inválida."));

        } catch (ClientServiceException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new ClientServiceException("Erro ao se conectar ao serviço de notificação. Verifique a disponibilidade do serviço e a conectividade de rede.", e);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new ClientServiceException(String.format("Erro ao enviar dados para notificacão - falha %s", e.getMessage()), e);
        }

    }

    private String getToken() {
        log.info("Obtendo token de autenticação");

        try {

            var url = String.format(TOKEN_URL, properties.getUrl());

            var body = TokenRequest.builder().email(properties.getEmail())
                    .password(properties.getPassword())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.ALL));

            HttpEntity<TokenRequest> request = new HttpEntity<>(body, headers);

            var response = restTemplate.exchange(url, HttpMethod.POST, request, TokenResponse.class);

            log.info("Dados do response {}", response.getBody());

            return Optional.ofNullable(response.getBody())
                        .map(TokenResponse::getAccessToken)
                        .orElseThrow(() -> new ClientServiceException("Token não encontrado na resposta do serviço."));

        } catch (ResourceAccessException e) {
            throw new ClientServiceException("Erro ao se conectar ao serviço de notificação. Verifique a disponibilidade do serviço e a conectividade de rede.", e);

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new ClientServiceException(String.format("Erro ao enviar dados para notificacão - falha %s", e.getMessage()), e);

        }

    }

}
