package com.montreal.msiav_bh.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVehicleResponse extends PageableResponse {

    @Schema(description = "Conteudo da página")
    private List<VehicleResponse> content;

}
