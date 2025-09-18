package com.montreal.core.config;

import com.montreal.core.interceptor.ClientIpInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final ClientIpInterceptor clientIpInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Iniciando configuração de CORS...");
        registry.addMapping("/**") // Habilita CORS para todas as rotas
                .allowedOrigins("http://localhost")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                .allowedHeaders("*") // Permitir todos os headers
                .exposedHeaders("Authorization", "Content-Disposition") // Headers expostos
                .allowCredentials(true) // Permitir envio de cookies/autenticação
                .maxAge(3600); // Tempo de cache das configurações de CORS em segundos
        log.info("Configuração de CORS aplicada com sucesso!");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientIpInterceptor).addPathPatterns("/**");
    }

}
