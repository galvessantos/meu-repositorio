package com.montreal.msiav_bh.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVehicleAssociationRequest {

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    @NotNull(message = "O ID do veículo é obrigatório.")
    private Long vehicleId;

    @NotNull(message = "O ID do usuário responsável pelo vínculo é obrigatório.")
    private Long associatedById;

    @NotNull(message = "A data de criação é obrigatória.")
    private LocalDateTime createdAt;

}
