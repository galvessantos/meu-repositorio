package com.montreal.broker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSendRequest {

    @JsonProperty("clienteId")
    private Integer clientTypeId;

    @JsonProperty("tipoEnvio")
    private Integer sendType;

    @JsonProperty("remetente")
    private String sender;

    @JsonProperty("assunto")
    private String subject;

    @JsonProperty("destinatario")
    private String recipient;

    @JsonProperty("telefone")
    private String phoneNumber;

    @JsonProperty("template")
    private String template;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("indicesDadosTemplate")
    private String templateDataIndices;

    @JsonProperty("nomeAnexo")
    private String attachmentName;

    @JsonProperty("anexo")
    private String attachment;

    @JsonProperty("idPedido")
    private String orderId;

    @JsonProperty("carimboEnvio")
    private boolean sendTimestamp;

    @JsonProperty("carimboEntrega")
    private boolean deliveryTimestamp;

    @JsonProperty("carimboAbertura")
    private boolean openTimestamp;

}
