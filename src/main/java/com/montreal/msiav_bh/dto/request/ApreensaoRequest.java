package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ApreensaoRequest {
    @NotNull
    private LocalDateTime dataHoraApreensao;
}
