package com.luc.qa.module.announcement.mapper;

import com.luc.qa.module.announcement.dto.AnnouncementResponseDTO;
import com.luc.qa.module.announcement.entity.Announcement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnnouncementMapper {

    AnnouncementResponseDTO toResponse(Announcement announcement);
}
