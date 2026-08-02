package com.luc.qa.module.question.dto;

import com.luc.qa.module.feed.dto.FeedSort;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionFilterDTO {

    private String communityPath;
    private boolean includeDescendants = true;
    private FeedSort sort = FeedSort.NEW;
    private String search;
}
