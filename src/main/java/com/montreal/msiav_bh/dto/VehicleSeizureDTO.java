package com.montreal.msiav_bh.dto;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleSeizureDTO {

    private Long id;

    @NotNull(message = "O campo userId é obrigatório.")
    @Min(value = 1, message = "O campo userId deve ser maior que 0.")
    private Integer userId; // Identificador do usuário associado

    @NotNull(message = "O campo vehicleId é obrigatório.")
    private Long vehicleId; // Identificador do veículo

    @NotNull(message = "O campo addressId é obrigatório.")
    private Long addressId; // Identificador do endereço associado

    @NotNull(message = "O campo companyId é obrigatório.")
    private Long companyId; // Identificador da empresa

    @NotNull(message = "O campo vehicleCondition é obrigatório.")
    private VehicleConditionEnum vehicleCondition;

    @NotNull(message = "O campo seizureDate é obrigatório.")
    private LocalDateTime seizureDate; // Data da apreensão

    private LocalDateTime createdAt; // Data de criação do registro (normalmente gerada automaticamente)

    @Size(max = 500, message = "A descrição pode ter no máximo 500 caracteres.")
    private String description; // Descrição opcional
    
    private SeizureStatusEnum status; 
    
}
