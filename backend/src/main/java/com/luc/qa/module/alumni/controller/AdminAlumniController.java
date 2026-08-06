package com.luc.qa.module.alumni.controller;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationFilterDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationResponseDTO;
import com.luc.qa.module.alumni.dto.ReviewVerificationRequestDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;
import com.luc.qa.module.alumni.service.AdminAlumniService;
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
    public PageResponseDTO<AdminVerificationResponseDTO> list(
        @ModelAttribute AdminVerificationFilterDTO filter
    ) {
        return adminAlumniService.listVerifications(filter);
    }

    @PostMapping("/{id}/approve")
    public VerificationResponseDTO approve(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return adminAlumniService.approve(id, jwt.getSubject());
    }

    @PostMapping("/{id}/reject")
    public VerificationResponseDTO reject(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody ReviewVerificationRequestDTO request
    ) {
        return adminAlumniService.reject(id, jwt.getSubject(), request);
    }
}
