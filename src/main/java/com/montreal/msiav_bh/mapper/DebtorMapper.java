package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.DebtorDTO;
import com.montreal.msiav_bh.dto.request.DebtorRequest;
import com.montreal.msiav_bh.dto.response.DebtorResponse;
import com.montreal.msiav_bh.entity.Debtor;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface DebtorMapper {

    DebtorMapper INSTANCE = Mappers.getMapper(DebtorMapper.class);

    @Mapping(target = "id", ignore = true)
    Debtor toEntity(DebtorRequest request);

    DebtorResponse toResponse(Debtor entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(DebtorRequest request, @MappingTarget Debtor entity);

    Debtor toEntity(DebtorDTO dto);

    DebtorDTO toDto(Debtor entity);

    List<DebtorResponse> toResponseList(List<Debtor> entities);

    List<DebtorDTO> toDtoList(List<Debtor> entities);
}
