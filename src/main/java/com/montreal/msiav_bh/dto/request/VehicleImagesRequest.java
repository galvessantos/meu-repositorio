package com.montreal.msiav_bh.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.entity.VehicleSeizure;
import com.montreal.msiav_bh.enumerations.VisionTypeEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleImagesRequest {
    private Long id;
    private Vehicle vehicle;
    private VehicleSeizure vehicleSeizure;
    private String name;
    private String originalSize;
    private String currentSize;
    private String imageType;
    private String imageUrl;
    private VisionTypeEnum visionType;
    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;
}
