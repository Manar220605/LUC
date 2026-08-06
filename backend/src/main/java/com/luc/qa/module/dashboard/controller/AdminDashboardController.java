package com.luc.qa.module.dashboard.controller;

import com.luc.qa.module.dashboard.dto.DashboardMetricsResponseDTO;
import com.luc.qa.module.dashboard.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public DashboardMetricsResponseDTO getMetrics() {
        return adminDashboardService.getMetrics();
    }
}
