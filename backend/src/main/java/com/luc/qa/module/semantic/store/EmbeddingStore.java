package com.luc.qa.module.semantic.store;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmbeddingStore {

    private final JdbcTemplate jdbcTemplate;

    public void saveTopicEmbedding(long id, float[] vector) {
        jdbcTemplate.update(
            "UPDATE topics SET embedding = CAST(? AS vector) WHERE id = ?",
            toLiteral(vector),
            id
        );
    }

    public void saveCourseEmbedding(long id, float[] vector) {
        jdbcTemplate.update(
            "UPDATE courses SET embedding = CAST(? AS vector) WHERE id = ?",
            toLiteral(vector),
            id
        );
    }

    public void saveQuestionEmbedding(long id, float[] vector) {
        jdbcTemplate.update(
            """
            UPDATE questions
            SET embedding = CAST(? AS vector), embedded_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """,
            toLiteral(vector),
            id
        );
    }

    public List<Long> topicIdsMissingEmbedding() {
        return jdbcTemplate.queryForList(
            "SELECT id FROM topics WHERE embedding IS NULL ORDER BY id",
            Long.class
        );
    }

    public List<Long> courseIdsMissingEmbedding() {
        return jdbcTemplate.queryForList(
            "SELECT id FROM courses WHERE embedding IS NULL ORDER BY id",
            Long.class
        );
    }

    public List<Long> questionIdsMissingEmbedding() {
        return jdbcTemplate.queryForList(
            """
            SELECT id FROM questions
            WHERE embedding IS NULL AND status <> 'DELETED'
            ORDER BY id
            """,
            Long.class
        );
    }

    public float[] loadCourseEmbedding(long courseId) {
        List<String> rows = jdbcTemplate.queryForList(
            "SELECT embedding::text FROM courses WHERE id = ? AND embedding IS NOT NULL",
            String.class,
            courseId
        );
        if (rows.isEmpty()) {
            return null;
        }
        return parseLiteral(rows.getFirst());
    }

    public List<ScoredId> nearestTopics(float[] vector, int limit) {
        return jdbcTemplate.query(
            """
            SELECT id, (embedding <=> CAST(? AS vector)) AS distance
            FROM topics
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(? AS vector)
            LIMIT ?
            """,
            (rs, rowNum) -> new ScoredId(rs.getLong("id"), rs.getDouble("distance")),
            toLiteral(vector),
            toLiteral(vector),
            limit
        );
    }

    public List<ScoredId> relatedQuestions(long courseId, float[] courseVector, int limit) {
        return jdbcTemplate.query(
            """
            SELECT q.id, (q.embedding <=> CAST(? AS vector)) AS distance
            FROM questions q
            WHERE q.status <> 'DELETED'
              AND q.embedding IS NOT NULL
              AND (q.course_id IS NULL OR q.course_id <> ?)
            ORDER BY q.embedding <=> CAST(? AS vector)
            LIMIT ?
            """,
            (rs, rowNum) -> new ScoredId(rs.getLong("id"), rs.getDouble("distance")),
            toLiteral(courseVector),
            courseId,
            toLiteral(courseVector),
            limit
        );
    }

    public static String toLiteral(float[] vector) {
        StringBuilder builder = new StringBuilder(vector.length * 8);
        builder.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }

    private static float[] parseLiteral(String literal) {
        String body = literal.replace("[", "").replace("]", "");
        String[] parts = body.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }
}
