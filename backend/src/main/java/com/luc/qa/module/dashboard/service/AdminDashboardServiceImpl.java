package com.luc.qa.module.dashboard.service;

import com.luc.qa.module.alumni.entity.VerificationStatus;
import com.luc.qa.module.alumni.repository.AlumniVerificationRepository;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.dashboard.dto.DashboardMetricsResponseDTO;
import com.luc.qa.module.moderation.entity.ReportStatus;
import com.luc.qa.module.moderation.repository.ReportRepository;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AlumniVerificationRepository verificationRepository;
    private final ReportRepository reportRepository;

    @Override
    public DashboardMetricsResponseDTO getMetrics() {
        return DashboardMetricsResponseDTO.builder()
            .totalUsers(userRepository.count())
            .totalAlumni(userRepository.countByRole(UserRole.ALUMNI))
            .totalQuestions(questionRepository.count())
            .totalAnswers(answerRepository.count())
            .pendingVerifications(verificationRepository.countByStatus(VerificationStatus.PENDING))
            .openReports(reportRepository.countByStatus(ReportStatus.PENDING))
            .build();
    }
}
