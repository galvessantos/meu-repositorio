package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.FileReportDTO;
import com.montreal.msiav_bh.dto.request.FileReportRequest;
import com.montreal.msiav_bh.dto.response.FileReportResponse;
import com.montreal.msiav_bh.entity.FileReport;

@Mapper(componentModel = "spring", uses = ReportMapper.class)
public interface FileReportMapper {

    FileReportMapper INSTANCE = Mappers.getMapper(FileReportMapper.class);

    @Mapping(target = "id", ignore = true)
    FileReport toEntity(FileReportRequest request);

    FileReportResponse toResponse(FileReport entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(FileReportRequest request, @MappingTarget FileReport entity);

    FileReport toEntity(FileReportDTO dto);

    FileReportDTO toDto(FileReport entity);

    List<FileReportResponse> toResponseList(List<FileReport> entities);

    List<FileReportDTO> toDtoList(List<FileReport> entities);
}
