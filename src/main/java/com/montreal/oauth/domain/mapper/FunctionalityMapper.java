package com.montreal.oauth.domain.mapper;

import com.montreal.oauth.domain.dto.FunctionalityDTO;
import com.montreal.oauth.domain.dto.response.FunctionalityResponseDTO;
import com.montreal.oauth.domain.entity.Functionality;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FunctionalityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Functionality toEntity(FunctionalityDTO dto);

    FunctionalityResponseDTO toResponseDTO(Functionality functionality);

    List<FunctionalityResponseDTO> toResponseDTOList(List<Functionality> functionalities);

    List<FunctionalityDTO> toDTOList(List<Functionality> functionalities);
}
