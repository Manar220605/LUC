package com.luc.qa.module.moderation.mapper;

import com.luc.qa.module.moderation.dto.ReportResponseDTO;
import com.luc.qa.module.moderation.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    ReportResponseDTO toResponse(Report report);
}
