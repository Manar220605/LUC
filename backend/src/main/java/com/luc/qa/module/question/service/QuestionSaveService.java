package com.luc.qa.module.question.service;

import com.luc.qa.module.question.dto.QuestionSaveResponseDTO;
import com.luc.qa.module.question.dto.SaveStatusDTO;
import java.util.List;

public interface QuestionSaveService {

    SaveStatusDTO save(String keycloakId, Long questionId);

    SaveStatusDTO unsave(String keycloakId, Long questionId);

    SaveStatusDTO status(String keycloakId, Long questionId);

    List<QuestionSaveResponseDTO> listMine(String keycloakId);
}
