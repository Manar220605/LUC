package com.luc.qa.module.announcement.controller;

import com.luc.qa.module.announcement.dto.AnnouncementResponseDTO;
import com.luc.qa.module.announcement.dto.CreateAnnouncementRequestDTO;
import com.luc.qa.module.announcement.dto.UpdateAnnouncementRequestDTO;
import com.luc.qa.module.announcement.mapper.AnnouncementMapper;
import com.luc.qa.module.announcement.service.AdminAnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Announcements")
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;
    private final AnnouncementMapper announcementMapper;

    @GetMapping
    @Operation(
        summary = "List all announcements (admin)",
        description = "Returns every announcement including drafts, newest first. Requires ADMIN role."
    )
    public List<AnnouncementResponseDTO> list() {
        return adminAnnouncementService.listAll().stream()
            .map(announcementMapper::toResponse)
            .toList();
    }

    @PostMapping
    @Operation(
        summary = "Create announcement (admin)",
        description = "Creates a new announcement. Requires ADMIN role."
    )
    public ResponseEntity<AnnouncementResponseDTO> create(@Valid @RequestBody CreateAnnouncementRequestDTO request) {
        var created = adminAnnouncementService.create(request);
        return ResponseEntity
            .created(URI.create("/api/announcements"))
            .body(announcementMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update announcement (admin)",
        description = "Updates an existing announcement by id. Requires ADMIN role."
    )
    public AnnouncementResponseDTO update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateAnnouncementRequestDTO request
    ) {
        return announcementMapper.toResponse(adminAnnouncementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete announcement (admin)",
        description = "Deletes an announcement by id. Requires ADMIN role."
    )
    public void delete(@PathVariable Long id) {
        adminAnnouncementService.delete(id);
    }
}
