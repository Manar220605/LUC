package com.luc.qa.module.mentorship.mapper;

import com.luc.qa.module.mentorship.dto.MentorshipRequestResponseDTO;
import com.luc.qa.module.mentorship.entity.MentorshipRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MentorshipRequestMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentDisplayName", source = "student.displayName")
    @Mapping(target = "studentAvatarUrl", source = "student.avatarUrl")
    @Mapping(target = "alumniId", source = "alumni.id")
    @Mapping(target = "alumniDisplayName", source = "alumni.displayName")
    @Mapping(target = "alumniAvatarUrl", source = "alumni.avatarUrl")
    @Mapping(target = "alumniLinkedinUrl", ignore = true)
    MentorshipRequestResponseDTO toResponse(MentorshipRequest request);
}
