package com.luc.qa.module.community.controller;

import com.luc.qa.module.community.dto.CommunityResponseDTO;
import com.luc.qa.module.community.dto.CommunityTreeNodeDTO;
import com.luc.qa.module.community.mapper.CommunityMapper;
import com.luc.qa.module.community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Tag(name = "Communities")
public class CommunityController {

    private final CommunityService communityService;
    private final CommunityMapper communityMapper;

    @GetMapping
    @Operation(
        summary = "Get community tree",
        description = "Returns the full hierarchical community tree. Public endpoint."
    )
    public List<CommunityTreeNodeDTO> getTree() {
        return communityService.getTree();
    }

    @GetMapping("/by-path")
    @Operation(
        summary = "Get community by path",
        description = "Returns a single community matched by its path. Public endpoint."
    )
    public CommunityResponseDTO getByPath(@RequestParam String path) {
        return communityMapper.toResponse(communityService.findByPath(path));
    }

    @GetMapping("/children")
    @Operation(
        summary = "List child communities",
        description = "Returns direct child communities for the given parent path. Public endpoint."
    )
    public List<CommunityResponseDTO> getChildren(@RequestParam String path) {
        return communityService.findChildren(path).stream()
            .map(communityMapper::toResponse)
            .toList();
    }
}
