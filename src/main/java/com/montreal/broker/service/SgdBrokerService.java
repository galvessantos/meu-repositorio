package com.montreal.broker.service;

import com.montreal.broker.client.SgdBrokerClient;
import com.montreal.broker.dto.enumerations.SendTypeEnum;
import com.montreal.broker.dto.request.DigitalSendRequest;
import com.montreal.broker.dto.response.DigitalSendResponse;
import com.montreal.core.domain.exception.SgdBrokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class SgdBrokerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
    );

    public static final Pattern PHONE_PATTERN = Pattern.compile("^55\\d{2}9\\d{8}$");

    private final SgdBrokerClient sgdBrokerClient;

    public DigitalSendResponse sendNotification(DigitalSendRequest request) {
        log.info("Enviando dados para notificação do tipo {}", SendTypeEnum.valueOf(request.getSendType()));

        validate(request);

        return sgdBrokerClient.loadSendDigital(request);
    }


    public void validate(DigitalSendRequest request) {
        var sendType = SendTypeEnum.valueOf(request.getSendType());

        if(SendTypeEnum.EMAIL.equals(sendType)) {
            validateFieldByEmail(request);

        } else if(SendTypeEnum.SMS.equals(sendType)) {
            validateFieldsSms(request);
        }

    }

    private void validateFieldByEmail(DigitalSendRequest request) {
        List<String> errors = new ArrayList<>();

        if (isBlank(request.getRecipient())) {
            errors.add("Campo destinatário é obrigatório.");
        } else if (!EMAIL_PATTERN.matcher(request.getRecipient()).matches()) {
            errors.add("E-mail não válido.");
        }

        if (isBlank(request.getSubject())) {
            errors.add("Campo assunto é obrigatório.");
        }

        if (isBlank(request.getSender())) {
            errors.add("Campo remetente é obrigatório.");
        }

        if (isBlank(request.getTemplate())) {
            errors.add("Campo template é obrigatório.");
        }

        if (!errors.isEmpty()) {
            throw new SgdBrokenException(String.join("\n", errors));
        }

    }

    private void validateFieldsSms(DigitalSendRequest request) {
        List<String> errors = new ArrayList<>();

        if (isBlank(request.getPhoneNumber())) {
            errors.add("Campo telefone é obrigatório.");
        } else if (!PHONE_PATTERN.matcher(request.getPhoneNumber()).matches()) {
            errors.add("Formato de telefone inválido. Deve começar com '55' seguido pelo DDD, '9' e mais 8 dígitos.");
        }

        if (isBlank(request.getTemplate())) {
            errors.add("Campo template é obrigatório.");
        }

        if (!errors.isEmpty()) {
            throw new SgdBrokenException(String.join("\n", errors));
        }

    }


    private boolean isBlank(String value) {
        return !hasText(value);
    }

}
