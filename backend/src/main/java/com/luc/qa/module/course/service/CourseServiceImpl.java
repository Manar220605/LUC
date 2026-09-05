package com.luc.qa.module.course.service;

import com.luc.qa.common.exception.CourseNotFoundException;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.repository.AlumniProfileRepository;
import com.luc.qa.module.course.dto.CourseDetailDTO;
import com.luc.qa.module.course.dto.CourseExpertDTO;
import com.luc.qa.module.course.dto.CourseSummaryDTO;
import com.luc.qa.module.course.entity.Course;
import com.luc.qa.module.course.mapper.CourseMapper;
import com.luc.qa.module.course.repository.CourseRepository;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.mapper.QuestionMapper;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.user.service.PublicAuthorService;
import com.luc.qa.module.vote.service.VoteService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private static final int PEOPLE_LIMIT = 8;

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final VoteService voteService;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final PublicAuthorService publicAuthorService;

    @Override
    public List<CourseSummaryDTO> listAll() {
        return courseRepository.findAllByOrderByYearLevelAscSemesterAscCodeAsc().stream()
            .map(courseMapper::toSummary)
            .toList();
    }

    @Override
    public CourseDetailDTO getByCode(String code, String keycloakId) {
        Course course = courseRepository.findWithTopicsByCodeIgnoreCase(code.trim())
            .orElseThrow(() -> new CourseNotFoundException(code));

        List<Question> linked = questionRepository.findByCourse_IdAndStatusNotOrderByCreatedAtDesc(
            course.getId(),
            QuestionStatus.DELETED
        );
        List<QuestionSummaryDTO> linkedDtos = toSummaries(linked, keycloakId);

        return CourseDetailDTO.builder()
            .course(courseMapper.toSummary(course))
            .description(course.getDescription())
            .topics(course.getTopics().stream()
                .sorted(Comparator.comparing(topic -> topic.getName().toLowerCase()))
                .map(courseMapper::toTopicSummary)
                .toList())
            .linkedQuestions(linkedDtos)
            .people(loadPeople(course.getId()))
            .build();
    }

    private List<QuestionSummaryDTO> toSummaries(List<Question> questions, String keycloakId) {
        List<QuestionSummaryDTO> dtos = questions.stream().map(questionMapper::toSummary).toList();
        voteService.enrichQuestionSummaries(dtos, questions, keycloakId);
        return dtos;
    }

    private List<CourseExpertDTO> loadPeople(long courseId) {
        List<ExpertRow> rows = jdbcTemplate.query(
            """
            SELECT ue.user_id AS user_id, SUM(ue.weight) AS weight
            FROM user_expertise ue
            INNER JOIN course_topics ct ON ct.topic_id = ue.topic_id
            INNER JOIN users u ON u.id = ue.user_id
            WHERE ct.course_id = ?
              AND u.is_banned = FALSE
            GROUP BY ue.user_id
            ORDER BY SUM(ue.weight) DESC
            LIMIT ?
            """,
            (rs, rowNum) -> new ExpertRow(rs.getLong("user_id"), rs.getDouble("weight")),
            courseId,
            PEOPLE_LIMIT
        );
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, User> users = userRepository.findAllById(
            rows.stream().map(ExpertRow::userId).toList()
        ).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, AlumniProfile> alumni = alumniProfileRepository.findAllById(
            rows.stream().map(ExpertRow::userId).toList()
        ).stream().collect(Collectors.toMap(AlumniProfile::getUserId, Function.identity()));

        List<CourseExpertDTO> people = new ArrayList<>();
        for (ExpertRow row : rows) {
            User user = users.get(row.userId());
            if (user == null) {
                continue;
            }
            AlumniProfile profile = alumni.get(user.getId());
            boolean isAlumni = user.getRole() == UserRole.ALUMNI
                && profile != null
                && profile.isPublicProfile();
            people.add(CourseExpertDTO.builder()
                .author(publicAuthorService.fromUser(user))
                .alumni(isAlumni)
                .currentCompany(isAlumni ? profile.getCurrentCompany() : null)
                .weight(row.weight())
                .build());
        }
        return people;
    }

    private record ExpertRow(long userId, double weight) {
    }
}
