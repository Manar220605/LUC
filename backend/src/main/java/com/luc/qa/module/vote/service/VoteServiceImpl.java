package com.luc.qa.module.vote.service;

import com.luc.qa.common.exception.AnswerNotFoundException;
import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.QuestionNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.common.exception.VoteNotAllowedException;
import com.luc.qa.module.answer.dto.AnswerTreeNodeDTO;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.question.dto.QuestionResponseDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.vote.dto.CastVoteRequestDTO;
import com.luc.qa.module.vote.dto.VoteResponseDTO;
import com.luc.qa.module.vote.entity.Vote;
import com.luc.qa.module.vote.entity.VoteTargetType;
import com.luc.qa.module.vote.repository.VoteRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Override
    @Retryable(
        retryFor = OptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 50)
    )
    public VoteResponseDTO castVote(CastVoteRequestDTO request, String keycloakId) {
        validateVoteValue(request.getValue());
        short voteValue = request.getValue().shortValue();
        User voter = resolveVoter(keycloakId);

        if (request.getTargetType() == VoteTargetType.QUESTION) {
            Question question = questionRepository.findByIdAndStatusNot(
                    request.getTargetId(),
                    QuestionStatus.DELETED
                )
                .orElseThrow(() -> new QuestionNotFoundException(request.getTargetId()));
            assertNotOwnContent(voter, question.getAuthor().getId());
            return applyVote(
                voter,
                VoteTargetType.QUESTION,
                question.getId(),
                voteValue,
                () -> questionRepository.findByIdAndStatusNot(question.getId(), QuestionStatus.DELETED)
                    .orElseThrow(() -> new QuestionNotFoundException(question.getId()))
                    .getScore(),
                score -> updateQuestionScore(question.getId(), score)
            );
        }

        Answer answer = answerRepository.findById(request.getTargetId())
            .orElseThrow(() -> new AnswerNotFoundException(request.getTargetId()));
        if (answer.isDeleted()) {
            throw new AnswerNotFoundException(request.getTargetId());
        }
        assertNotOwnContent(voter, answer.getAuthor().getId());
        return applyVote(
            voter,
            VoteTargetType.ANSWER,
            answer.getId(),
            voteValue,
            () -> answerRepository.findById(answer.getId())
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() -> new AnswerNotFoundException(answer.getId()))
                .getScore(),
            score -> updateAnswerScore(answer.getId(), score)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void enrichQuestionResponse(QuestionResponseDTO dto, Question question, String keycloakId) {
        Long voterId = resolveCurrentUserId(keycloakId);
        if (voterId == null) {
            return;
        }
        dto.setOwnedByCurrentUser(question.getAuthor().getId().equals(voterId));
        dto.setViewerVote(findViewerVote(voterId, VoteTargetType.QUESTION, question.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public void enrichQuestionSummary(QuestionSummaryDTO dto, Question question, String keycloakId) {
        Long voterId = resolveCurrentUserId(keycloakId);
        if (voterId == null) {
            return;
        }
        dto.setOwnedByCurrentUser(question.getAuthor().getId().equals(voterId));
        dto.setViewerVote(findViewerVote(voterId, VoteTargetType.QUESTION, question.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public void enrichQuestionSummaries(
        List<QuestionSummaryDTO> dtos,
        List<Question> questions,
        String keycloakId
    ) {
        Long voterId = resolveCurrentUserId(keycloakId);
        if (voterId == null || dtos.isEmpty()) {
            return;
        }

        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        Map<Long, Integer> votes = loadViewerVotes(voterId, VoteTargetType.QUESTION, questionIds);

        for (int i = 0; i < dtos.size(); i++) {
            QuestionSummaryDTO dto = dtos.get(i);
            Question question = questions.get(i);
            dto.setOwnedByCurrentUser(question.getAuthor().getId().equals(voterId));
            dto.setViewerVote(votes.get(question.getId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void enrichAnswerTree(List<AnswerTreeNodeDTO> tree, String keycloakId) {
        Long voterId = resolveCurrentUserId(keycloakId);
        if (voterId == null || tree.isEmpty()) {
            return;
        }

        List<Long> answerIds = collectAnswerIds(tree);
        Map<Long, Integer> votes = loadViewerVotes(voterId, VoteTargetType.ANSWER, answerIds);
        applyViewerVotes(tree, votes);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> findViewerVotes(
        String keycloakId,
        VoteTargetType targetType,
        List<Long> targetIds
    ) {
        Long voterId = resolveCurrentUserId(keycloakId);
        if (voterId == null || targetIds.isEmpty()) {
            return Map.of();
        }
        return loadViewerVotes(voterId, targetType, targetIds);
    }

    private VoteResponseDTO applyVote(
        User voter,
        VoteTargetType targetType,
        Long targetId,
        short value,
        IntSupplier scoreReader,
        ScoreUpdater scoreUpdater
    ) {
        Optional<Vote> existingVote = voteRepository.findByVoterIdAndTargetTypeAndTargetId(
            voter.getId(),
            targetType,
            targetId
        );

        Integer viewerVote;
        int scoreDelta;
        int voteAsInt = value;

        if (existingVote.isEmpty()) {
            voteRepository.save(Vote.builder()
                .voter(voter)
                .targetType(targetType)
                .targetId(targetId)
                .value(value)
                .build());
            viewerVote = voteAsInt;
            scoreDelta = voteAsInt;
        } else if (existingVote.get().getValue() == value) {
            voteRepository.delete(existingVote.get());
            viewerVote = null;
            scoreDelta = -voteAsInt;
        } else {
            Vote vote = existingVote.get();
            vote.setValue(value);
            voteRepository.save(vote);
            viewerVote = voteAsInt;
            scoreDelta = 2 * voteAsInt;
        }

        int currentScore = scoreReader.getAsInt();
        int newScore = currentScore + scoreDelta;
        scoreUpdater.update(newScore);

        return VoteResponseDTO.builder()
            .targetType(targetType)
            .targetId(targetId)
            .viewerVote(viewerVote)
            .score(newScore)
            .build();
    }

    private void updateQuestionScore(Long questionId, int newScore) {
        Question question = questionRepository.findByIdAndStatusNot(questionId, QuestionStatus.DELETED)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));
        question.setScore(newScore);
        questionRepository.save(question);
    }

    private void updateAnswerScore(Long answerId, int newScore) {
        Answer answer = answerRepository.findById(answerId)
            .filter(existing -> !existing.isDeleted())
            .orElseThrow(() -> new AnswerNotFoundException(answerId));
        answer.setScore(newScore);
        answerRepository.save(answer);
    }

    private void validateVoteValue(Integer value) {
        if (value == null || (value != -1 && value != 1)) {
            throw new BadRequestException("Vote value must be -1 or 1");
        }
    }

    private void assertNotOwnContent(User voter, Long authorId) {
        if (voter.getId().equals(authorId)) {
            throw new VoteNotAllowedException("You cannot vote on your own content");
        }
    }

    private User resolveVoter(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }

    private Long resolveCurrentUserId(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            return null;
        }
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .map(User::getId)
            .orElse(null);
    }

    private Integer findViewerVote(Long voterId, VoteTargetType targetType, Long targetId) {
        return voteRepository.findByVoterIdAndTargetTypeAndTargetId(voterId, targetType, targetId)
            .map(vote -> (int) vote.getValue())
            .orElse(null);
    }

    private Map<Long, Integer> loadViewerVotes(
        Long voterId,
        VoteTargetType targetType,
        List<Long> targetIds
    ) {
        if (targetIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> votes = new HashMap<>();
        voteRepository.findByVoterIdAndTargetTypeAndTargetIdIn(voterId, targetType, targetIds)
            .forEach(vote -> votes.put(vote.getTargetId(), (int) vote.getValue()));
        return votes;
    }

    private List<Long> collectAnswerIds(List<AnswerTreeNodeDTO> nodes) {
        List<Long> ids = new ArrayList<>();
        collectAnswerIds(nodes, ids);
        return ids;
    }

    private void collectAnswerIds(List<AnswerTreeNodeDTO> nodes, List<Long> ids) {
        for (AnswerTreeNodeDTO node : nodes) {
            ids.add(node.getId());
            collectAnswerIds(node.getReplies(), ids);
        }
    }

    private void applyViewerVotes(List<AnswerTreeNodeDTO> nodes, Map<Long, Integer> votes) {
        for (AnswerTreeNodeDTO node : nodes) {
            node.setViewerVote(votes.get(node.getId()));
            applyViewerVotes(node.getReplies(), votes);
        }
    }

    @FunctionalInterface
    private interface ScoreUpdater {
        void update(int newScore);
    }
}
