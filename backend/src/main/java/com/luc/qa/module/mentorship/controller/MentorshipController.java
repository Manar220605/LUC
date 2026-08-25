package com.luc.qa.module.mentorship.controller;

import com.luc.qa.module.mentorship.dto.CreateMentorshipRequestDTO;
import com.luc.qa.module.mentorship.dto.MentorshipInboxDTO;
import com.luc.qa.module.mentorship.dto.MentorshipRequestResponseDTO;
import com.luc.qa.module.mentorship.dto.MentorshipStatusDTO;
import com.luc.qa.module.mentorship.service.MentorshipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentorship")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Mentorship")
public class MentorshipController {

    private final MentorshipService mentorshipService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public MentorshipRequestResponseDTO create(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateMentorshipRequestDTO request
    ) {
        return mentorshipService.create(jwt.getSubject(), request);
    }

    @GetMapping("/requests")
    public MentorshipInboxDTO listMine(@AuthenticationPrincipal Jwt jwt) {
        return mentorshipService.listMine(jwt.getSubject());
    }

    @GetMapping("/with/{userId}")
    public MentorshipStatusDTO statusWith(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long userId
    ) {
        return mentorshipService.statusWith(jwt.getSubject(), userId);
    }

    @PostMapping("/requests/{id}/accept")
    public MentorshipRequestResponseDTO accept(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        return mentorshipService.accept(jwt.getSubject(), id);
    }

    @PostMapping("/requests/{id}/decline")
    public MentorshipRequestResponseDTO decline(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        return mentorshipService.decline(jwt.getSubject(), id);
    }
}
