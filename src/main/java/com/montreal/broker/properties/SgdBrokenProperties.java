package com.montreal.broker.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties("sgd.broken")
public class SgdBrokenProperties {

    private String url;
    private String sender;
    private String email;
    private String password;

}
