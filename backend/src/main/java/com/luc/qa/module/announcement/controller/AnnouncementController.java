package com.luc.qa.module.announcement.controller;

import com.luc.qa.module.announcement.dto.AnnouncementResponseDTO;
import com.luc.qa.module.announcement.mapper.AnnouncementMapper;
import com.luc.qa.module.announcement.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Tag(name = "Announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final AnnouncementMapper announcementMapper;

    @GetMapping
    @Operation(
        summary = "List published announcements",
        description = "Returns all published announcements, newest first. Public endpoint."
    )
    public List<AnnouncementResponseDTO> list() {
        return announcementService.listPublished().stream()
            .map(announcementMapper::toResponse)
            .toList();
    }
}
