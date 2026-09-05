package com.luc.qa.module.user.controller;

import com.luc.qa.module.user.dto.PublicProfileDTO;
import com.luc.qa.module.user.service.PublicProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    @GetMapping("/{id}/public-profile")
    @Operation(
        summary = "Get public user profile",
        description = "Returns the public profile for a user by internal ID. Public endpoint; no authentication required."
    )
    public PublicProfileDTO get(@PathVariable Long id) {
        return publicProfileService.getPublicProfile(id);
    }
}
