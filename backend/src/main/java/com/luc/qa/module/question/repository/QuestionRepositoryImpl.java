package com.luc.qa.module.question.repository;

import com.luc.qa.module.feed.dto.FeedSort;
import com.luc.qa.module.question.dto.QuestionFilterDTO;
import com.luc.qa.module.question.entity.Question;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    private static final String BASE_FROM = """
        FROM questions q
        INNER JOIN communities c ON c.id = q.community_id
        WHERE q.status = 'OPEN'
        AND q.search_vector @@ plainto_tsquery('english', :search)
        """;

    private final EntityManager entityManager;

    @Override
    public Page<Question> findByFullTextSearch(QuestionFilterDTO filter, Pageable pageable) {
        String search = normalizeSearch(filter.getSearch());
        Map<String, Object> params = new HashMap<>();
        params.put("search", search);

        String where = BASE_FROM + communityClause(filter, params);
        String orderBy = orderByClause(filter.getSort(), true);

        Query countQuery = entityManager.createNativeQuery("SELECT count(*) " + where);
        bindParams(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query dataQuery = entityManager.createNativeQuery(
            "SELECT q.* " + where + orderBy,
            Question.class
        );
        bindParams(dataQuery, params);
        dataQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Question> content = dataQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Question> findHotCandidatesByFullTextSearch(QuestionFilterDTO filter, int limit) {
        String search = normalizeSearch(filter.getSearch());
        Map<String, Object> params = new HashMap<>();
        params.put("search", search);

        String where = BASE_FROM + communityClause(filter, params);
        String orderBy = " ORDER BY q.created_at DESC, q.id DESC";

        Query dataQuery = entityManager.createNativeQuery(
            "SELECT q.* " + where + orderBy,
            Question.class
        );
        bindParams(dataQuery, params);
        dataQuery.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<Question> content = dataQuery.getResultList();
        return content;
    }

    @Override
    public long countByFullTextSearch(QuestionFilterDTO filter) {
        String search = normalizeSearch(filter.getSearch());
        Map<String, Object> params = new HashMap<>();
        params.put("search", search);

        String where = BASE_FROM + communityClause(filter, params);
        Query countQuery = entityManager.createNativeQuery("SELECT count(*) " + where);
        bindParams(countQuery, params);
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    private static String normalizeSearch(String search) {
        if (!StringUtils.hasText(search)) {
            throw new IllegalArgumentException("search is required for full-text query");
        }
        return search.trim();
    }

    private static String communityClause(QuestionFilterDTO filter, Map<String, Object> params) {
        if (!StringUtils.hasText(filter.getCommunityPath())) {
            return "";
        }

        params.put("communityPath", filter.getCommunityPath().trim());
        if (filter.isIncludeDescendants()) {
            params.put("communityPrefix", filter.getCommunityPath().trim() + "/%");
            return " AND (c.path = :communityPath OR c.path LIKE :communityPrefix)";
        }
        return " AND c.path = :communityPath";
    }

    private static String orderByClause(FeedSort sort, boolean includeRelevance) {
        FeedSort effectiveSort = sort != null ? sort : FeedSort.NEW;
        String relevance = includeRelevance
            ? "ts_rank(q.search_vector, plainto_tsquery('english', :search)) DESC, "
            : "";

        return switch (effectiveSort) {
            case TOP -> " ORDER BY " + relevance + "q.score DESC, q.created_at DESC, q.id DESC";
            case HOT -> " ORDER BY q.created_at DESC, q.id DESC";
            case NEW -> " ORDER BY " + relevance + "q.created_at DESC, q.id DESC";
        };
    }

    private static void bindParams(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }
}
