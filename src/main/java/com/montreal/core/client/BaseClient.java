package com.montreal.core.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.montreal.core.domain.exception.ClientServiceException;
import com.montreal.core.domain.exception.InternalErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

import static com.montreal.core.config.SSLConfig.createInsecureSslContext;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseClient {

    public static final String FAILURE_TO_EXECUTE_CALL = "Falha ao executar chamada [%s] com Status %s - Response %s";
    public static final String AUTHORIZATION = "Authorization";
    public static final String X_CSRF_TOKEN = "ZXbOdTqngXEWXLpsOKx8y5myKWNfcdFk7WYGbblp";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(50);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(20);

    protected final ObjectMapper mapper;

    /**
     * Executes a POST request and returns the response.
     *
     * @param url            The URL of the request.
     * @param requestObject  The object to be sent as the request body.
     * @param token          The authorization token for the request. Can be null.
     * @param responseType   The class type of the response.
     * @param <T>            The type of the response.
     * @return The response object of type T.
     * @throws Exception     if an error occurs while executing the request.
     */
    protected <T> T executePostRequest(String url, Object requestObject, String token, TypeReference<T> responseType) throws Exception {

        try {

            var requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestObject);
            var uri = URI.create(url);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("accept", "*/*")
                    .header("Content-Type", "application/json")
                    .header("X-CSRF-TOKEN", X_CSRF_TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            if (token != null) {
                requestBuilder.header(AUTHORIZATION, "Bearer " + token);
            }

            var httpRequest = requestBuilder.build();
            var response = executeHttpRequest(httpRequest, url);

            return mapper.readValue(response.body(), responseType);

        } catch (ClientServiceException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar chamada POST {}", url, e);
            throw new ClientServiceException(String.format("Erro ao executar chamada POST %s", url), e);
        }

    }


    /**
     * Executes a GET request and returns the response.
     *
     * @param url          The URL of the request.
     * @param token        The authorization token for the request. Can be null.
     * @param responseType The class type of the response.
     * @param <T>          The type of the response.
     * @return The response object of type T.
     * @throws Exception if an error occurs while executing the request.
     */
    protected <T> T executeGetRequest(String url, String token, TypeReference<T> responseType) throws Exception {

        var uri = URI.create(url);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .header("accept", "*/*")
                .header("X-CSRF-TOKEN", X_CSRF_TOKEN)
                .header("Content-Type", "application/json")
                .timeout(REQUEST_TIMEOUT)
                .GET();

        if (token != null) {
            requestBuilder.header(AUTHORIZATION, "Bearer " + token);
        }

        var httpRequest = requestBuilder.build();
        var response = executeHttpRequest(httpRequest, url);

        return mapper.readValue(response.body(), responseType);
    }


    /**
     * Executes an HTTP request and returns the response.
     *
     * @param httpRequest The HTTP request to be executed.
     * @param urlRequest  The URL of the request.
     * @return The HTTP response.
     * @throws IOException            If an I/O error occurs while sending or receiving the request.
     * @throws InterruptedException If the thread is interrupted while sending or receiving the request.
     */
    private HttpResponse<String> executeHttpRequest(HttpRequest httpRequest, String urlRequest) throws Exception {

        SSLContext sslContext = createInsecureSslContext();

        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        log.info("Response: {} - Status: {}", response, response.statusCode());

        if (Objects.requireNonNull(HttpStatus.resolve(response.statusCode())).isError()) {
            throw new ClientServiceException(String.format(FAILURE_TO_EXECUTE_CALL, urlRequest, response.statusCode(), response.body()));
        }

        return response;
    }

    protected void handleException(Exception e, String url) {

        if (e instanceof ClientServiceException clientServiceException) {
            throw clientServiceException;
        }

        throw new InternalErrorException(String.format("Falha ao executar chamada [%s]", url), e);

    }

}
