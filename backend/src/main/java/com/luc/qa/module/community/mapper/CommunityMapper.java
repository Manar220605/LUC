package com.luc.qa.module.community.mapper;

import com.luc.qa.module.community.dto.CommunityResponseDTO;
import com.luc.qa.module.community.dto.CommunityTreeNodeDTO;
import com.luc.qa.module.community.entity.Community;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommunityMapper {

    @Mapping(target = "parentPath", source = "parent.path")
    CommunityResponseDTO toResponse(Community community);

    @Mapping(target = "children", ignore = true)
    CommunityTreeNodeDTO toTreeNode(Community community);
}
