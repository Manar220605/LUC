package com.luc.qa.module.alumni.controller;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.alumni.dto.AlumniDirectoryEntryDTO;
import com.luc.qa.module.alumni.dto.AlumniDirectoryFilterDTO;
import com.luc.qa.module.alumni.service.AlumniDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
@Tag(name = "Alumni")
public class AlumniDirectoryController {

    private final AlumniDirectoryService alumniDirectoryService;

    @GetMapping
    @Operation(
        summary = "List alumni directory",
        description = "Returns a paginated, filterable directory of verified alumni profiles. Public endpoint."
    )
    public PageResponseDTO<AlumniDirectoryEntryDTO> list(@ModelAttribute AlumniDirectoryFilterDTO filter) {
        return alumniDirectoryService.list(filter);
    }
}
