package com.montreal.msiav_bh.dto;

import lombok.Builder;
import org.springframework.core.io.Resource;

@Builder
public record FileDownloadDTO(
        Long fileSize,
        String fileName,
        Resource file
) {
}
