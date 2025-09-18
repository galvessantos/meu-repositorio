package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeizureDateRequest {

	private Long id;

	@NotNull(message = "O ID do veículo associado é obrigatório.")
    private Long vehicleId;

    @NotNull(message = "A data de apreensão é obrigatória.")
    private LocalDateTime seizureDate;

    @NotNull(message = "A data de criação é obrigatória.")
    private LocalDateTime createdAt;
}
