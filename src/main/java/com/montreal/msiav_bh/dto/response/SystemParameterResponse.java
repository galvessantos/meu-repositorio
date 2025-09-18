package com.montreal.msiav_bh.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemParameterResponse {
	
    @JsonProperty("id")
    private Long id;

    @JsonProperty("system")
    private String system;

    @JsonProperty("parameter")
    private String parameter;

    @JsonProperty("value")
    private String value;
}
