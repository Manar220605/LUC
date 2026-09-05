package com.luc.qa.module.alumni.controller;

import com.luc.qa.module.alumni.dto.AlumniProfileResponseDTO;
import com.luc.qa.module.alumni.dto.SubmitVerificationRequestDTO;
import com.luc.qa.module.alumni.dto.UpdateAlumniProfileRequestDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;
import com.luc.qa.module.alumni.service.AlumniProfileService;
import com.luc.qa.module.alumni.service.AlumniVerificationService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Alumni")
public class AlumniController {

    private final AlumniVerificationService verificationService;
    private final AlumniProfileService profileService;

    @PostMapping("/verifications")
    @Operation(
        summary = "Submit alumni verification",
        description = "Submits alumni verification details for admin review. Requires JWT authentication."
    )
    public VerificationResponseDTO submitVerification(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody SubmitVerificationRequestDTO request
    ) {
        return verificationService.submit(jwt.getSubject(), request);
    }

    @GetMapping("/verifications/me")
    @Operation(
        summary = "Get my alumni verification",
        description = "Returns the authenticated user's alumni verification status. Requires JWT authentication."
    )
    public VerificationResponseDTO getMyVerification(@AuthenticationPrincipal Jwt jwt) {
        return verificationService.getMine(jwt.getSubject());
    }

    @GetMapping("/profiles/me")
    @Operation(
        summary = "Get my alumni profile",
        description = "Returns the authenticated user's alumni profile. Requires JWT authentication."
    )
    public AlumniProfileResponseDTO getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return profileService.getMine(jwt.getSubject());
    }

    @PutMapping("/profiles/me")
    @Operation(
        summary = "Update my alumni profile",
        description = "Updates the authenticated user's alumni profile fields. Requires JWT authentication."
    )
    public AlumniProfileResponseDTO updateMyProfile(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdateAlumniProfileRequestDTO request
    ) {
        return profileService.updateMine(jwt.getSubject(), request);
    }
}
