package com.luc.qa.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Returns a simple UP status for liveness/readiness probes. Public; no authentication required."
    )
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
