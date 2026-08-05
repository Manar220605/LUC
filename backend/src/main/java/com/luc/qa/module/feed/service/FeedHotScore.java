package com.luc.qa.module.feed.service;

import com.luc.qa.module.question.entity.Question;
import java.time.Duration;
import java.time.Instant;

public final class FeedHotScore {

    private static final double EXPONENT = 1.5;
    private static final double HOUR_OFFSET = 2.0;

    private FeedHotScore() {}

    public static double compute(Question question, Instant now) {
        double hoursSinceCreated = Duration.between(question.getCreatedAt(), now).toSeconds() / 3600.0;
        return question.getScore() / Math.pow(hoursSinceCreated + HOUR_OFFSET, EXPONENT);
    }
}
