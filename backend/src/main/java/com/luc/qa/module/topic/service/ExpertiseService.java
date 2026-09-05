package com.luc.qa.module.topic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpertiseService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void recomputeAll() {
        jdbcTemplate.update("DELETE FROM user_expertise");
        jdbcTemplate.update(
            """
            INSERT INTO user_expertise (
                user_id, topic_id, weight, evidence_count, computed_at, created_at, updated_at, version
            )
            SELECT
                a.author_id,
                qt.topic_id,
                SUM(
                    CASE WHEN q.accepted_answer_id = a.id THEN 3.0 ELSE 1.0 END
                    + GREATEST(a.score, 0) * 0.1
                ) AS weight,
                COUNT(*) AS evidence_count,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP,
                0
            FROM answers a
            INNER JOIN questions q ON q.id = a.question_id
            INNER JOIN question_topics qt ON qt.question_id = q.id
            WHERE a.is_deleted = FALSE
              AND a.is_anonymous = FALSE
              AND q.status <> 'DELETED'
            GROUP BY a.author_id, qt.topic_id
            """
        );
    }
}
