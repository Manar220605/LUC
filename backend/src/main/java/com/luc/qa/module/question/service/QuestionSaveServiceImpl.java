package com.luc.qa.module.question.service;

import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.question.dto.QuestionSaveResponseDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.dto.SaveStatusDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionSave;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.mapper.QuestionMapper;
import com.luc.qa.module.question.repository.QuestionSaveRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.vote.service.VoteService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionSaveServiceImpl implements QuestionSaveService {

    private final QuestionSaveRepository questionSaveRepository;
    private final QuestionService questionService;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final VoteService voteService;

    @Override
    public SaveStatusDTO save(String keycloakId, Long questionId) {
        User user = findUser(keycloakId);
        Question question = questionService.findById(questionId);
        if (!questionSaveRepository.existsByUserIdAndQuestionId(user.getId(), question.getId())) {
            questionSaveRepository.save(QuestionSave.builder()
                .user(user)
                .question(question)
                .build());
        }
        return statusFor(user.getId(), question.getId());
    }

    @Override
    public SaveStatusDTO unsave(String keycloakId, Long questionId) {
        User user = findUser(keycloakId);
        questionSaveRepository.deleteByUserIdAndQuestionId(user.getId(), questionId);
        return SaveStatusDTO.builder().saved(false).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SaveStatusDTO status(String keycloakId, Long questionId) {
        User user = findUser(keycloakId);
        Question question = questionService.findById(questionId);
        return statusFor(user.getId(), question.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionSaveResponseDTO> listMine(String keycloakId) {
        User user = findUser(keycloakId);
        List<QuestionSave> saves = questionSaveRepository
            .findByUser_IdAndQuestion_StatusNotOrderByCreatedAtDesc(user.getId(), QuestionStatus.DELETED);
        List<Question> questions = saves.stream().map(QuestionSave::getQuestion).toList();
        List<QuestionSummaryDTO> summaries = questions.stream().map(questionMapper::toSummary).toList();
        voteService.enrichQuestionSummaries(summaries, questions, keycloakId);

        List<QuestionSaveResponseDTO> result = new ArrayList<>(saves.size());
        for (int i = 0; i < saves.size(); i++) {
            result.add(QuestionSaveResponseDTO.builder()
                .question(summaries.get(i))
                .savedAt(saves.get(i).getCreatedAt())
                .build());
        }
        return result;
    }

    private SaveStatusDTO statusFor(Long userId, Long questionId) {
        return SaveStatusDTO.builder()
            .saved(questionSaveRepository.existsByUserIdAndQuestionId(userId, questionId))
            .build();
    }

    private User findUser(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }
}
