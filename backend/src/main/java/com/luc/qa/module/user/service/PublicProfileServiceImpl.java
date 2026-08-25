package com.luc.qa.module.user.service;

import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.repository.AlumniProfileRepository;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.user.dto.PublicAlumniInfoDTO;
import com.luc.qa.module.user.dto.PublicProfileAnswerDTO;
import com.luc.qa.module.user.dto.PublicProfileDTO;
import com.luc.qa.module.user.dto.PublicProfileQuestionDTO;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicProfileServiceImpl implements PublicProfileService {

    private static final int ACTIVITY_LIMIT = 20;
    private static final int PREVIEW_LENGTH = 160;

    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Override
    public PublicProfileDTO getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        var pageable = PageRequest.of(0, ACTIVITY_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt", "id"));

        long questionCount = questionRepository.countByAuthor_IdAndAnonymousFalseAndStatusNot(
            userId,
            QuestionStatus.DELETED
        );
        long answerCount = answerRepository.countByAuthor_IdAndAnonymousFalseAndDeletedFalseAndQuestion_StatusNot(
            userId,
            QuestionStatus.DELETED
        );
        long score = nvl(questionRepository.sumPublicScoreByAuthor(userId))
            + nvl(answerRepository.sumPublicScoreByAuthor(userId));

        List<PublicProfileQuestionDTO> questions = questionRepository
            .findByAuthor_IdAndAnonymousFalseAndStatusNot(userId, QuestionStatus.DELETED, pageable)
            .stream()
            .map(this::toQuestion)
            .toList();

        List<PublicProfileAnswerDTO> answers = answerRepository
            .findByAuthor_IdAndAnonymousFalseAndDeletedFalseAndQuestion_StatusNot(
                userId,
                QuestionStatus.DELETED,
                pageable
            )
            .stream()
            .map(this::toAnswer)
            .toList();

        return PublicProfileDTO.builder()
            .id(user.getId())
            .displayName(user.getDisplayName())
            .avatarUrl(blankToNull(user.getAvatarUrl()))
            .bio(blankToNull(user.getBio()))
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .questionCount(questionCount)
            .answerCount(answerCount)
            .score(score)
            .alumni(publicAlumni(user))
            .questions(questions)
            .answers(answers)
            .build();
    }

    private PublicAlumniInfoDTO publicAlumni(User user) {
        if (user.getRole() != UserRole.ALUMNI) {
            return null;
        }
        return alumniProfileRepository.findById(user.getId())
            .filter(AlumniProfile::isPublicProfile)
            .map(profile -> PublicAlumniInfoDTO.builder()
                .gradYear(profile.getGradYear())
                .faculty(profile.getFaculty())
                .degree(profile.getDegree())
                .major(blankToNull(profile.getMajor()))
                .currentPosition(blankToNull(profile.getCurrentPosition()))
                .currentCompany(blankToNull(profile.getCurrentCompany()))
                .linkedinUrl(blankToNull(profile.getLinkedinUrl()))
                .build())
            .orElse(null);
    }

    private PublicProfileQuestionDTO toQuestion(Question question) {
        return PublicProfileQuestionDTO.builder()
            .id(question.getId())
            .title(question.getTitle())
            .communityPath(question.getCommunity().getPath())
            .communityName(question.getCommunity().getName())
            .score(question.getScore())
            .answerCount(question.getAnswerCount())
            .createdAt(question.getCreatedAt())
            .build();
    }

    private PublicProfileAnswerDTO toAnswer(Answer answer) {
        return PublicProfileAnswerDTO.builder()
            .id(answer.getId())
            .questionId(answer.getQuestion().getId())
            .questionTitle(answer.getQuestion().getTitle())
            .bodyPreview(preview(answer.getBody()))
            .score(answer.getScore())
            .createdAt(answer.getCreatedAt())
            .build();
    }

    private static String preview(String body) {
        if (body == null) {
            return "";
        }
        String flattened = body.replace('\n', ' ').replace('\r', ' ').replaceAll(" +", " ").trim();
        if (flattened.length() <= PREVIEW_LENGTH) {
            return flattened;
        }
        return flattened.substring(0, PREVIEW_LENGTH - 1).trim() + "…";
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static long nvl(Long value) {
        return value == null ? 0L : value;
    }
}
