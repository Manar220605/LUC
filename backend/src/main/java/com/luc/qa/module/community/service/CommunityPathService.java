package com.luc.qa.module.community.service;

import com.luc.qa.common.exception.CommunityHierarchyException;
import com.luc.qa.module.community.entity.Community;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CommunityPathService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9_]{1,50}$");
    private static final int MAX_DEPTH = 5;

    public void validateSlug(String slug) {
        if (slug == null || !SLUG_PATTERN.matcher(slug).matches()) {
            throw new CommunityHierarchyException("Slug must match ^[a-z0-9_]{1,50}$");
        }
    }

    public String computePath(Community parent, String slug) {
        if (parent == null) {
            return slug;
        }
        return parent.getPath() + "/" + slug;
    }

    public int computeDepth(Community parent) {
        return parent == null ? 0 : parent.getDepth() + 1;
    }

    public void validateDepth(int depth) {
        if (depth > MAX_DEPTH) {
            throw new CommunityHierarchyException("Maximum community depth is " + MAX_DEPTH);
        }
    }
}
