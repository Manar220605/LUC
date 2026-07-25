package com.luc.qa.module.community.controller;

import com.luc.qa.module.community.dto.CommunityResponseDTO;
import com.luc.qa.module.community.dto.CommunityTreeNodeDTO;
import com.luc.qa.module.community.mapper.CommunityMapper;
import com.luc.qa.module.community.service.CommunityService;
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
    public List<CommunityTreeNodeDTO> getTree() {
        return communityService.getTree();
    }

    @GetMapping("/by-path")
    public CommunityResponseDTO getByPath(@RequestParam String path) {
        return communityMapper.toResponse(communityService.findByPath(path));
    }

    @GetMapping("/children")
    public List<CommunityResponseDTO> getChildren(@RequestParam String path) {
        return communityService.findChildren(path).stream()
            .map(communityMapper::toResponse)
            .toList();
    }
}
