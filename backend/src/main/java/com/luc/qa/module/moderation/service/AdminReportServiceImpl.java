package com.luc.qa.module.moderation.service;

import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.ReportNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.moderation.dto.AdminReportFilterDTO;
import com.luc.qa.module.moderation.dto.AdminReportResponseDTO;
import com.luc.qa.module.moderation.dto.ModeratorAuthorDTO;
import com.luc.qa.module.moderation.dto.ResolveReportRequestDTO;
import com.luc.qa.module.moderation.entity.Report;
import com.luc.qa.module.moderation.entity.ReportStatus;
import com.luc.qa.module.moderation.entity.ReportTargetType;
import com.luc.qa.module.moderation.entity.ResolutionAction;
import com.luc.qa.module.moderation.repository.ReportRepository;
import com.luc.qa.module.moderation.specification.ReportSpecifications;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.user.service.AdminUserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminReportServiceImpl implements AdminReportService {

    private static final int PREVIEW_MAX_LENGTH = 240;

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AdminUserService adminUserService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminReportResponseDTO> listReports(AdminReportFilterDTO filter) {
        Specification<Report> spec = ReportSpecifications.fromFilter(filter);
        PageRequest pageable = PageRequest.of(
            filter.getPage(),
            filter.getSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Report> page = reportRepository.findAll(spec, pageable);
        List<AdminReportResponseDTO> content = page.getContent().stream()
            .map(this::toAdminResponse)
            .toList();
        return PageResponseDTO.<AdminReportResponseDTO>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    @Override
    public AdminReportResponseDTO resolve(
        Long id,
        String resolverKeycloakId,
        ResolveReportRequestDTO request
    ) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new ReportNotFoundException(id));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BadRequestException("Only pending reports can be resolved");
        }

        User resolver = findUser(resolverKeycloakId);
        applyAction(report, request.getAction(), request.getResolutionNote());

        report.setStatus(ReportStatus.RESOLVED);
        report.setResolver(resolver);
        report.setResolvedAt(Instant.now());
        report.setResolutionNote(trimToNull(request.getResolutionNote()));

        return toAdminResponse(reportRepository.save(report));
    }

    private void applyAction(Report report, ResolutionAction action, String resolutionNote) {
        switch (action) {
            case NONE -> {
                // close without modifying the target
            }
            case DELETE_CONTENT -> deleteContent(report);
            case BAN_USER -> banTargetUser(report, resolutionNote);
            default -> throw new BadRequestException("Unsupported resolution action");
        }
    }

    private void deleteContent(Report report) {
        switch (report.getTargetType()) {
            case QUESTION -> softDeleteQuestion(report.getTargetId());
            case ANSWER -> softDeleteAnswer(report.getTargetId());
            case USER -> throw new BadRequestException("DELETE_CONTENT is not valid for user reports");
            default -> throw new BadRequestException("Unsupported report target type");
        }
    }

    private void softDeleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new BadRequestException("Question not found"));
        if (question.getStatus() != QuestionStatus.DELETED) {
            question.setStatus(QuestionStatus.DELETED);
            questionRepository.save(question);
        }
    }

    private void softDeleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new BadRequestException("Answer not found"));
        if (!answer.isDeleted()) {
            answer.setDeleted(true);
            answerRepository.save(answer);
            questionRepository.decrementAnswerCount(answer.getQuestion().getId());
        }
    }

    private void banTargetUser(Report report, String resolutionNote) {
        User targetUser = resolveTargetUser(report);
        if (targetUser.isBanned()) {
            return;
        }
        String reason = trimToNull(resolutionNote);
        if (reason == null) {
            reason = "Banned via report #" + report.getId();
        }
        try {
            adminUserService.banUser(targetUser.getId(), reason);
        } catch (Exception ex) {
            log.error("Failed to ban user {} via report {}", targetUser.getId(), report.getId(), ex);
            throw new BadRequestException("Failed to ban user in Keycloak: " + ex.getMessage());
        }
    }

    private User resolveTargetUser(Report report) {
        return switch (report.getTargetType()) {
            case USER -> userRepository.findById(report.getTargetId())
                .orElseThrow(() -> new BadRequestException("User not found"));
            case QUESTION -> questionRepository.findById(report.getTargetId())
                .map(Question::getAuthor)
                .orElseThrow(() -> new BadRequestException("Question not found"));
            case ANSWER -> answerRepository.findById(report.getTargetId())
                .map(Answer::getAuthor)
                .orElseThrow(() -> new BadRequestException("Answer not found"));
        };
    }

    private AdminReportResponseDTO toAdminResponse(Report report) {
        TargetContext context = loadTargetContext(report.getTargetType(), report.getTargetId());
        return AdminReportResponseDTO.builder()
            .id(report.getId())
            .reporterId(report.getReporter().getId())
            .reporterDisplayName(report.getReporter().getDisplayName())
            .reporterEmail(report.getReporter().getEmail())
            .targetType(report.getTargetType())
            .targetId(report.getTargetId())
            .reason(report.getReason())
            .details(report.getDetails())
            .status(report.getStatus())
            .createdAt(report.getCreatedAt())
            .resolvedAt(report.getResolvedAt())
            .resolutionNote(report.getResolutionNote())
            .resolverDisplayName(report.getResolver() != null ? report.getResolver().getDisplayName() : null)
            .targetPreview(context.preview())
            .targetQuestionId(context.questionId())
            .targetAuthor(context.author())
            .build();
    }

    private TargetContext loadTargetContext(ReportTargetType targetType, Long targetId) {
        return switch (targetType) {
            case QUESTION -> {
                Question question = questionRepository.findById(targetId)
                    .orElse(null);
                if (question == null) {
                    yield new TargetContext("Question not found", null, null);
                }
                yield new TargetContext(
                    previewText(question.getTitle(), question.getBody()),
                    question.getId(),
                    moderatorAuthor(question.getAuthor(), question.isAnonymous())
                );
            }
            case ANSWER -> {
                Answer answer = answerRepository.findById(targetId)
                    .orElse(null);
                if (answer == null) {
                    yield new TargetContext("Answer not found", null, null);
                }
                yield new TargetContext(
                    previewText(null, answer.isDeleted() ? "[deleted]" : answer.getBody()),
                    answer.getQuestion().getId(),
                    moderatorAuthor(answer.getAuthor(), answer.isAnonymous())
                );
            }
            case USER -> {
                User user = userRepository.findById(targetId)
                    .orElse(null);
                if (user == null) {
                    yield new TargetContext("User not found", null, null);
                }
                yield new TargetContext(
                    user.getDisplayName() + " (" + user.getEmail() + ")",
                    null,
                    ModeratorAuthorDTO.builder()
                        .userId(user.getId())
                        .displayName(user.getDisplayName())
                        .email(user.getEmail())
                        .postedAnonymously(false)
                        .build()
                );
            }
        };
    }

    private ModeratorAuthorDTO moderatorAuthor(User author, boolean postedAnonymously) {
        return ModeratorAuthorDTO.builder()
            .userId(author.getId())
            .displayName(author.getDisplayName())
            .email(author.getEmail())
            .postedAnonymously(postedAnonymously)
            .build();
    }

    private String previewText(String title, String body) {
        String combined = title != null && !title.isBlank()
            ? title + "\n\n" + body
            : body;
        if (combined == null) {
            return "";
        }
        if (combined.length() <= PREVIEW_MAX_LENGTH) {
            return combined;
        }
        return combined.substring(0, PREVIEW_MAX_LENGTH) + "…";
    }

    private User findUser(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record TargetContext(String preview, Long questionId, ModeratorAuthorDTO author) {
    }
}
