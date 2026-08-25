package com.luc.qa.module.semantic.service;

import com.luc.qa.module.course.entity.Course;
import com.luc.qa.module.course.repository.CourseRepository;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.semantic.client.EmbeddingClient;
import com.luc.qa.module.semantic.store.EmbeddingStore;
import com.luc.qa.module.semantic.store.ScoredId;
import com.luc.qa.module.topic.entity.QuestionTopic;
import com.luc.qa.module.topic.entity.Topic;
import com.luc.qa.module.topic.entity.TopicSource;
import com.luc.qa.module.topic.repository.QuestionTopicRepository;
import com.luc.qa.module.topic.repository.TopicRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticIndexService {

    private static final int AUTO_TAG_LIMIT = 3;
    private static final double AUTO_TAG_MAX_DISTANCE = 0.55;

    private final EmbeddingClient embeddingClient;
    private final EmbeddingStore embeddingStore;
    private final TopicRepository topicRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionTopicRepository questionTopicRepository;
    private final ExpertiseService expertiseService;

    @Transactional
    public void indexQuestion(Question question) {
        if (question == null || question.getId() == null) {
            return;
        }
        String text = embedText(question.getTitle(), question.getBody());
        float[] vector = embeddingClient.embedOne(text);
        if (vector == null || vector.length != EmbeddingClient.DIMS) {
            return;
        }
        embeddingStore.saveQuestionEmbedding(question.getId(), vector);
        applyAutoTags(question, vector);
        expertiseService.recomputeAll();
    }

    @Transactional
    public int backfill() {
        if (!embeddingClient.probe()) {
            log.warn("Embedder is not reachable — skipping embedding backfill");
            return 0;
        }

        int updated = 0;
        updated += embedTopics();
        updated += embedCourses();
        updated += embedQuestions();
        expertiseService.recomputeAll();
        log.info("Embedding backfill updated {} rows", updated);
        return updated;
    }

    private int embedTopics() {
        List<Long> ids = embeddingStore.topicIdsMissingEmbedding();
        int count = 0;
        for (Long id : ids) {
            Topic topic = topicRepository.findById(id).orElse(null);
            if (topic == null) {
                continue;
            }
            float[] vector = embeddingClient.embedOne(embedText(topic.getName(), topic.getDescription()));
            if (vector == null) {
                break;
            }
            embeddingStore.saveTopicEmbedding(id, vector);
            count++;
        }
        return count;
    }

    private int embedCourses() {
        List<Long> ids = embeddingStore.courseIdsMissingEmbedding();
        int count = 0;
        for (Long id : ids) {
            Course course = courseRepository.findById(id).orElse(null);
            if (course == null) {
                continue;
            }
            String text = embedText(
                course.getCode() + " " + course.getTitle(),
                course.getDescription()
            );
            float[] vector = embeddingClient.embedOne(text);
            if (vector == null) {
                break;
            }
            embeddingStore.saveCourseEmbedding(id, vector);
            count++;
        }
        return count;
    }

    private int embedQuestions() {
        List<Long> ids = embeddingStore.questionIdsMissingEmbedding();
        int count = 0;
        for (Long id : ids) {
            Question question = questionRepository.findById(id).orElse(null);
            if (question == null) {
                continue;
            }
            float[] vector = embeddingClient.embedOne(embedText(question.getTitle(), question.getBody()));
            if (vector == null) {
                break;
            }
            embeddingStore.saveQuestionEmbedding(id, vector);
            applyAutoTags(question, vector);
            count++;
        }
        return count;
    }

    private void applyAutoTags(Question question, float[] vector) {
        List<ScoredId> nearest = embeddingStore.nearestTopics(vector, AUTO_TAG_LIMIT);
        questionTopicRepository.deleteAutoTagsForQuestion(question.getId());
        List<QuestionTopic> tags = new ArrayList<>();
        for (ScoredId scored : nearest) {
            if (scored.distance() > AUTO_TAG_MAX_DISTANCE) {
                continue;
            }
            if (questionTopicRepository.existsByQuestionIdAndTopicId(question.getId(), scored.id())) {
                continue;
            }
            Topic topic = topicRepository.findById(scored.id()).orElse(null);
            if (topic == null) {
                continue;
            }
            tags.add(QuestionTopic.builder()
                .question(question)
                .topic(topic)
                .confidence(Math.max(0, 1.0 - scored.distance()))
                .source(TopicSource.AUTO)
                .build());
        }
        if (!tags.isEmpty()) {
            questionTopicRepository.saveAll(tags);
        }
    }

    private static String embedText(String title, String body) {
        String safeTitle = title == null ? "" : title.trim();
        String safeBody = body == null ? "" : body.trim();
        if (safeBody.length() > 4000) {
            safeBody = safeBody.substring(0, 4000);
        }
        return (safeTitle + "\n" + safeBody).trim();
    }
}
