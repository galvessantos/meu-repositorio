package com.montreal.core.domain.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDataDTO {

    private String name;
    private String originalSize;
    private String imageType;
    private String imageUrl;

}
