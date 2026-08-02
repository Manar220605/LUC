package com.luc.qa.module.question.service;

import com.luc.qa.module.question.dto.CreateQuestionRequestDTO;
import com.luc.qa.module.question.dto.QuestionFilterDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.dto.UpdateQuestionRequestDTO;
import com.luc.qa.module.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService {

    Question findById(Long id);

    Page<QuestionSummaryDTO> findFeed(QuestionFilterDTO filter, Pageable pageable);

    Question create(CreateQuestionRequestDTO request, String keycloakId);

    Question update(Long id, UpdateQuestionRequestDTO request, String keycloakId);

    void softDelete(Long id, String keycloakId);

    void incrementView(Long id);
}
