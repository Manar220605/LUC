package com.luc.qa.module.question.controller;

import com.luc.qa.module.question.dto.CreateSaveRequestDTO;
import com.luc.qa.module.question.dto.QuestionSaveResponseDTO;
import com.luc.qa.module.question.dto.SaveStatusDTO;
import com.luc.qa.module.question.service.QuestionSaveService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saves")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Question Saves")
public class QuestionSaveController {

    private final QuestionSaveService questionSaveService;

    @PostMapping
    public SaveStatusDTO save(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateSaveRequestDTO request
    ) {
        return questionSaveService.save(jwt.getSubject(), request.getQuestionId());
    }

    @DeleteMapping
    public SaveStatusDTO unsave(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam Long questionId
    ) {
        return questionSaveService.unsave(jwt.getSubject(), questionId);
    }

    @GetMapping
    public List<QuestionSaveResponseDTO> listMine(@AuthenticationPrincipal Jwt jwt) {
        return questionSaveService.listMine(jwt.getSubject());
    }

    @GetMapping("/status")
    public SaveStatusDTO status(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam Long questionId
    ) {
        return questionSaveService.status(jwt.getSubject(), questionId);
    }
}
