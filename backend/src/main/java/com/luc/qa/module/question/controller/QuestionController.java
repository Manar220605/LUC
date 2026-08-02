package com.luc.qa.module.question.controller;

import com.luc.qa.module.question.dto.CreateQuestionRequestDTO;
import com.luc.qa.module.question.dto.QuestionResponseDTO;
import com.luc.qa.module.question.dto.UpdateQuestionRequestDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.mapper.QuestionMapper;
import com.luc.qa.module.question.service.QuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @GetMapping("/{id}")
    public QuestionResponseDTO get(@PathVariable Long id) {
        questionService.incrementView(id);
        return questionMapper.toResponse(questionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
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
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        questionService.softDelete(id, jwt.getSubject());
    }
}
