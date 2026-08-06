package com.luc.qa.module.alumni.mapper;

import com.luc.qa.module.alumni.dto.AlumniProfileResponseDTO;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlumniProfileMapper {

    @Mapping(target = "isPublic", source = "publicProfile")
    AlumniProfileResponseDTO toResponse(AlumniProfile profile);
}
