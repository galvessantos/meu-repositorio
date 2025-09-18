package com.montreal.broker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSendResponse {

    @JsonProperty("idEnvio")
    private Long sendId;

    @JsonProperty("to")
    private String recipient;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

}
