package com.montreal.oauth.domain.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FunctionalityResponseDTO {

    private Long id;
    private String name;
    private String description;
    private boolean checked;
    private List<FunctionalityResponseDTO> functionalityResponseDTOList;
    private LocalDateTime createdAt;
}

