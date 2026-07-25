package com.luc.qa.module.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityResponseDTO {

    private Long id;
    private String path;
    private String slug;
    private String name;
    private String description;
    private int depth;
    private int questionCount;
    private String parentPath;
}
