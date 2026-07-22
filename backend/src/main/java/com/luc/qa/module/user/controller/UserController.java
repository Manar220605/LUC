package com.luc.qa.module.user.controller;

import com.luc.qa.module.user.dto.OnboardingRequestDTO;
import com.luc.qa.module.user.dto.UpdateProfileRequestDTO;
import com.luc.qa.module.user.dto.UserResponseDTO;
import com.luc.qa.module.user.mapper.UserMapper;
import com.luc.qa.module.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "Current User")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UserResponseDTO getMe(@AuthenticationPrincipal Jwt jwt) {
        return userMapper.toResponse(userService.getByKeycloakId(jwt.getSubject()));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public UserResponseDTO updateMe(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdateProfileRequestDTO request
    ) {
        return userMapper.toResponse(userService.updateProfile(jwt.getSubject(), request));
    }

    @PostMapping("/onboarding")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDTO onboarding(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody OnboardingRequestDTO request
    ) {
        return userMapper.toResponse(userService.completeOnboarding(jwt.getSubject(), request));
    }
}
