package com.luc.qa.module.community.controller;

import com.luc.qa.module.community.dto.CommunityFollowResponseDTO;
import com.luc.qa.module.community.dto.CreateFollowRequestDTO;
import com.luc.qa.module.community.dto.FollowStatusDTO;
import com.luc.qa.module.community.service.CommunityFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Community Follows")
public class CommunityFollowController {

    private final CommunityFollowService communityFollowService;

    @PostMapping
    @Operation(
        summary = "Follow a community",
        description = "Follows the community at the given path for the authenticated user. Requires JWT authentication."
    )
    public FollowStatusDTO follow(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateFollowRequestDTO request
    ) {
        return communityFollowService.follow(jwt.getSubject(), request.getCommunityPath());
    }

    @DeleteMapping
    @Operation(
        summary = "Unfollow a community",
        description = "Unfollows the community at the given path for the authenticated user. Requires JWT authentication."
    )
    public FollowStatusDTO unfollow(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam String communityPath
    ) {
        return communityFollowService.unfollow(jwt.getSubject(), communityPath);
    }

    @GetMapping
    @Operation(
        summary = "List my followed communities",
        description = "Returns communities followed by the authenticated user. Requires JWT authentication."
    )
    public List<CommunityFollowResponseDTO> listMine(@AuthenticationPrincipal Jwt jwt) {
        return communityFollowService.listMine(jwt.getSubject());
    }

    @GetMapping("/status")
    @Operation(
        summary = "Get follow status",
        description = "Returns whether the authenticated user follows the given community path. Requires JWT authentication."
    )
    public FollowStatusDTO status(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam String communityPath
    ) {
        return communityFollowService.status(jwt.getSubject(), communityPath);
    }
}
