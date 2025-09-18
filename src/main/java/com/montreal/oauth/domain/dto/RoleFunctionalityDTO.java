package com.montreal.oauth.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class RoleFunctionalityDTO {

    private Long functionalityId;
    private boolean checked;


}
