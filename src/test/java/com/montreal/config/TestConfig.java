package com.montreal.config;

import com.montreal.broker.dto.response.DigitalSendResponse;
import com.montreal.broker.service.SgdBrokerService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public SgdBrokerService sgdBrokerServiceMock() {
        SgdBrokerService mock = Mockito.mock(SgdBrokerService.class);
        Mockito.when(mock.sendNotification(Mockito.any())).thenReturn(new DigitalSendResponse());
        return mock;
    }
}