package com.luc.qa.module.answer.repository;

import com.luc.qa.module.answer.entity.Answer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @EntityGraph(attributePaths = {"author", "parentAnswer"})
    List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);

    @EntityGraph(attributePaths = {"author", "question", "parentAnswer"})
    Optional<Answer> findById(Long id);
}
