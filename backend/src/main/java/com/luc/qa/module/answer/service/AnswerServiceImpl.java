package com.luc.qa.module.answer.service;

import com.luc.qa.common.exception.AnswerNotFoundException;
import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.ForbiddenException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.answer.dto.AnswerResponseDTO;
import com.luc.qa.module.answer.dto.AnswerTreeNodeDTO;
import com.luc.qa.module.answer.dto.CreateAnswerRequestDTO;
import com.luc.qa.module.answer.dto.UpdateAnswerRequestDTO;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.answer.mapper.AnswerMapper;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.question.service.QuestionService;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;
    private final UserRepository userRepository;
    private final AnswerMapper answerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AnswerTreeNodeDTO> getAnswerTree(Long questionId, String keycloakId) {
        questionService.findById(questionId);

        List<Answer> flat = answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
        Map<Long, List<Answer>> childrenByParentId = new HashMap<>();
        List<Answer> roots = new ArrayList<>();
        Long currentUserId = resolveCurrentUserId(keycloakId);

        for (Answer answer : flat) {
            if (answer.getParentAnswer() == null) {
                roots.add(answer);
            } else {
                childrenByParentId
                    .computeIfAbsent(answer.getParentAnswer().getId(), ignored -> new ArrayList<>())
                    .add(answer);
            }
        }

        return roots.stream()
            .map(root -> buildTreeNode(root, childrenByParentId, currentUserId))
            .toList();
    }

    @Override
    public AnswerResponseDTO createTopLevel(
        Long questionId,
        CreateAnswerRequestDTO request,
        String keycloakId
    ) {
        Question question = questionService.findById(questionId);
        User author = resolveAuthor(keycloakId);

        Answer answer = Answer.builder()
            .question(question)
            .author(author)
            .body(request.getBody().trim())
            .anonymous(request.isAnonymous())
            .build();

        Answer saved = answerRepository.save(answer);
        questionRepository.incrementAnswerCount(question.getId());
        return toOwnedResponse(reload(saved.getId()));
    }

    @Override
    public AnswerResponseDTO createReply(
        Long parentAnswerId,
        CreateAnswerRequestDTO request,
        String keycloakId
    ) {
        Answer parent = answerRepository.findById(parentAnswerId)
            .orElseThrow(() -> new AnswerNotFoundException(parentAnswerId));

        if (parent.isDeleted()) {
            throw new BadRequestException("Cannot reply to a deleted answer");
        }

        User author = resolveAuthor(keycloakId);
        Question question = parent.getQuestion();

        Answer answer = Answer.builder()
            .question(question)
            .parentAnswer(parent)
            .author(author)
            .body(request.getBody().trim())
            .anonymous(request.isAnonymous())
            .build();

        Answer saved = answerRepository.save(answer);
        questionRepository.incrementAnswerCount(question.getId());
        return toOwnedResponse(reload(saved.getId()));
    }

    @Override
    public AnswerResponseDTO update(Long id, UpdateAnswerRequestDTO request, String keycloakId) {
        Answer answer = findOwnedAnswer(id, keycloakId);

        if (answer.isDeleted()) {
            throw new BadRequestException("Cannot update a deleted answer");
        }

        answer.setBody(request.getBody().trim());
        answer.setAnonymous(request.isAnonymous());
        Answer saved = answerRepository.save(answer);
        return toOwnedResponse(reload(saved.getId()));
    }

    @Override
    public void softDelete(Long id, String keycloakId) {
        Answer answer = findOwnedAnswer(id, keycloakId);

        if (answer.isDeleted()) {
            throw new BadRequestException("Answer is already deleted");
        }

        answer.setDeleted(true);
        answerRepository.save(answer);
        questionRepository.decrementAnswerCount(answer.getQuestion().getId());
    }

    private AnswerTreeNodeDTO buildTreeNode(
        Answer answer,
        Map<Long, List<Answer>> childrenByParentId,
        Long currentUserId
    ) {
        AnswerTreeNodeDTO node = answerMapper.toTreeNode(answer);
        node.setOwnedByCurrentUser(isOwnedBy(answer, currentUserId));
        List<Answer> children = childrenByParentId.getOrDefault(answer.getId(), List.of());
        node.setReplies(children.stream()
            .map(child -> buildTreeNode(child, childrenByParentId, currentUserId))
            .toList());
        return node;
    }

    private AnswerResponseDTO toOwnedResponse(Answer answer) {
        return answerMapper.toResponse(answer).toBuilder()
            .ownedByCurrentUser(true)
            .build();
    }

    private Long resolveCurrentUserId(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            return null;
        }
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .map(User::getId)
            .orElse(null);
    }

    private boolean isOwnedBy(Answer answer, Long currentUserId) {
        return currentUserId != null
            && !answer.isDeleted()
            && answer.getAuthor().getId().equals(currentUserId);
    }

    private Answer reload(Long id) {
        return answerRepository.findById(id)
            .orElseThrow(() -> new AnswerNotFoundException(id));
    }

    private User resolveAuthor(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }

    private Answer findOwnedAnswer(Long id, String keycloakId) {
        Answer answer = answerRepository.findById(id)
            .orElseThrow(() -> new AnswerNotFoundException(id));
        User author = resolveAuthor(keycloakId);
        if (!answer.getAuthor().getId().equals(author.getId())) {
            throw new ForbiddenException("You can only modify your own answers");
        }
        return answer;
    }
}
