package com.luc.qa.module.alumni.mapper;

import com.luc.qa.module.alumni.dto.AdminVerificationResponseDTO;
import com.luc.qa.module.alumni.dto.AlumniProfileResponseDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.entity.AlumniVerification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlumniVerificationMapper {

    VerificationResponseDTO toResponse(AlumniVerification verification);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userDisplayName", source = "user.displayName")
    @Mapping(target = "userEmail", source = "user.email")
    AdminVerificationResponseDTO toAdminResponse(AlumniVerification verification);
}
