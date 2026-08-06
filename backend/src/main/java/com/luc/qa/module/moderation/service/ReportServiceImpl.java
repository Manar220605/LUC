package com.luc.qa.module.moderation.service;

import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.ReportAlreadyOpenException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.moderation.dto.CreateReportRequestDTO;
import com.luc.qa.module.moderation.dto.ReportResponseDTO;
import com.luc.qa.module.moderation.entity.Report;
import com.luc.qa.module.moderation.entity.ReportStatus;
import com.luc.qa.module.moderation.entity.ReportTargetType;
import com.luc.qa.module.moderation.mapper.ReportMapper;
import com.luc.qa.module.moderation.repository.ReportRepository;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponseDTO create(CreateReportRequestDTO request, String keycloakId) {
        User reporter = findUser(keycloakId);
        validateTarget(request.getTargetType(), request.getTargetId(), reporter.getId());

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
            reporter.getId(),
            request.getTargetType(),
            request.getTargetId(),
            ReportStatus.PENDING
        )) {
            throw new ReportAlreadyOpenException();
        }

        Report report = Report.builder()
            .reporter(reporter)
            .targetType(request.getTargetType())
            .targetId(request.getTargetId())
            .reason(request.getReason())
            .details(trimToNull(request.getDetails()))
            .status(ReportStatus.PENDING)
            .build();

        return reportMapper.toResponse(reportRepository.save(report));
    }

    private void validateTarget(ReportTargetType targetType, Long targetId, Long reporterId) {
        switch (targetType) {
            case QUESTION -> validateQuestionTarget(targetId, reporterId);
            case ANSWER -> validateAnswerTarget(targetId, reporterId);
            case USER -> validateUserTarget(targetId, reporterId);
            default -> throw new BadRequestException("Unsupported report target type");
        }
    }

    private void validateQuestionTarget(Long targetId, Long reporterId) {
        Question question = questionRepository.findById(targetId)
            .orElseThrow(() -> new BadRequestException("Question not found"));
        if (question.getStatus() == QuestionStatus.DELETED) {
            throw new BadRequestException("Cannot report deleted content");
        }
        if (question.getAuthor().getId().equals(reporterId)) {
            throw new BadRequestException("You cannot report your own content");
        }
    }

    private void validateAnswerTarget(Long targetId, Long reporterId) {
        Answer answer = answerRepository.findById(targetId)
            .orElseThrow(() -> new BadRequestException("Answer not found"));
        if (answer.isDeleted()) {
            throw new BadRequestException("Cannot report deleted content");
        }
        if (answer.getAuthor().getId().equals(reporterId)) {
            throw new BadRequestException("You cannot report your own content");
        }
    }

    private void validateUserTarget(Long targetId, Long reporterId) {
        User target = userRepository.findById(targetId)
            .orElseThrow(() -> new BadRequestException("User not found"));
        if (target.getId().equals(reporterId)) {
            throw new BadRequestException("You cannot report yourself");
        }
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
}
