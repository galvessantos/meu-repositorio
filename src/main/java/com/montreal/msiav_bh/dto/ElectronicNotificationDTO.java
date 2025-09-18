package com.montreal.msiav_bh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ElectronicNotificationDTO {

    @JsonProperty("communicationMethod")
    private String communicationMethod;

    @JsonProperty("sentDate")
    private LocalDate sentDate;

    @JsonProperty("readDate")
    private LocalDate readDate;

    @JsonProperty("fileEvidence")
    private String fileEvidence;
}