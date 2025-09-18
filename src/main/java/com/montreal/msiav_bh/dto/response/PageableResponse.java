package com.montreal.msiav_bh.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageableResponse {

    @Schema(description = "Número da página atual")
    @JsonProperty("page_number")
    private Integer pageNumber;

    @Schema(description = "Número de elementos por página")
    @JsonProperty("page_size")
    private Integer pageSize;

    @Schema(description = "Total de elementos disponíveis")
    @JsonProperty("total_elements")
    private Long totalElements;
}
