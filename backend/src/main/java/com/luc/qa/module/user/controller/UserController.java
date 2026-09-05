package com.luc.qa.module.user.controller;

import com.luc.qa.module.user.dto.OnboardingRequestDTO;
import com.luc.qa.module.user.dto.UpdateProfileRequestDTO;
import com.luc.qa.module.user.dto.UserResponseDTO;
import com.luc.qa.module.user.mapper.UserMapper;
import com.luc.qa.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "Current User")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the authenticated user's profile. Requires JWT authentication."
    )
    public UserResponseDTO getMe(@AuthenticationPrincipal Jwt jwt) {
        return userMapper.toResponse(userService.getByKeycloakId(jwt.getSubject()));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update current user profile",
        description = "Updates profile fields for the authenticated user. Requires JWT authentication."
    )
    public UserResponseDTO updateMe(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdateProfileRequestDTO request
    ) {
        return userMapper.toResponse(userService.updateProfile(jwt.getSubject(), request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Upload avatar",
        description = "Uploads a new avatar image (multipart form field: file). Requires JWT authentication."
    )
    public UserResponseDTO uploadAvatar(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam("file") MultipartFile file
    ) {
        return userMapper.toResponse(userService.updateAvatar(jwt.getSubject(), file));
    }

    @DeleteMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete avatar",
        description = "Removes the current user's avatar. Requires JWT authentication."
    )
    public void deleteAvatar(@AuthenticationPrincipal Jwt jwt) {
        userService.clearAvatar(jwt.getSubject());
    }

    @PostMapping("/onboarding")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Complete onboarding",
        description = "Completes first-time onboarding for the authenticated user. Requires JWT authentication."
    )
    public UserResponseDTO onboarding(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody OnboardingRequestDTO request
    ) {
        return userMapper.toResponse(userService.completeOnboarding(jwt.getSubject(), request));
    }
}
