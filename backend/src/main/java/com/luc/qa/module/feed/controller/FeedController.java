package com.luc.qa.module.feed.controller;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.feed.dto.FeedSort;
import com.luc.qa.module.feed.service.FeedService;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Tag(name = "Feed")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public PageResponseDTO<QuestionSummaryDTO> getFeed(
        @RequestParam(defaultValue = "NEW") FeedSort sort,
        @RequestParam(required = false) String community,
        @RequestParam(defaultValue = "true") boolean includeDescendants,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return feedService.getFeed(sort, community, includeDescendants, page, size);
    }
}
