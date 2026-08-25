package com.luc.qa.module.topic.repository;

import com.luc.qa.module.topic.entity.QuestionTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionTopicRepository extends JpaRepository<QuestionTopic, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM QuestionTopic qt WHERE qt.question.id = :questionId AND qt.source = com.luc.qa.module.topic.entity.TopicSource.AUTO")
    void deleteAutoTagsForQuestion(@Param("questionId") Long questionId);

    boolean existsByQuestionIdAndTopicId(Long questionId, Long topicId);
}
