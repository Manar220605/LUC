package com.luc.qa.module.community.controller;

import com.luc.qa.module.community.dto.CommunityResponseDTO;
import com.luc.qa.module.community.dto.CreateCommunityRequestDTO;
import com.luc.qa.module.community.dto.UpdateCommunityRequestDTO;
import com.luc.qa.module.community.mapper.CommunityMapper;
import com.luc.qa.module.community.service.AdminCommunityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin/communities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Communities")
public class AdminCommunityController {

    private final AdminCommunityService adminCommunityService;
    private final CommunityMapper communityMapper;

    @PostMapping
    public ResponseEntity<CommunityResponseDTO> create(@Valid @RequestBody CreateCommunityRequestDTO request) {
        var created = adminCommunityService.create(request);
        return ResponseEntity
            .created(URI.create("/api/communities/by-path?path=" + created.getPath()))
            .body(communityMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public CommunityResponseDTO update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCommunityRequestDTO request
    ) {
        return communityMapper.toResponse(adminCommunityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adminCommunityService.delete(id);
    }
}
