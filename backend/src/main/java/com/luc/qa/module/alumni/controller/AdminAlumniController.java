package com.luc.qa.module.alumni.controller;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationFilterDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationResponseDTO;
import com.luc.qa.module.alumni.dto.ReviewVerificationRequestDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;
import com.luc.qa.module.alumni.service.AdminAlumniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/verifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Verifications")
public class AdminAlumniController {

    private final AdminAlumniService adminAlumniService;

    @GetMapping
    @Operation(
        summary = "List alumni verifications (admin)",
        description = "Returns a paginated, filterable list of alumni verification requests. Requires ADMIN role."
    )
    public PageResponseDTO<AdminVerificationResponseDTO> list(
        @ModelAttribute AdminVerificationFilterDTO filter
    ) {
        return adminAlumniService.listVerifications(filter);
    }

    @PostMapping("/{id}/approve")
    @Operation(
        summary = "Approve alumni verification (admin)",
        description = "Approves an alumni verification request. Requires ADMIN role."
    )
    public VerificationResponseDTO approve(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return adminAlumniService.approve(id, jwt.getSubject());
    }

    @PostMapping("/{id}/reject")
    @Operation(
        summary = "Reject alumni verification (admin)",
        description = "Rejects an alumni verification request with a review reason. Requires ADMIN role."
    )
    public VerificationResponseDTO reject(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody ReviewVerificationRequestDTO request
    ) {
        return adminAlumniService.reject(id, jwt.getSubject(), request);
    }
}
