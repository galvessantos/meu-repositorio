package com.montreal.msiav_bh.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record StorageApiResponse(
        String id,
        @JsonProperty("nameFile") String fileName,
        String url,
        @JsonProperty("typeFile") String fileType,
        String source,
        String product,
        @JsonProperty("dataUpload") LocalDateTime uploadDate,
        @JsonProperty("storageTierEnum") String tier
) {
}
