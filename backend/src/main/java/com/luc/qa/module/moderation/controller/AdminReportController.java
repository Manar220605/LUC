package com.luc.qa.module.moderation.controller;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.moderation.dto.AdminReportFilterDTO;
import com.luc.qa.module.moderation.dto.AdminReportResponseDTO;
import com.luc.qa.module.moderation.dto.ResolveReportRequestDTO;
import com.luc.qa.module.moderation.service.AdminReportService;
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
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    @Operation(
        summary = "List reports (admin)",
        description = "Returns a paginated, filterable list of moderation reports. Requires ADMIN role."
    )
    public PageResponseDTO<AdminReportResponseDTO> list(
        @ModelAttribute AdminReportFilterDTO filter
    ) {
        return adminReportService.listReports(filter);
    }

    @PostMapping("/{id}/resolve")
    @Operation(
        summary = "Resolve report (admin)",
        description = "Resolves a moderation report with an admin decision. Requires ADMIN role."
    )
    public AdminReportResponseDTO resolve(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody ResolveReportRequestDTO request
    ) {
        return adminReportService.resolve(id, jwt.getSubject(), request);
    }
}
