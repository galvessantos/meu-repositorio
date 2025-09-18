package com.montreal.msiav_bh.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "montreal.api")
public class ApiQueryConfig {

    private String baseUrl;
    private String username;
    private String password;
    private long tokenRefreshInterval = 8000;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getTokenRefreshInterval() { return tokenRefreshInterval; }
    public void setTokenRefreshInterval(long tokenRefreshInterval) { this.tokenRefreshInterval = tokenRefreshInterval; }
}