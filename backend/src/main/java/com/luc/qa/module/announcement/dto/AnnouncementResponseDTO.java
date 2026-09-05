package com.luc.qa.module.announcement.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnnouncementResponseDTO {

    private Long id;
    private String title;
    private String body;
    private boolean published;
    private Instant createdAt;
    private Instant updatedAt;
}
