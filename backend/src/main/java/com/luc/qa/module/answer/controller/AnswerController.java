package com.luc.qa.module.answer.controller;

import com.luc.qa.module.answer.dto.AnswerResponseDTO;
import com.luc.qa.module.answer.dto.AnswerTreeNodeDTO;
import com.luc.qa.module.answer.dto.CreateAnswerRequestDTO;
import com.luc.qa.module.answer.dto.UpdateAnswerRequestDTO;
import com.luc.qa.module.answer.service.AnswerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "Answers")
public class AnswerController {

    private final AnswerService answerService;

    @GetMapping("/questions/{questionId}/answers")
    public List<AnswerTreeNodeDTO> getAnswers(@PathVariable Long questionId) {
        return answerService.getAnswerTree(questionId, resolveKeycloakId());
    }

    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerResponseDTO> createTopLevel(
        @PathVariable Long questionId,
        @Valid @RequestBody CreateAnswerRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        AnswerResponseDTO created = answerService.createTopLevel(questionId, request, jwt.getSubject());
        return ResponseEntity
            .created(URI.create("/api/answers/" + created.getId()))
            .body(created);
    }

    @PostMapping("/answers/{id}/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerResponseDTO> createReply(
        @PathVariable Long id,
        @Valid @RequestBody CreateAnswerRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        AnswerResponseDTO created = answerService.createReply(id, request, jwt.getSubject());
        return ResponseEntity
            .created(URI.create("/api/answers/" + created.getId()))
            .body(created);
    }

    @PutMapping("/answers/{id}")
    @PreAuthorize("isAuthenticated()")
    public AnswerResponseDTO update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateAnswerRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return answerService.update(id, request, jwt.getSubject());
    }

    @DeleteMapping("/answers/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        answerService.softDelete(id, jwt.getSubject());
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
