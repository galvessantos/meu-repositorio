package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record DetranMSSendSeizureNoticeRequest(
        @JsonProperty("numeroSequencial") Integer nsu,
        @JsonProperty("estadoVeiculo") int vehicleCondition,
        @JsonProperty("dataApreensao")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z", message = "A data deve estar no formato YYYY-MM-DDTHH:MM:SS.sssZ")
        String seizureDate,
        @JsonProperty("arquivo") String file
) {
}
