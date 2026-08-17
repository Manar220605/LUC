package com.luc.qa.module.question.service;

import com.luc.qa.common.exception.AnswerNotFoundException;
import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.CommunityNotFoundException;
import com.luc.qa.common.exception.ForbiddenException;
import com.luc.qa.common.exception.QuestionNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.answer.repository.AnswerRepository;
import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.community.repository.CommunityRepository;
import com.luc.qa.module.feed.dto.FeedSort;
import com.luc.qa.module.feed.service.FeedHotScore;
import com.luc.qa.module.notification.service.NotificationService;
import com.luc.qa.module.question.dto.CreateQuestionRequestDTO;
import com.luc.qa.module.question.dto.QuestionFilterDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.dto.UpdateQuestionRequestDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.question.mapper.QuestionMapper;
import com.luc.qa.module.question.repository.QuestionRepository;
import com.luc.qa.module.question.specification.QuestionSpecifications;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.vote.service.VoteService;
import com.luc.qa.module.vote.entity.VoteTargetType;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private static final int HOT_CANDIDATE_LIMIT = 200;

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final QuestionMapper questionMapper;
    private final VoteService voteService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Question findById(Long id) {
        return questionRepository.findByIdAndStatusNot(id, QuestionStatus.DELETED)
            .orElseThrow(() -> new QuestionNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionSummaryDTO> findFeed(QuestionFilterDTO filter, Pageable pageable, String keycloakId) {
        FeedSort sort = filter.getSort() != null ? filter.getSort() : FeedSort.NEW;
        boolean hasSearch = filter.getSearch() != null && !filter.getSearch().isBlank();

        if (hasSearch) {
            if (sort == FeedSort.HOT) {
                return findHotFullTextFeed(filter, pageable, keycloakId);
            }

            Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
            );
            Page<Question> questions = questionRepository.findByFullTextSearch(filter, sortedPageable);
            return toSummaryPage(questions, keycloakId);
        }

        Specification<Question> spec = buildFeedSpecification(filter);

        if (sort == FeedSort.HOT) {
            return findHotFeed(spec, pageable, keycloakId);
        }

        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort == FeedSort.TOP
                ? Sort.by(Sort.Direction.DESC, "score", "createdAt", "id")
                : Sort.by(Sort.Direction.DESC, "createdAt", "id")
        );

        Page<Question> questions = questionRepository.findAll(spec, sortedPageable);
        return toSummaryPage(questions, keycloakId);
    }

    private Page<QuestionSummaryDTO> findHotFullTextFeed(
        QuestionFilterDTO filter,
        Pageable pageable,
        String keycloakId
    ) {
        List<Question> candidates = questionRepository.findHotCandidatesByFullTextSearch(
            filter,
            HOT_CANDIDATE_LIMIT
        );
        Instant now = Instant.now();

        List<Question> ranked = candidates.stream()
            .sorted(Comparator
                .comparingDouble((Question question) -> FeedHotScore.compute(question, now))
                .reversed()
                .thenComparing(Question::getCreatedAt, Comparator.reverseOrder())
                .thenComparing(Question::getId, Comparator.reverseOrder()))
            .toList();

        int start = Math.toIntExact(pageable.getOffset());
        int end = Math.min(start + pageable.getPageSize(), ranked.size());
        List<Question> pageContent = start >= ranked.size() ? List.of() : ranked.subList(start, end);

        long totalMatching = questionRepository.countByFullTextSearch(filter);
        long totalElements = Math.min(totalMatching, HOT_CANDIDATE_LIMIT);

        List<QuestionSummaryDTO> summaries = pageContent.stream()
            .map(questionMapper::toSummary)
            .toList();
        voteService.enrichQuestionSummaries(summaries, pageContent, keycloakId);
        return new PageImpl<>(summaries, pageable, totalElements);
    }

    private Specification<Question> buildFeedSpecification(QuestionFilterDTO filter) {
        Specification<Question> spec = Specification.where(
            QuestionSpecifications.hasStatus(QuestionStatus.OPEN)
        );

        if (filter.getCommunityPath() != null && !filter.getCommunityPath().isBlank()) {
            spec = spec.and(filter.isIncludeDescendants()
                ? QuestionSpecifications.inCommunityOrDescendants(filter.getCommunityPath())
                : QuestionSpecifications.inCommunityExact(filter.getCommunityPath()));
        }

        return spec;
    }

    private Page<QuestionSummaryDTO> findHotFeed(
        Specification<Question> spec,
        Pageable pageable,
        String keycloakId
    ) {
        Pageable recencyPage = PageRequest.of(
            0,
            HOT_CANDIDATE_LIMIT,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        List<Question> candidates = questionRepository.findAll(spec, recencyPage).getContent();
        Instant now = Instant.now();

        List<Question> ranked = candidates.stream()
            .sorted(Comparator
                .comparingDouble((Question question) -> FeedHotScore.compute(question, now))
                .reversed()
                .thenComparing(Question::getCreatedAt, Comparator.reverseOrder())
                .thenComparing(Question::getId, Comparator.reverseOrder()))
            .toList();

        int start = Math.toIntExact(pageable.getOffset());
        int end = Math.min(start + pageable.getPageSize(), ranked.size());
        List<Question> pageContent = start >= ranked.size() ? List.of() : ranked.subList(start, end);

        long totalMatching = questionRepository.count(spec);
        long totalElements = Math.min(totalMatching, HOT_CANDIDATE_LIMIT);

        List<QuestionSummaryDTO> summaries = pageContent.stream()
            .map(questionMapper::toSummary)
            .toList();
        voteService.enrichQuestionSummaries(summaries, pageContent, keycloakId);
        return new PageImpl<>(summaries, pageable, totalElements);
    }

    private Page<QuestionSummaryDTO> toSummaryPage(Page<Question> questions, String keycloakId) {
        List<QuestionSummaryDTO> summaries = questions.getContent().stream()
            .map(questionMapper::toSummary)
            .toList();
        voteService.enrichQuestionSummaries(summaries, questions.getContent(), keycloakId);
        return new PageImpl<>(summaries, questions.getPageable(), questions.getTotalElements());
    }

    @Override
    public Question create(CreateQuestionRequestDTO request, String keycloakId) {
        User author = userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));

        Community community = communityRepository.findByPath(request.getCommunityPath().trim())
            .orElseThrow(() -> new CommunityNotFoundException(request.getCommunityPath()));

        Question question = Question.builder()
            .author(author)
            .community(community)
            .title(request.getTitle().trim())
            .body(request.getBody())
            .anonymous(request.isAnonymous())
            .status(QuestionStatus.OPEN)
            .build();

        Question saved = questionRepository.save(question);
        communityRepository.incrementQuestionCount(community.getId());
        notificationService.notifyMentions(
            author,
            request.getBody(),
            saved,
            VoteTargetType.QUESTION,
            saved.getId(),
            request.isAnonymous()
        );
        return saved;
    }

    @Override
    public Question update(Long id, UpdateQuestionRequestDTO request, String keycloakId) {
        Question question = findOwnedQuestion(id, keycloakId);
        question.setTitle(request.getTitle().trim());
        question.setBody(request.getBody());
        question.setAnonymous(request.isAnonymous());
        Question saved = questionRepository.save(question);
        notificationService.notifyMentions(
            question.getAuthor(),
            request.getBody(),
            saved,
            VoteTargetType.QUESTION,
            saved.getId(),
            request.isAnonymous()
        );
        return saved;
    }

    @Override
    public void softDelete(Long id, String keycloakId) {
        Question question = findOwnedQuestion(id, keycloakId);
        question.setStatus(QuestionStatus.DELETED);
        questionRepository.save(question);
    }

    @Override
    public void incrementView(Long id) {
        questionRepository.incrementViewCount(id);
    }

    @Override
    public Question acceptAnswer(Long questionId, Long answerId, String keycloakId) {
        Question question = findOwnedQuestion(questionId, keycloakId);
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new AnswerNotFoundException(answerId));

        if (!answer.getQuestion().getId().equals(question.getId())) {
            throw new BadRequestException("Answer does not belong to this question");
        }
        if (answer.isDeleted()) {
            throw new BadRequestException("Cannot accept a deleted answer");
        }
        if (answer.getParentAnswer() != null) {
            throw new BadRequestException("Only top-level answers can be accepted");
        }

        question.setAcceptedAnswer(answer);
        return questionRepository.save(question);
    }

    @Override
    public Question unacceptAnswer(Long questionId, String keycloakId) {
        Question question = findOwnedQuestion(questionId, keycloakId);
        question.setAcceptedAnswer(null);
        return questionRepository.save(question);
    }

    private Question findOwnedQuestion(Long id, String keycloakId) {
        Question question = findById(id);
        User author = userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
        if (!question.getAuthor().getId().equals(author.getId())) {
            throw new ForbiddenException("You can only modify your own questions");
        }
        return question;
    }
}
