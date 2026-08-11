package com.luc.qa.common.seed;

import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.community.repository.CommunityRepository;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.vote.entity.Vote;
import com.luc.qa.module.vote.entity.VoteTargetType;
import com.luc.qa.module.vote.repository.VoteRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SampleContentSeeder implements CommandLineRunner {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;

    private static final String[] SAMPLE_AUTHORS = {
        "Rami Chaar", "Nadine Haddad", "Karim Nasser", "Layla Mokdad",
        "Omar Fakhoury", "Zeina Salloum", "Elie Rizk", "Maya Aoun"
    };

    private static final String[][] QUESTION_TEMPLATES = {
        {"How do I get started with {0}?",
         "Looking for a solid starting point in {0}. What resources helped you the most when you were beginning?"},
        {"Best courses at LU for {0}?",
         "Which LU courses actually cover {0} well? Which professors go into practical detail vs just theory?"},
        {"Anyone doing an internship in {0}?",
         "Trying to line up a summer internship in {0}. Any Beirut companies actively taking interns? How was your experience?"},
        {"Career prospects in {0} — Lebanon vs abroad?",
         "Realistic take on {0} — is it worth staying local, or should I plan for opportunities abroad?"},
        {"Free resources for learning {0}?",
         "Broke student here — dropping any high-quality free tutorials, YouTube channels, or open courses for {0}."}
    };

    private static final String[] ANSWER_TEMPLATES = {
        "Great question. When I started, I found the best approach was to combine theory with a hands-on project. Pick one small thing and finish it end-to-end.",
        "Honestly, LU covers the fundamentals well. Beyond that, MIT OpenCourseWare and freeCodeCamp took me from beginner to comfortable.",
        "Depends on your goals. If you want depth, follow a structured curriculum. If you want speed, build projects and learn what you need as you go.",
        "I did this last year — DM me if you want the full breakdown. Short version: start small, be consistent, and don't chase every new tool.",
        "One tip that changed things for me: join a Discord community focused on this. Being around people asking questions daily accelerated me hugely.",
        "Skip the tutorials after your first two. Build something real, even if it's ugly. You learn 10x faster that way."
    };

    private static final String[] REPLY_TEMPLATES = {
        "Agree with this. I did the same and it worked out.",
        "Which specific resource would you point to first?",
        "This is spot-on. Nothing beats shipping something small.",
        "Was going to say the same. Consistency > intensity."
    };

    private final Random random = new Random(42);

    @Override
    @Transactional
    public void run(String... args) {
        if (questionRepository.count() > 0) {
            log.info("Sample content already seeded — skipping.");
            return;
        }

        List<Community> communities = communityRepository.findAll();
        if (communities.isEmpty()) {
            log.warn("No communities found — sample content seeding aborted.");
            return;
        }

        List<User> authors = seedAuthors();
        log.info("Seeded {} sample authors", authors.size());

        int questionsCreated = 0;
        int answersCreated = 0;
        int votesCreated = 0;

        for (Community community : communities) {
            int perCommunity = 2 + random.nextInt(2);
            for (int q = 0; q < perCommunity; q++) {
                String[] template = QUESTION_TEMPLATES[random.nextInt(QUESTION_TEMPLATES.length)];
                User author = authors.get(random.nextInt(authors.size()));

                Question question = Question.builder()
                    .author(author)
                    .community(community)
                    .title(template[0].replace("{0}", community.getName()))
                    .body(template[1].replace("{0}", community.getName()))
                    .anonymous(random.nextInt(5) == 0)
                    .status(QuestionStatus.OPEN)
                    .viewCount(5 + random.nextInt(200))
                    .build();
                question = questionRepository.save(question);
                questionsCreated++;

                votesCreated += castVotes(question.getId(), VoteTargetType.QUESTION, author, authors, question);

                int topLevel = random.nextInt(4);
                int totalAnswers = 0;
                for (int a = 0; a < topLevel; a++) {
                    User answerAuthor = authors.get(random.nextInt(authors.size()));
                    Answer answer = Answer.builder()
                        .question(question)
                        .author(answerAuthor)
                        .body(ANSWER_TEMPLATES[random.nextInt(ANSWER_TEMPLATES.length)])
                        .anonymous(false)
                        .deleted(false)
                        .build();
                    answer = answerRepository.save(answer);
                    answersCreated++;
                    totalAnswers++;

                    votesCreated += castVotes(answer.getId(), VoteTargetType.ANSWER, answerAuthor, authors, answer);

                    int replies = random.nextInt(3);
                    for (int r = 0; r < replies; r++) {
                        User replyAuthor = authors.get(random.nextInt(authors.size()));
                        Answer reply = Answer.builder()
                            .question(question)
                            .parentAnswer(answer)
                            .author(replyAuthor)
                            .body(REPLY_TEMPLATES[random.nextInt(REPLY_TEMPLATES.length)])
                            .anonymous(false)
                            .deleted(false)
                            .build();
                        reply = answerRepository.save(reply);
                        answersCreated++;
                        totalAnswers++;

                        votesCreated += castVotes(reply.getId(), VoteTargetType.ANSWER, replyAuthor, authors, reply);
                    }
                }
                question.setAnswerCount(totalAnswers);
            }
            community.setQuestionCount(perCommunity);
        }

        log.info("Seeded {} questions, {} answers, {} votes across {} communities",
            questionsCreated, answersCreated, votesCreated, communities.size());
    }

    private List<User> seedAuthors() {
        List<User> authors = new ArrayList<>();
        for (String name : SAMPLE_AUTHORS) {
            String email = name.toLowerCase().replace(" ", ".") + "@example.lb";
            User user = User.builder()
                .keycloakId(UUID.randomUUID())
                .email(email)
                .displayName(name)
                .role(UserRole.MEMBER)
                .banned(false)
                .build();
            authors.add(userRepository.save(user));
        }
        return authors;
    }

    private int castVotes(Long targetId, VoteTargetType targetType, User owner, List<User> voters, Object target) {
        int cast = 0;
        int score = 0;
        for (User voter : voters) {
            if (Objects.equals(voter.getId(), owner.getId())) {
                continue;
            }
            int r = random.nextInt(10);
            short value;
            if (r < 6) {
                value = 1;
            } else if (r < 7) {
                value = -1;
            } else {
                continue;
            }
            voteRepository.save(Vote.builder()
                .voter(voter)
                .targetType(targetType)
                .targetId(targetId)
                .value(value)
                .build());
            cast++;
            score += value;
        }
        if (target instanceof Question q) {
            q.setScore(score);
        } else if (target instanceof Answer a) {
            a.setScore(score);
        }
        return cast;
    }
}
