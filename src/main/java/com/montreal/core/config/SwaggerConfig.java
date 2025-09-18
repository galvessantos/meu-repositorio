package com.montreal.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${msiav.openapi.local-url}")
    private String localUrl;

    @Value("${msiav.openapi.dev-url}")
    private String devUrl;

    @Value("${msiav.openapi.hml-url}")
    private String hmlUrl;

    @Value("${msiav.openapi.prod-url}")
    private String prodUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers());
    }

    private Info apiInfo() {
        return new Info()
                .title("MSIAV API")
                .version("1.0")
                .contact(apiContact())
                .description("Esta API fornece rotas para MSIAV")
                .license(apiLicense());
    }

    private Contact apiContact() {
        return new Contact()
                .name("Montreal")
                .email("contato@montreal.com.br")
                .url("https://www.montreal.com.br");
    }

    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://www.montreal.com.br/");
    }

    private List<Server> apiServers() {
        List<Server> servers = new ArrayList<>();

        if ("dev".equalsIgnoreCase(activeProfile)) {
            servers.add(createServer(devUrl, "Development environment"));
        } else if ("local".equalsIgnoreCase(activeProfile)) {
            servers.add(createServer(localUrl, "Localhost environment"));
        } else if ("hml".equalsIgnoreCase(activeProfile)) {
        	servers.add(createServer(hmlUrl, "Homologation environment"));
        } else if ("prod".equalsIgnoreCase(activeProfile)) {
            servers.add(createServer(prodUrl, "Production environment"));
        }

        return servers;
    }

    private Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }
}
