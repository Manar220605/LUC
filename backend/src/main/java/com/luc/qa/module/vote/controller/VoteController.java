package com.luc.qa.module.vote.controller;

import com.luc.qa.module.vote.dto.CastVoteRequestDTO;
import com.luc.qa.module.vote.dto.VoteResponseDTO;
import com.luc.qa.module.vote.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Votes")
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Cast or change a vote",
        description = "Creates or updates a vote on a question or answer. Requires JWT authentication."
    )
    public VoteResponseDTO castVote(
        @Valid @RequestBody CastVoteRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return voteService.castVote(request, jwt.getSubject());
    }
}
