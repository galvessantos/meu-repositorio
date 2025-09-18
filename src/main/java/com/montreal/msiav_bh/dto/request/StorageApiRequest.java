package com.montreal.msiav_bh.dto.request;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record StorageApiRequest(
        MultipartFile file,
        String fileName,
        String fileType,
        String source,
        String product,
        String tier
) {
}
