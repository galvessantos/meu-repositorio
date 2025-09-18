package com.montreal.msiav_bh.dto;

import com.montreal.msiav_bh.enumerations.VisionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleImagesDTO {

    private Long id;
    private Long vehicleId;
    private Long vehicleSeizureId;
    private String name;
    private String originalSize;
    private String imageType;
    private String imageUrl;
    private VisionTypeEnum visionType;

}
