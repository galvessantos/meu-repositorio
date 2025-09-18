package com.montreal.msiav_bh.dto;

import com.montreal.oauth.domain.dto.RoleFunctionalityDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleFunctionalityUpdateRequest  {

    private List<RoleFunctionalityDTO> roles;


}