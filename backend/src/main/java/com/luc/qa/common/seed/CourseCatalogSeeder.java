package com.luc.qa.common.seed;

import com.luc.qa.module.course.entity.Course;
import com.luc.qa.module.course.repository.CourseRepository;
import com.luc.qa.module.topic.entity.Topic;
import com.luc.qa.module.topic.entity.TopicKind;
import com.luc.qa.module.topic.repository.TopicRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class CourseCatalogSeeder implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final CourseRepository courseRepository;

    @Override
    public void run(String... args) {
        seedTopics();
        seedCourses();
    }

    private void seedTopics() {
        try (BufferedReader reader = open("seed/topics.csv")) {
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            int inserted = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", 4);
                if (parts.length < 4) {
                    continue;
                }
                String slug = parts[0].trim();
                if (topicRepository.existsBySlug(slug)) {
                    continue;
                }
                topicRepository.save(Topic.builder()
                    .slug(slug)
                    .name(parts[1].trim())
                    .kind(TopicKind.valueOf(parts[2].trim()))
                    .description(parts[3].trim())
                    .build());
                inserted++;
            }
            if (inserted > 0) {
                log.info("Seeded {} topics", inserted);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to seed topics", ex);
        }
    }

    private void seedCourses() {
        try (BufferedReader reader = open("seed/courses.csv")) {
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            int inserted = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", 7);
                if (parts.length < 7) {
                    continue;
                }
                String code = parts[0].trim();
                if (courseRepository.existsByCodeIgnoreCase(code)) {
                    continue;
                }
                Set<Topic> topics = new HashSet<>();
                for (String slug : parts[6].split("\\|")) {
                    topicRepository.findBySlug(slug.trim()).ifPresent(topics::add);
                }
                courseRepository.save(Course.builder()
                    .code(code)
                    .title(parts[1].trim())
                    .yearLevel(Integer.parseInt(parts[2].trim()))
                    .semester(Integer.parseInt(parts[3].trim()))
                    .credits(Integer.parseInt(parts[4].trim()))
                    .description(parts[5].trim())
                    .topics(topics)
                    .build());
                inserted++;
            }
            if (inserted > 0) {
                log.info("Seeded {} courses", inserted);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to seed courses", ex);
        }
    }

    private BufferedReader open(String path) throws Exception {
        return new BufferedReader(new InputStreamReader(
            new ClassPathResource(path).getInputStream(),
            StandardCharsets.UTF_8
        ));
    }
}
