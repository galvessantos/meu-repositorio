package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.ReportDTO;
import com.montreal.msiav_bh.dto.request.ReportRequest;
import com.montreal.msiav_bh.dto.response.ReportResponse;
import com.montreal.msiav_bh.entity.Report;

@Mapper(componentModel = "spring", uses = {SeizureDateMapper.class, WitnessesMapper.class})
public interface ReportMapper {

    ReportMapper INSTANCE = Mappers.getMapper(ReportMapper.class);

    @Mapping(target = "id", ignore = true)
    Report toEntity(ReportRequest request);

    ReportResponse toResponse(Report entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ReportRequest request, @MappingTarget Report entity);

    Report toEntity(ReportDTO dto);

    ReportDTO toDto(Report entity);

    List<ReportResponse> toResponseList(List<Report> entities);

    List<ReportDTO> toDtoList(List<Report> entities);
}
