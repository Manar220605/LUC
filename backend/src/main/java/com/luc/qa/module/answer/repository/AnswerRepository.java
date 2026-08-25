package com.luc.qa.module.answer.repository;

import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.question.entity.QuestionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @EntityGraph(attributePaths = {"author", "parentAnswer"})
    List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);

    @EntityGraph(attributePaths = {"author", "question", "parentAnswer"})
    Optional<Answer> findById(Long id);

    @EntityGraph(attributePaths = {"question"})
    Page<Answer> findByAuthor_IdAndAnonymousFalseAndDeletedFalseAndQuestion_StatusNot(
        Long authorId,
        QuestionStatus status,
        Pageable pageable
    );

    long countByAuthor_IdAndAnonymousFalseAndDeletedFalseAndQuestion_StatusNot(
        Long authorId,
        QuestionStatus status
    );

    @Query("""
        SELECT COALESCE(SUM(a.score), 0)
        FROM Answer a
        WHERE a.author.id = :authorId
          AND a.anonymous = false
          AND a.deleted = false
          AND a.question.status <> com.luc.qa.module.question.entity.QuestionStatus.DELETED
        """)
    Long sumPublicScoreByAuthor(@Param("authorId") Long authorId);
}
