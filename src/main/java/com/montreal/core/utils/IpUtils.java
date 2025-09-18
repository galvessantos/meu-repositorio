package com.montreal.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpUtils {

    /**
     * Recupera o endereço IP real do cliente, verificando os cabeçalhos HTTP que podem conter o IP original
     * quando a aplicação está atrás de um proxy ou balanceador de carga.
     *
     * @param request HttpServletRequest da requisição atual.
     * @return O endereço IP do cliente.
     */
    public static String getClientIp(HttpServletRequest request) {

        try {

            String[] headers = {
                    "X-Forwarded-For",
                    "Proxy-Client-IP",
                    "WL-Proxy-Client-IP",
                    "HTTP_X_FORWARDED_FOR",
                    "HTTP_X_FORWARDED",
                    "HTTP_X_CLUSTER_CLIENT_IP",
                    "HTTP_CLIENT_IP",
                    "HTTP_FORWARDED_FOR",
                    "HTTP_FORWARDED",
                    "HTTP_VIA",
                    "REMOTE_ADDR"
            };

            for (String header : headers) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip.split(",")[0];
                }
            }

            return request.getRemoteAddr();

        } catch (Exception e) {
            log.warn("Erro ao recuperar o endereço IP do cliente: {}", e.getMessage());
        }

        return "";

    }

}

