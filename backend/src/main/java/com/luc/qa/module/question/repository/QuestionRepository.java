package com.luc.qa.module.question.repository;

import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository
    extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    @EntityGraph(attributePaths = {"author", "community", "community.parent"})
    Optional<Question> findByIdAndStatusNot(Long id, QuestionStatus status);

    @EntityGraph(attributePaths = {"author", "community"})
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);

    @Modifying
    @Query("UPDATE Question q SET q.viewCount = q.viewCount + 1 WHERE q.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Question q SET q.answerCount = q.answerCount + 1 WHERE q.id = :id")
    void incrementAnswerCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Question q SET q.answerCount = q.answerCount - 1 WHERE q.id = :id AND q.answerCount > 0")
    void decrementAnswerCount(@Param("id") Long id);
}
