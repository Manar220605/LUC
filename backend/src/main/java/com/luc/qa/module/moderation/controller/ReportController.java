package com.luc.qa.module.moderation.controller;

import com.luc.qa.module.moderation.dto.CreateReportRequestDTO;
import com.luc.qa.module.moderation.dto.ReportResponseDTO;
import com.luc.qa.module.moderation.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ReportResponseDTO create(
        @Valid @RequestBody CreateReportRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return reportService.create(request, jwt.getSubject());
    }
}
