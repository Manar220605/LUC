package com.luc.qa.module.question.specification;

import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.question.entity.QuestionStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class QuestionSpecifications {

    private QuestionSpecifications() {}

    public static Specification<Question> hasStatus(QuestionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Question> inCommunityExact(String path) {
        return (root, query, cb) -> {
            Join<Question, Community> community = root.join("community");
            return cb.equal(community.get("path"), path);
        };
    }

    public static Specification<Question> inCommunityOrDescendants(String path) {
        return (root, query, cb) -> {
            Join<Question, Community> community = root.join("community");
            return cb.or(
                cb.equal(community.get("path"), path),
                cb.like(community.get("path"), path + "/%")
            );
        };
    }

    public static Specification<Question> titleOrBodyContains(String search) {
        return (root, query, cb) -> {
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("body")), like)
            );
        };
    }
}
