package com.montreal.core.domain.service;

import com.montreal.broker.components.SgdBrokerComponent;
import com.montreal.broker.service.SgdBrokerService;
import com.montreal.core.domain.component.EmailComponent;
import com.montreal.core.domain.exception.ClientServiceException;
import com.montreal.core.domain.exception.EmailException;
import com.montreal.core.domain.exception.SgdBrokenException;
import com.montreal.core.properties.UiHubProperties;
import com.montreal.oauth.domain.entity.UserInfo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SgdBrokerComponent sgdBrokerComponent;
    private final SgdBrokerService sgdBrokerService;
    private final UiHubProperties uiHubProperties;

    public void sendPasswordResetEmail(String to, String token) {
        log.info("Iniciando envio de e-mail de redefinição de senha para {}", to);

        String resetUrl = UriComponentsBuilder.fromHttpUrl(uiHubProperties.getUrl())
                .path("/reset-password")
                .queryParam("token", token)
                .toUriString();

        String subject = "Redefinição de Senha";
        String emailBodyAsHtml = EmailComponent.getTemplatePasswordReset(resetUrl);

        sendEmailWithFallback(to, subject, emailBodyAsHtml);
    }

    public String getTamplate(String templateName, String name, String link) {

        StringBuilder headBuilder = new StringBuilder();
        StringBuilder footerBuilder = new StringBuilder();
        StringBuilder contentBuilder = new StringBuilder();

        try {

            BufferedReader inHead = new BufferedReader(new FileReader("C:\\PROJETO\\RELEASE\\MONTREAL-GESTAO-GARANTIAS-BACKEND\\src\\main\\java\\com\\montreal\\core\\templates\\shared\\head.html"));
            BufferedReader inContent = new BufferedReader(new FileReader("C:\\PROJETO\\RELEASE\\MONTREAL-GESTAO-GARANTIAS-BACKEND\\src\\main\\java\\com\\montreal\\core\\templates\\" + templateName + ".html"));
            BufferedReader inFooter = new BufferedReader(new FileReader("C:\\PROJETO\\RELEASE\\MONTREAL-GESTAO-GARANTIAS-BACKEND\\src\\main\\java\\com\\montreal\\core\\templates\\shared\\footer.html"));
            String strHead;
            String strFooter;
            String strContent;
            while ((strHead = inHead.readLine()) != null) {
                headBuilder.append(strHead);
            }
            while ((strContent = inContent.readLine()) != null) {
                contentBuilder.append(strContent);
            }
            while ((strFooter = inFooter.readLine()) != null) {
                footerBuilder.append(strFooter);
            }
            inHead.close();
            inContent.close();
            inFooter.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String head = headBuilder.toString();
        String footer = footerBuilder.toString();

        String content = contentBuilder.toString();
        content = content.replace("${HEAD}", head);
        content = content.replace("${FOOTER}", footer);
        content = content.replace("${NAME}", name);
        content = content.replace("${LINK}", link);
        return content;
    }

    public void sendEmailFromTemplate(String name, String link, String recipient) throws MessagingException {
        MimeMessage message = this.mailSender.createMimeMessage();
        String template = getTamplate("forgot-password", name, link);
        String subject = "Recuperação de senha";
        String from = "suporte@montreal.com.br";
        Map<String, Object> variables = new HashMap<>();
        variables.put("NAME", "WELL");
        variables.put("LINK", link);

        try {
            message.setSubject(subject);
            message.setContent(template, "text/html; charset=utf-8");
            message.setRecipients(MimeMessage.RecipientType.TO, recipient);
            message.setFrom(from);
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEmailRegistrationConfirmation(UserInfo userInfo) {
        log.info("Enviando e-mail de confirmação de cadastro para {}", userInfo.getEmail());
        try {
            var linkRegister = uiHubProperties.getUrl() + "cadastrarSenha/" + userInfo.getId();
            var linkLogin = uiHubProperties.getUrl() + "login/";
            String subject = "Confirmação de Cadastro";
            var template = EmailComponent.getTemplateEmailNewUser(userInfo.getUsername(), userInfo.getFullName(), linkRegister, linkLogin);

            sendEmailWithFallback(userInfo.getEmail(), subject, template);

        } catch (Exception e) {
            throw new EmailException("Erro ao enviar e-mail de confirmação de cadastro", e);
        }
    }

    private void sendEmailWithFallback(String to, String subject, String htmlBody) {
        try {
            // Tenta enviar com o serviço principal (SgdBroker)
            log.info("Tentando enviar e-mail para {} via SgdBrokerService.", to);
            var digitalSendRequest = sgdBrokerComponent.createTypeEmail(subject, htmlBody, to);
            var digitalSendResponse = sgdBrokerService.sendNotification(digitalSendRequest);
            log.info("E-mail enviado com sucesso para {} via SgdBrokerService. Código de envio: {}", to, digitalSendResponse.getSendId());
        } catch (ClientServiceException | SgdBrokenException ex) {
            // Se o serviço principal falhar, usa o fallback (JavaMailSender)
            log.warn("Falha ao enviar e-mail via SgdBrokerService: {}. Tentando fallback com JavaMailSender.", ex.getMessage());
            try {
                MimeMessage message = mailSender.createMimeMessage();
                message.setFrom("noreplymontrealaceleramaker@gmail.com");
                message.setRecipients(MimeMessage.RecipientType.TO, to);
                message.setSubject(subject);
                message.setContent(htmlBody, "text/html; charset=utf-8");
                mailSender.send(message);
                log.info("E-mail enviado com sucesso para {} via fallback (JavaMailSender).", to);
            } catch (MessagingException e) {
                throw new EmailException("Falha no envio de e-mail via SgdBrokerService e também no fallback com JavaMailSender.", e);
            }
        }
    }
}
