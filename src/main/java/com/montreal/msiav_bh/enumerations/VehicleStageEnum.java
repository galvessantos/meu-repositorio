package com.montreal.msiav_bh.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * Etapa do veiculo
 */
@Getter
@AllArgsConstructor
public enum VehicleStageEnum {

    CERTIDAO_BUSCA_APREENSAO_EMITIDA("Certidão Busca Apreensão Emitida"),
    BUSCA_PELO_VEICULO("Busca pelo Veículo"),
    RECOLHIMENTO_DO_VEICULO("Recolhimento do Veículo"),
    VEICULO_RECOLHIDO("Veículo Recolhido"),
    AGENTE_OFICIAL_ACIONADO ("Agente Oficial Acionado"),
    APREENSAO_CONCLUIDA ("Apreensão Concluida"),
    CANCELAMENTO_PEDIDO_CREDOR("Cancelamento do pedido pelo credor");

    private final String description;

    public static Optional<VehicleStageEnum> fromDescription(String description) {
        for (VehicleStageEnum value : VehicleStageEnum.values()) {
            if (value.getDescription().equals(description)) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

}
