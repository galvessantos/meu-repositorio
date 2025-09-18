package com.montreal.broker.components;

import com.montreal.broker.dto.enumerations.ClientTypeEnum;
import com.montreal.broker.dto.enumerations.SendTypeEnum;
import com.montreal.broker.dto.request.DigitalSendRequest;
import com.montreal.broker.properties.SgdBrokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SgdBrokerComponent {

    public static final String SENDER_MAIL = "esteira-reply@montreal.com.br";
    private final SgdBrokenProperties properties;

    public DigitalSendRequest createTypeSms(String template, String phoneNumber) {
        return DigitalSendRequest.builder()
                .clientTypeId(ClientTypeEnum.GUARANTEE.getDescription())
                .sendType(SendTypeEnum.SMS.getDescription())
                .template(template)
                .phoneNumber(phoneNumber)
                .build();
    }

    public DigitalSendRequest createTypeEmail(String subject, String template, String recipient) {
        return DigitalSendRequest.builder()
                .clientTypeId(ClientTypeEnum.GUARANTEE.getDescription())
                .sendType(SendTypeEnum.EMAIL.getDescription())
                .subject(subject)
                .template(template)
                .recipient(recipient)
                .sender(Optional.ofNullable(properties.getSender()).orElse(SENDER_MAIL))
                .build();
    }

}
