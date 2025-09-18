package com.montreal.oauth.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Builder
public class FunctionalityDTO {

    private  Long id;
    private  String name;
    private  String description;
    private  boolean checked;
    private  List<FunctionalityDTO> children ;

}
