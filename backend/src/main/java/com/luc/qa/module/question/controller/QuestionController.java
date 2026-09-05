package com.luc.qa.module.question.controller;

import com.luc.qa.module.question.dto.CreateQuestionRequestDTO;
import com.luc.qa.module.question.dto.QuestionResponseDTO;
import com.luc.qa.module.question.dto.UpdateQuestionRequestDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.mapper.QuestionMapper;
import com.luc.qa.module.question.service.QuestionService;
import com.luc.qa.module.vote.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final VoteService voteService;

    @GetMapping("/{id}")
    @Operation(
        summary = "Get question by ID",
        description = "Fetches a question, increments its view count, and enriches vote state when a JWT is present."
    )
    public QuestionResponseDTO get(@PathVariable Long id) {
        questionService.incrementView(id);
        Question question = questionService.findById(id);
        QuestionResponseDTO response = questionMapper.toResponse(question);
        voteService.enrichQuestionResponse(response, question, resolveKeycloakId());
        return response;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create question",
        description = "Creates a new question. Requires JWT authentication."
    )
    public ResponseEntity<QuestionResponseDTO> create(
        @Valid @RequestBody CreateQuestionRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Question created = questionService.create(request, jwt.getSubject());
        return ResponseEntity
            .created(URI.create("/api/questions/" + created.getId()))
            .body(questionMapper.toResponse(questionService.findById(created.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update question",
        description = "Updates an existing question. Requires JWT authentication; caller must own the question (or have edit rights)."
    )
    public QuestionResponseDTO update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateQuestionRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return questionMapper.toResponse(questionService.findById(
            questionService.update(id, request, jwt.getSubject()).getId()
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Soft-delete question",
        description = "Soft-deletes a question. Requires JWT authentication; caller must own the question."
    )
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        questionService.softDelete(id, jwt.getSubject());
    }

    @PutMapping("/{questionId}/accepted-answer/{answerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Accept an answer",
        description = "Marks an answer as accepted for the question. Requires JWT authentication; typically the question author."
    )
    public QuestionResponseDTO acceptAnswer(
        @PathVariable Long questionId,
        @PathVariable Long answerId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Question question = questionService.acceptAnswer(questionId, answerId, jwt.getSubject());
        Question reloaded = questionService.findById(question.getId());
        QuestionResponseDTO response = questionMapper.toResponse(reloaded);
        voteService.enrichQuestionResponse(response, reloaded, jwt.getSubject());
        return response;
    }

    @DeleteMapping("/{questionId}/accepted-answer")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Unaccept answer",
        description = "Clears the accepted answer on a question. Requires JWT authentication; typically the question author."
    )
    public QuestionResponseDTO unacceptAnswer(
        @PathVariable Long questionId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Question question = questionService.unacceptAnswer(questionId, jwt.getSubject());
        Question reloaded = questionService.findById(question.getId());
        QuestionResponseDTO response = questionMapper.toResponse(reloaded);
        voteService.enrichQuestionResponse(response, reloaded, jwt.getSubject());
        return response;
    }

    private String resolveKeycloakId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getSubject();
        }
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }
}
