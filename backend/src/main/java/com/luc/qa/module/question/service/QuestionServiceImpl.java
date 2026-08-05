package com.luc.qa.module.question.service;

import com.luc.qa.common.exception.CommunityNotFoundException;
import com.luc.qa.common.exception.ForbiddenException;
import com.luc.qa.common.exception.QuestionNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.community.repository.CommunityRepository;
import com.luc.qa.module.feed.dto.FeedSort;
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

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final QuestionMapper questionMapper;
    private final VoteService voteService;

    @Override
    @Transactional(readOnly = true)
    public Question findById(Long id) {
        return questionRepository.findByIdAndStatusNot(id, QuestionStatus.DELETED)
            .orElseThrow(() -> new QuestionNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionSummaryDTO> findFeed(QuestionFilterDTO filter, Pageable pageable, String keycloakId) {
        Specification<Question> spec = Specification.where(
            QuestionSpecifications.hasStatus(QuestionStatus.OPEN)
        );

        if (filter.getCommunityPath() != null && !filter.getCommunityPath().isBlank()) {
            spec = spec.and(filter.isIncludeDescendants()
                ? QuestionSpecifications.inCommunityOrDescendants(filter.getCommunityPath())
                : QuestionSpecifications.inCommunityExact(filter.getCommunityPath()));
        }

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            spec = spec.and(QuestionSpecifications.titleOrBodyContains(filter.getSearch()));
        }

        Pageable sortedPageable = pageable;
        FeedSort sort = filter.getSort() != null ? filter.getSort() : FeedSort.NEW;
        if (sort == FeedSort.NEW || sort == FeedSort.HOT || sort == FeedSort.TOP) {
            sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        Page<Question> questions = questionRepository.findAll(spec, sortedPageable);
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
        return saved;
    }

    @Override
    public Question update(Long id, UpdateQuestionRequestDTO request, String keycloakId) {
        Question question = findOwnedQuestion(id, keycloakId);
        question.setTitle(request.getTitle().trim());
        question.setBody(request.getBody());
        question.setAnonymous(request.isAnonymous());
        return questionRepository.save(question);
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
