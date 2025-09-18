package com.montreal.core.domain.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties("file")
public class FileProperties {

    private String uploadDir;

    /**
     * Tamanho máximo do upload (em bytes). Ex: 5MB = 5 * 1024 * 1024 = 5242880
     */
    private long maxUploadSizeInBytes = 5242880; // valor default de 5MB
}
