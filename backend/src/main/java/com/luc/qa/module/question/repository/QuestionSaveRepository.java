package com.luc.qa.module.question.repository;

import com.luc.qa.module.question.entity.QuestionSave;
import com.luc.qa.module.question.entity.QuestionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSaveRepository extends JpaRepository<QuestionSave, Long> {

    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);

    void deleteByUserIdAndQuestionId(Long userId, Long questionId);

    @EntityGraph(attributePaths = {"question", "question.author", "question.community"})
    List<QuestionSave> findByUser_IdAndQuestion_StatusNotOrderByCreatedAtDesc(
        Long userId,
        QuestionStatus status
    );
}
