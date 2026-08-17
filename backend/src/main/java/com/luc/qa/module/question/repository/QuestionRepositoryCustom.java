package com.luc.qa.module.question.repository;

import com.luc.qa.module.feed.dto.FeedSort;
import com.luc.qa.module.question.dto.QuestionFilterDTO;
import com.luc.qa.module.question.entity.Question;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionRepositoryCustom {

    Page<Question> findByFullTextSearch(QuestionFilterDTO filter, Pageable pageable);

    List<Question> findHotCandidatesByFullTextSearch(QuestionFilterDTO filter, int limit);

    long countByFullTextSearch(QuestionFilterDTO filter);
}
