package com.luc.qa.module.feed.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.feed.dto.FeedSort;
import com.luc.qa.module.question.dto.QuestionFilterDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;

public interface FeedService {

    PageResponseDTO<QuestionSummaryDTO> getFeed(
        FeedSort sort,
        String communityPath,
        boolean includeDescendants,
        String search,
        int page,
        int size,
        String keycloakId
    );
}
