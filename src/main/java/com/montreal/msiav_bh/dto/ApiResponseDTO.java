package com.montreal.msiav_bh.dto;

import lombok.Data;

@Data
public class ApiResponseDTO {
    private boolean success;
    private ApiDataDTO data;
    private String message;
}
