package com.luc.qa.module.answer.service;

import com.luc.qa.module.answer.dto.AnswerResponseDTO;
import com.luc.qa.module.answer.dto.AnswerTreeNodeDTO;
import com.luc.qa.module.answer.dto.CreateAnswerRequestDTO;
import com.luc.qa.module.answer.dto.UpdateAnswerRequestDTO;
import java.util.List;

public interface AnswerService {

    List<AnswerTreeNodeDTO> getAnswerTree(Long questionId, String keycloakId);

    AnswerResponseDTO createTopLevel(Long questionId, CreateAnswerRequestDTO request, String keycloakId);

    AnswerResponseDTO createReply(Long parentAnswerId, CreateAnswerRequestDTO request, String keycloakId);

    AnswerResponseDTO update(Long id, UpdateAnswerRequestDTO request, String keycloakId);

    void softDelete(Long id, String keycloakId);
}
