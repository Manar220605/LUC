package com.luc.qa.module.notification.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MentionParser {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9][A-Za-z0-9._-]*)");

    private MentionParser() {}

    public static Set<String> parseHandles(String body) {
        if (body == null || body.isBlank()) {
            return Set.of();
        }

        Set<String> handles = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(body);
        while (matcher.find()) {
            handles.add(matcher.group(1));
        }
        return handles;
    }
}
