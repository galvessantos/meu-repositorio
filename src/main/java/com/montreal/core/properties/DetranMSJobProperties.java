package com.montreal.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "detran.ms.job")
public class DetranMSJobProperties {
    private Boolean enabled;
    private String cron;
}
