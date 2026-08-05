package com.luc.qa.module.feed.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.feed.dto.FeedSort;
import com.luc.qa.module.question.dto.QuestionFilterDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final QuestionService questionService;

    @Override
    public PageResponseDTO<QuestionSummaryDTO> getFeed(
        FeedSort sort,
        String communityPath,
        boolean includeDescendants,
        int page,
        int size,
        String keycloakId
    ) {
        QuestionFilterDTO filter = new QuestionFilterDTO();
        filter.setSort(sort != null ? sort : FeedSort.NEW);
        filter.setCommunityPath(communityPath);
        filter.setIncludeDescendants(includeDescendants);

        Page<QuestionSummaryDTO> result = questionService.findFeed(
            filter,
            PageRequest.of(page, size),
            keycloakId
        );

        return PageResponseDTO.<QuestionSummaryDTO>builder()
            .content(result.getContent())
            .page(result.getNumber())
            .size(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .build();
    }
}
