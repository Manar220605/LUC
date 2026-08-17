package com.luc.qa.module.question.entity;

import com.luc.qa.common.entity.AuditableEntity;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id")
    private Community community;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "answer_count", nullable = false)
    @Builder.Default
    private int answerCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_answer_id")
    private Answer acceptedAnswer;

    @Column(nullable = false)
    @Builder.Default
    private int score = 0;
}
