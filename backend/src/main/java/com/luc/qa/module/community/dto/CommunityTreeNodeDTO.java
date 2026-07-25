package com.luc.qa.module.community.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityTreeNodeDTO {

    private Long id;
    private String path;
    private String slug;
    private String name;
    private String description;
    private int depth;
    private int questionCount;

    @Builder.Default
    private List<CommunityTreeNodeDTO> children = new ArrayList<>();
}
