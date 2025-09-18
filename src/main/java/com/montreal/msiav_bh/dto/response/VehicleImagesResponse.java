package com.montreal.msiav_bh.dto.response;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.VisionTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleImagesResponse {

    private Long id;
    private Long vehicleId;
    private Long vehicleSeizureId;
    private String name;
    private String originalSize;
    private String currentSize;
    private String imageType;
    private String imageUrl;
    private VisionTypeEnum visionType;
    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;
}
