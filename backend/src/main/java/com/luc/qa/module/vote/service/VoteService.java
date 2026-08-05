package com.luc.qa.module.vote.service;

import com.luc.qa.module.answer.dto.AnswerTreeNodeDTO;
import com.luc.qa.module.question.dto.QuestionResponseDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.vote.dto.CastVoteRequestDTO;
import com.luc.qa.module.vote.dto.VoteResponseDTO;
import java.util.List;
import java.util.Map;

public interface VoteService {

    VoteResponseDTO castVote(CastVoteRequestDTO request, String keycloakId);

    void enrichQuestionResponse(QuestionResponseDTO dto, Question question, String keycloakId);

    void enrichQuestionSummary(QuestionSummaryDTO dto, Question question, String keycloakId);

    void enrichQuestionSummaries(List<QuestionSummaryDTO> dtos, List<Question> questions, String keycloakId);

    void enrichAnswerTree(List<AnswerTreeNodeDTO> tree, String keycloakId);

    Map<Long, Integer> findViewerVotes(String keycloakId, com.luc.qa.module.vote.entity.VoteTargetType targetType, List<Long> targetIds);
}
